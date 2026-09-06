#!/usr/bin/env python3
"""Offline structural checks for generated Civitas resources; game loader remains authoritative."""
import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]/'src/main/resources';count=0
for path in root.rglob('*.json'):
 data=json.loads(path.read_text());count+=1
 if '/environment/emissions/' in str(path):
  assert {'target','industrial_load','emissions'}<=data.keys(),path
 if '/recipe/' in str(path) and data.get('type','').startswith('minecraft:crafting'):
  assert data['result']['id'].startswith('civitas_industria:'),path
  assert 1<=data['result'].get('count',1)<=64,path
print(f'PASS: {count} JSON resources parse and content schemas match')
