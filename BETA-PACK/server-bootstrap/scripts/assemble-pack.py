#!/usr/bin/env python3
"""Assemble a new isolated pack from exact local downloads. Never modify a live instance."""
import argparse,hashlib,importlib.util,json,shutil
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def main():
 p=argparse.ArgumentParser();p.add_argument('--side',choices=['server','client'],required=True);p.add_argument('--artifacts',required=True,type=Path);p.add_argument('--civitas',required=True,type=Path);p.add_argument('--output',required=True,type=Path);a=p.parse_args()
 if a.output.exists():raise SystemExit('Output already exists; choose a new release directory.')
 lock=json.loads((ROOT/'pack/mods.lock.json').read_text());selected=[x for x in lock['artifacts'] if x['side'] in ('both',a.side)]
 # Preflight all sources before creating the destination.
 for x in selected:
  if hashlib.sha256((a.artifacts/x['filename']).read_bytes()).hexdigest()!=x['sha256']:raise ValueError('Hash mismatch '+x['filename'])
 civitas=a.civitas.read_bytes();own={'filename':a.civitas.name,'side':'both','minecraft':'1.21.1','loader':'neoforge','sha256':hashlib.sha256(civitas).hexdigest(),'project':'civitas-industria'}
 a.output.mkdir(parents=True);(a.output/'mods').mkdir()
 for x in selected:shutil.copy2(a.artifacts/x['filename'],a.output/'mods'/x['filename'])
 (a.output/'mods'/a.civitas.name).write_bytes(civitas)
 shutil.copytree(ROOT/'pack/overrides',a.output,dirs_exist_ok=True)
 if a.side=='client':(a.output/'server.properties').unlink(missing_ok=True)
 assembled={'schema':1,'artifacts':selected+[own]};(a.output/'mods.lock.json').write_text(json.dumps(assembled,indent=2)+'\n')
 spec=importlib.util.spec_from_file_location('verify',ROOT/'scripts/verify-pack.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m);m.verify(assembled,a.output/'mods',a.side)
 state={str(f.relative_to(a.output)):hashlib.sha256(f.read_bytes()).hexdigest() for f in sorted(a.output.rglob('*')) if f.is_file()}
 (a.output/'pack-state.json').write_text(json.dumps({'minecraft':'1.21.1','neoforge':'21.1.249','side':a.side,'files':state},indent=2)+'\n')
 print('Verified pack assembled:',a.output)
if __name__=='__main__':main()
