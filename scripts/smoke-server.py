#!/usr/bin/env python3
"""Start an isolated dev server, verify /ci version, stop cleanly; retain logs."""
import pathlib
import queue
import subprocess
import threading
import time
import sys
import argparse
import os
import signal

parser = argparse.ArgumentParser()
parser.add_argument("--runtime-write", action="store_true")
parser.add_argument("--runtime-read", action="store_true")
args = parser.parse_args()
steps = [("ci version", ("Civitas Industria", "| data 2", "| Minecraft 1.21.1", "| NeoForge", "| Java 21"))]
if args.runtime_write or args.runtime_read:
    for dimension in ("overworld", "the_nether", "the_end"):
        if args.runtime_write:
            steps.append((f"execute in minecraft:{dimension} positioned -1000001 64 -1000001 run ci env clear all", ("CI cleared pollutants",)))
            steps.append((f"execute in minecraft:{dimension} positioned -1000001 64 -1000001 run ci env add sox 123", ("CI added sox",)))
        steps.append((f"execute in minecraft:{dimension} run ci env inspect -15626 -15626", ("CI cell", "AQI 123.0", "acid 123.0")))
    steps.append(("ci civilization networks", ("CI networks",)))
    steps.append(("ci perf", ("CI stored cells",)))
step_index = 0
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
    text=True, encoding="utf-8", errors="replace", bufsize=1, start_new_session=(os.name!="nt"))
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
                process.stdin.write(steps[step_index][0]+"\n")
                process.stdin.flush()
            if ready and step_index<len(steps) and all(token in line for token in steps[step_index][1]):
                step_index += 1
                if step_index == len(steps):
                    version_seen = True
                    process.stdin.write("stop\n")
                    process.stdin.flush()
                    break
                process.stdin.write(steps[step_index][0]+"\n")
                process.stdin.flush()
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
        server_log=(log_dir / "server.log").read_text(encoding="utf-8")
        if "Encountered an unexpected exception" in server_log or "All dimensions are saved" not in server_log:
            raise RuntimeError("Server crashed or did not confirm saved dimensions")
        if result != 0:
            raise RuntimeError(f"Server exited with code {result}")
finally:
    if process.poll() is None:
        try:
            process.stdin.write("stop\n")
            process.stdin.flush()
            process.wait(timeout=30)
        except (OSError, subprocess.TimeoutExpired):
            if os.name=="nt":
                process.kill()
            else:
                os.killpg(process.pid,signal.SIGKILL)
            process.wait()
