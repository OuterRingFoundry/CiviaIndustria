"""Run disposable Linux Gradle servers, preserving saves before timeout cleanup."""
import os
import signal
import subprocess
import time
from pathlib import Path


def descendants(root):
    rows = subprocess.check_output(['ps', '-eo', 'pid=,ppid='], text=True).splitlines()
    parents = {int(p): int(pp) for p, pp in (row.split() for row in rows)}
    found = {root}
    while True:
        more = {pid for pid, parent in parents.items() if parent in found} - found
        if not more:
            return found
        found.update(more)


def identity(pid):
    """A PID may be reused while graceful shutdown is pending."""
    try:
        fields = Path(f'/proc/{pid}/stat').read_text().rsplit(') ', 1)[1].split()
        return None if fields[0] == 'Z' else fields[19]
    except (FileNotFoundError, ProcessLookupError):
        return None


def remaining(owned):
    return [pid for pid, born in owned.items() if born is not None and identity(pid) == born]


def signal_owned(owned, sig):
    for pid in remaining(owned):
        try:
            os.kill(pid, sig)
        except ProcessLookupError:
            pass


def run(command, cwd, log, timeout):
    if '--no-daemon' not in command:
        raise ValueError('Fixture cleanup requires a dedicated single-use Gradle daemon')
    process = subprocess.Popen(command, cwd=cwd, stdin=subprocess.PIPE, stdout=log,
                               stderr=subprocess.STDOUT, text=True, start_new_session=True)
    try:
        return process.wait(timeout=timeout)
    except (subprocess.TimeoutExpired, KeyboardInterrupt):
        # Gradle's single-use daemon may have its own process group. Capture the
        # actual descendants; killing only the wrapper's group leaves Java alive.
        owned = {pid: identity(pid) for pid in descendants(process.pid)}
        try:
            process.stdin.write('stop\n')
            process.stdin.flush()
        except (BrokenPipeError, OSError):
            pass
        try:
            process.wait(timeout=30)
        except subprocess.TimeoutExpired:
            owned.update({pid: identity(pid) for pid in descendants(process.pid) if pid not in owned})
        # Also reap detached children when the wrapper exits before its children.
        signal_owned(owned, signal.SIGTERM)
        deadline = time.monotonic() + 15
        while remaining(owned) and time.monotonic() < deadline:
            time.sleep(.1)
        signal_owned(owned, signal.SIGKILL)
        process.wait()
        raise
    finally:
        process.stdin.close()
