#!/usr/bin/env python3
"""Start an isolated dev server, verify /ci version, stop cleanly; retain logs."""
import pathlib
import queue
import subprocess
import threading
import time
import sys

root = pathlib.Path(__file__).resolve().parents[1]
run_dir = root / "run"
run_dir.mkdir(exist_ok=True)
# Dedicated, disposable CI/dev world only. Never point this at production.
(run_dir / "eula.txt").write_text("eula=true\n", encoding="utf-8")
(run_dir / "server.properties").write_text(
    "server-ip=127.0.0.1\nserver-port=25585\nlevel-name=smoke-world\n"
    "max-players=1\nview-distance=2\nsimulation-distance=2\n"
    "enable-rcon=false\nonline-mode=true\n", encoding="utf-8")
cmd = ["cmd", "/c", "gradlew.bat"] if sys.platform == "win32" else ["bash", "./gradlew"]
process = subprocess.Popen(cmd + ["--no-daemon", "--console=plain", "runServer"],
    cwd=root, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
    text=True, encoding="utf-8", errors="replace", bufsize=1)
lines = queue.Queue()

def read_output():
    for line in process.stdout:
        lines.put(line)
    lines.put(None)

threading.Thread(target=read_output, daemon=True).start()
deadline = time.monotonic() + 600
ready = version_seen = False
log_dir = root / "build" / "smoke"
log_dir.mkdir(parents=True, exist_ok=True)
try:
    with (log_dir / "server.log").open("w", encoding="utf-8") as log:
        while time.monotonic() < deadline:
            try:
                line = lines.get(timeout=1)
            except queue.Empty:
                if process.poll() is not None:
                    break
                continue
            if line is None:
                break
            log.write(line)
            log.flush()
            print(line, end="", flush=True)
            if not ready and "Done (" in line:
                ready = True
                process.stdin.write("ci version\n")
                process.stdin.flush()
            if ready and all(token in line for token in
                    ("Civitas Industria", "| data 1", "| Minecraft 1.21.1", "| NeoForge", "| Java 21")):
                version_seen = True
                process.stdin.write("stop\n")
                process.stdin.flush()
                break
        if not (ready and version_seen):
            raise RuntimeError("Server boot or /ci version failed; inspect build/smoke/server.log")
        # Drain output while the server saves and Gradle exits.
        while time.monotonic() < deadline:
            try:
                line = lines.get(timeout=1)
            except queue.Empty:
                continue
            if line is None:
                break
            log.write(line)
            log.flush()
        result = process.wait(timeout=max(1, deadline - time.monotonic()))
        if result != 0:
            raise RuntimeError(f"Server exited with code {result}")
finally:
    if process.poll() is None:
        try:
            process.stdin.write("stop\n")
            process.stdin.flush()
            process.wait(timeout=30)
        except (OSError, subprocess.TimeoutExpired):
            process.kill()
            process.wait()
