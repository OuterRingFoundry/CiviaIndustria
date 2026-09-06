#!/usr/bin/env python3
"""Install and fingerprint the pinned NeoForge runtime into an assembled instance."""
import argparse,hashlib,json,subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def main():
 p=argparse.ArgumentParser();p.add_argument('--instance',required=True,type=Path);p.add_argument('--installer',required=True,type=Path);a=p.parse_args()
 lock=json.loads((ROOT/'pack/runtime.lock.json').read_text())
 if hashlib.sha256(a.installer.read_bytes()).hexdigest()!=lock['installer']['sha256']:raise ValueError('Installer checksum mismatch')
 if not (a.instance/'mods.lock.json').is_file():raise ValueError('Assembled pack required')
 subprocess.run(['java','-jar',str(a.installer.resolve()),'--installServer',str(a.instance.resolve())],check=True)
 state=json.loads((a.instance/'pack-state.json').read_text())
 for path in a.instance.rglob('*'):
  if path.is_file() and path.name!='pack-state.json' and not {'logs','world'}&set(path.relative_to(a.instance).parts):
   with path.open('rb') as file:state['files'][str(path.relative_to(a.instance))]=hashlib.file_digest(file,'sha256').hexdigest()
 (a.instance/'runtime.lock.json').write_text(json.dumps(lock,indent=2)+'\n')
 state['files']['runtime.lock.json']=hashlib.sha256((a.instance/'runtime.lock.json').read_bytes()).hexdigest()
 (a.instance/'pack-state.json').write_text(json.dumps(state,indent=2)+'\n');print('Runtime installed and fingerprinted')
if __name__=='__main__':main()
