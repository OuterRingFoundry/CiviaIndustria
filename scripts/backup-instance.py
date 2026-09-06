#!/usr/bin/env python3
"""Offline full-instance backup and safe restore to a new directory. Requires Python 3.12+."""
import argparse,datetime,hashlib,json,os,tarfile,tempfile
from pathlib import Path
TIERS={'hourly':24,'daily':14,'weekly':8,'monthly':6}
def backup(instance,destination,tier):
 # Java FileChannel locks use POSIX record locks on supported Linux Minecraft servers.
 if not instance.is_dir() or not (instance/"mods.lock.json").is_file():raise ValueError("Instance and exact mods.lock.json required")
 import fcntl
 if instance.resolve()==destination.resolve() or instance.resolve() in destination.resolve().parents:raise ValueError("Backup destination must be outside instance")
 locks=[]
 try:
  for path in instance.glob('**/session.lock'):
   handle=path.open('r+b');locks.append(handle);fcntl.lockf(handle,fcntl.LOCK_EX|fcntl.LOCK_NB)
  destination.mkdir(parents=True,exist_ok=True);stamp=datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%S%fZ')
  target=destination/f'{tier}-{stamp}.tar.gz'
  with tempfile.NamedTemporaryFile(dir=destination,delete=False,suffix='.partial') as file:temp=Path(file.name)
  try:
   with tarfile.open(temp,'w:gz') as archive:
    for path in sorted(instance.rglob('*')):
     if path.is_symlink():raise ValueError('Symlink requires explicit backup policy: '+str(path))
     if path.is_file() and not {'logs','crash-reports'}&set(path.relative_to(instance).parts):archive.add(path,arcname=str(path.relative_to(instance)),recursive=False)
   os.replace(temp,target)
  finally:temp.unlink(missing_ok=True)
  with target.open('rb') as file:digest=hashlib.file_digest(file,'sha256').hexdigest()
  target.with_suffix(target.suffix+'.sha256').write_text(digest+'\n')
  for old in sorted(destination.glob(tier+'-*.tar.gz'),reverse=True)[TIERS[tier]:]:old.unlink();old.with_suffix(old.suffix+'.sha256').unlink(missing_ok=True)
  return target
 finally:
  for handle in locks:handle.close()
def restore(archive,output):
 if output.exists():raise ValueError('Restore requires a new destination')
 expected=archive.with_suffix(archive.suffix+'.sha256').read_text().strip()
 with archive.open('rb') as file:
  if hashlib.file_digest(file,'sha256').hexdigest()!=expected:raise ValueError('Backup checksum mismatch')
 output.mkdir(parents=True)
 with tarfile.open(archive) as file:file.extractall(output,filter='data')
 return output
if __name__=='__main__':
 p=argparse.ArgumentParser();sub=p.add_subparsers(dest='command',required=True)
 b=sub.add_parser('backup');b.add_argument('instance',type=Path);b.add_argument('destination',type=Path);b.add_argument('--tier',choices=TIERS,default='hourly')
 r=sub.add_parser('restore');r.add_argument('archive',type=Path);r.add_argument('output',type=Path);a=p.parse_args()
 print(backup(a.instance,a.destination,a.tier) if a.command=='backup' else restore(a.archive,a.output))
