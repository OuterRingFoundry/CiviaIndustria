#!/usr/bin/env python3
"""Timeout cleanup must stop a detached child even after its wrapper exits."""
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path
from fixture_process import identity, run


class FixtureProcessTests(unittest.TestCase):
    def test_detached_child_cleanup(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            wrapper = root / 'wrapper.py'
            wrapper.write_text('''import subprocess,sys
from pathlib import Path
child=subprocess.Popen([sys.executable,'-c','import time; time.sleep(120)'],start_new_session=True)
Path('child.pid').write_text(str(child.pid))
assert sys.stdin.readline().strip()=='stop'
''')
            with (root / 'log').open('w') as log:
                with self.assertRaises(subprocess.TimeoutExpired):
                    run([sys.executable, str(wrapper), '--no-daemon'], root, log, 1)
            self.assertIsNone(identity(int((root / 'child.pid').read_text())))

    def test_refuses_shared_daemon(self):
        with self.assertRaises(ValueError):
            run(['gradle', 'runServer'], '.', None, 1)


if __name__ == '__main__':
    unittest.main()
