#!/usr/bin/env python3
"""Exercise actual NeoForge startup against damaged copies of the disposable smoke save."""
import gzip
import hashlib
import os
from pathlib import Path
import signal
import struct
import subprocess

root = Path(__file__).resolve().parents[1]
run = root / "run"
properties = (run / "server.properties").read_text()
assert "level-name=smoke-world\n" in properties and "server-ip=127.0.0.1\n" in properties
target = run / "smoke-world" / "data" / "civitas_industria.dat"
original = target.read_bytes()
raw = gzip.decompress(original)
field = b"\x03\x00\x0bdataVersion" + struct.pack(">i", 3)
assert raw.count(field) == 1, "Expected one schema-3 envelope"
cases = {
    "future": gzip.compress(raw.replace(field, field[:-4] + struct.pack(">i", 99))),
    "mismatch": gzip.compress(raw.replace(field, field[:-4] + struct.pack(">i", 1))),
    "truncated": original[:16],
}
logs = root / "build" / "save-refusal"
logs.mkdir(parents=True, exist_ok=True)
try:
    for name, damaged in cases.items():
        target.write_bytes(damaged)
        before = hashlib.sha256(damaged).hexdigest()
        with (logs / (name + ".log")).open("w") as output:
            process = subprocess.Popen(["bash", "./gradlew", "--console=plain", "runServer"],
                cwd=root, stdin=subprocess.DEVNULL, stdout=output, stderr=subprocess.STDOUT,
                start_new_session=True)
            try:
                code = process.wait(timeout=180)
            except subprocess.TimeoutExpired:
                os.killpg(process.pid, signal.SIGKILL)
                process.wait()
                raise
        log = (logs / (name + ".log")).read_text()
        # Minecraft catches startup exceptions and can exit 0; Gradle success is not a boot oracle.
        assert "Done (" not in log, f"{name}: incompatible save reached server readiness"
        assert "Encountered an unexpected exception" in log, f"{name}: expected startup rejection absent"
        assert "refusing to overwrite" in log, f"{name}: startup failed for an unrelated reason"
        assert hashlib.sha256(target.read_bytes()).hexdigest() == before, f"{name}: damaged bytes changed"
        print(f"PASS {name}: startup refused, original damaged bytes retained (launcher exit {code})", flush=True)
finally:
    target.write_bytes(original)
    assert target.read_bytes() == original
