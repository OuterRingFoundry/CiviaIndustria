#!/usr/bin/env python3
"""Validate immutable pack files before launch; mutable worlds/configs remain separate."""
import argparse,hashlib,json
from pathlib import Path
from pack_manifest import properties
def verify(root):
 manifest=json.loads((root/'pack-state.json').read_text());files=manifest['files']
 def safe(name):
  path=Path(name)
  if path.is_absolute() or '..' in path.parts or not name:raise ValueError('Unsafe manifest path')
  return path
 if not files:raise ValueError('Empty pack manifest')
 for name,digest in files.items():
  path=safe(name)
  if hashlib.sha256((root/path).read_bytes()).hexdigest()!=digest:raise ValueError('Pack changed: '+name)
 for name,expected_values in manifest.get("properties",{}).items():
  if properties(root/safe(name))!=expected_values:raise ValueError("Properties changed: "+name)
 managed=manifest.get("managed_prefixes",[])
 expected_paths=set(files)|set(manifest.get("properties",{}))
 for prefix in managed:
  for path in (root/safe(prefix)).rglob("*"):
   if path.is_file() and path.relative_to(root).as_posix() not in expected_paths:raise ValueError("Unexpected pack file: "+str(path))
 expected={Path(n).name for n in files if Path(n).parent==Path('mods')}
 if {p.name for p in (root/'mods').glob('*.jar')}!=expected:raise ValueError('Unexpected mod jars')
 return manifest
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('instance',type=Path);a=p.parse_args();verify(a.instance);print('PASS: exact pack, configs and scripts')
