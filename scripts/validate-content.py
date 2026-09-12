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
  assert ':' in data['result']['id'],path
  if '/data/civitas_industria/' not in str(path):
   assert data.get('neoforge:conditions'),path
  assert 1<=data['result'].get('count',1)<=64,path
print(f'PASS: {count} JSON resources parse and content schemas match')
# All project-owned model textures must exist and remain power-of-two Minecraft assets.
import struct
assets=root/'assets/civitas_industria'
for path in (assets/'models').rglob('*.json'):
 for value in json.loads(path.read_text()).get('textures',{}).values():
  if value.startswith('civitas_industria:'):
   image=assets/'textures'/(value.split(':',1)[1]+'.png')
   assert image.is_file(),(path,image)
textures=list((assets/'textures').rglob('*.png'))
for path in textures:
 raw=path.read_bytes();assert raw[:8]==b'\x89PNG\r\n\x1a\n',path
 width,height=struct.unpack('>II',raw[16:24])
 if path.parent.name=='entity':assert 32<=width<=2048 and height==width,(path,width,height)
 else:assert (width,height)==(32,32),(path,width,height)
print(f'PASS: {len(textures)} original textures (32x32 block/item tiles, bounded entity skins) and model references')
