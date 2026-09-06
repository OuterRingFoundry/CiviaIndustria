#!/usr/bin/env python3
import importlib.util,tempfile,unittest,subprocess,sys
from pathlib import Path
spec=importlib.util.spec_from_file_location('backup',Path(__file__).with_name('backup-instance.py'));m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
class BackupTests(unittest.TestCase):
 def test_restore_and_corruption(self):
  with tempfile.TemporaryDirectory() as d:
   root=Path(d);source=root/'instance';source.mkdir();(source/'world').mkdir();(source/'world/level.dat').write_bytes(b'world');(source/'mods.lock.json').write_text('{"exact":"version"}')
   a=m.backup(source,root/'backups','hourly');m.restore(a,root/'restored');self.assertEqual((root/'restored/world/level.dat').read_bytes(),b'world')
   with self.assertRaises(ValueError):m.restore(a,root/'restored')
   with self.assertRaises(ValueError):m.backup(source,source/'inside','hourly')
   a.write_bytes(b'corrupt')
   with self.assertRaises(ValueError):m.restore(a,root/'bad')
 def test_running_world_refused(self):
  with tempfile.TemporaryDirectory() as d:
   root=Path(d);source=root/'instance';(source/'world').mkdir(parents=True);lock=source/'world/session.lock';lock.write_bytes(b'lock');(source/'mods.lock.json').write_text('{}')
   process=subprocess.Popen([sys.executable,'-c','import fcntl,sys; f=open(sys.argv[1],"r+b"); fcntl.lockf(f,fcntl.LOCK_EX); print("ready",flush=True); sys.stdin.read()',str(lock)],stdin=subprocess.PIPE,stdout=subprocess.PIPE,text=True)
   try:
    self.assertEqual(process.stdout.readline().strip(),'ready')
    with self.assertRaises(BlockingIOError):m.backup(source,root/'backups','hourly')
   finally:process.communicate('stop',timeout=5)
if __name__=='__main__':unittest.main()
