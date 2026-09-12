#!/usr/bin/env python3
"""Deterministic Minecraft-material finish and original cuboid machine detailing.
Run after the historical geometry generators. No copied third-party bitmap assets.
"""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/civitas_industria/models'
PALETTE={
 'workshop_iron':'iron_block','workshop_cast_iron':'gray_concrete','workshop_brass':'gold_block',
 'workshop_coil':'copper_block','workshop_bearing':'polished_andesite','workshop_gauge':'quartz_block_bottom',
 'workshop_timber':'spruce_planks','workshop_tank':'oxidized_copper','workshop_vent':'gray_concrete',
 'filter_mesh':'gray_concrete','wash_window':'cyan_terracotta','steel_casing':'polished_andesite',
 'brass_gear':'gold_block','tank_side':'oxidized_copper','tank_top':'copper_block'}
def write(p,data):p.write_text(json.dumps(data,indent=2)+'\n')
for p in sorted(ROOT.rglob('*.json')):
 data=json.loads(p.read_text())
 for key,value in data.get('textures',{}).items():
  if key in PALETTE:data['textures'][key]='minecraft:block/'+PALETTE[key]
  elif value.endswith('/factory_active'):data['textures'][key]='minecraft:block/redstone_lamp_on'
  elif value.endswith('/factory_idle'):data['textures'][key]='minecraft:block/redstone_lamp'
 if p.parent.name=='block' and p.stem=='factory_controller_active':data['textures']['status']='minecraft:block/redstone_lamp_on'
 elif p.parent.name=='block' and p.stem=='factory_controller':data['textures']['status']='minecraft:block/redstone_lamp'
 write(p,data)
MAT={'stone':'polished_andesite','iron':'iron_block','dark':'gray_concrete','brass':'gold_block',
 'copper':'copper_block','wood':'spruce_planks','paper':'quartz_block_bottom','green':'oxidized_copper',
 'black':'black_concrete','blue':'cyan_terracotta','map':'cartography_table_top'}
def part(a,b,t,**overrides):
 return {'from':a,'to':b,'faces':{f:{'texture':'#'+overrides.get(f,t)} for f in ('up','down','north','south','east','west')}}
def panel(x,y,w,h,t,z=-.125):return part([x,y,z],[x+w,y+h,0],t)
def frame(material='stone'):
 e=[part([2,2,0],[14,14,16],material)]
 # Raised corner strips and top/bottom bands have distinct silhouettes at inventory scale.
 for x in (0,14):e.append(part([x,0,-.125],[x+2,16,16.125],'iron'))
 for y in (0,14):e.append(part([2,y,-.125],[14,y+2,16.125],'dark'))
 return e

def dial(x=3,y=8):return [panel(x,y,5,5,'brass'),panel(x+1,y+1,3,3,'paper',-.25),panel(x+2,y+2,1,2,'black',-.375)]
def vents(x=9,y=3):return [panel(x,y+i*2,4,1,'black',-.25) for i in range(4)]
def model(name,e):
 used={face['texture'][1:] for el in e for face in el['faces'].values()}
 write(ROOT/'block'/f'{name}.json',{'parent':'minecraft:block/block','textures':{**{k:'minecraft:block/'+MAT[k] for k in sorted(used)},'particle':'minecraft:block/'+MAT['stone']},'elements':e})
for name in ('civic_core','civic_relay','logistics_node','defense_node','maintenance_depot'):
 e=frame()+[panel(2,2,12,12,'dark',-.0625)]
 if name=='civic_core':e+=dial(5,7)+[panel(4,3,8,2,'brass')]
 elif name=='civic_relay':
  e += [panel(3,3,2,10,'copper'),panel(7,5,2,8,'copper'),panel(11,7,2,6,'copper')]
 elif name=='logistics_node':e += [panel(3,4,10,2,'brass'),panel(9,6,4,2,'brass'),panel(7,8,4,2,'brass')]
 elif name=='defense_node':e += [panel(4,6,8,7,'iron'),panel(6,3,4,3,'iron'),panel(7,6,2,7,'brass',-.25)]
 else:e += [panel(3,4,10,2,'iron'),panel(4,9,8,2,'brass'),panel(6,2,4,2,'black',-.25)]
 model(name,e)
model('cargo_crate',frame('wood')+[panel(3,6,10,4,'wood'),panel(7,6,2,4,'brass',-.25)])
model('warehouse_casing',frame('wood')+[panel(3,3,10,2,'iron'),panel(3,11,10,2,'iron')])
model('warehouse_controller',frame('wood')+[panel(3,3,10,10,'dark',-.0625)]+dial(5,7)+[panel(5,4,6,1,'brass',-.25)])
model('warehouse_port',frame('wood')+[panel(3,3,10,10,'iron'),panel(4,4,8,8,'black',-.25),panel(5,6,6,2,'brass',-.375)])
for name,metal in [('cargo_loader','copper'),('cargo_unloader','green')]:
 model(name,frame()+[panel(2,2,12,12,'dark',-.0625),panel(3,6,10,4,metal),panel(5,4,6,2,metal),panel(5,10,6,2,metal),panel(5,7,6,2,'black',-.25)])
model('bulk_tank',frame('green')+[panel(6,3,4,10,'brass'),panel(7,4,2,8,'blue',-.25),panel(7,10,2,2,'paper',-.375)])
model('market_counter',[part([2,0,0],[14,14,16],'wood'),part([0,14,0],[16,16,16],'wood',up='map'),panel(2,2,12,10,'dark'),panel(3,3,10,8,'wood',-.25),panel(7,8,2,1,'brass',-.375),part([0,0,0],[2,14,16],'iron'),part([14,0,0],[16,14,16],'iron')])
print('Finished industrial material palette and 13 distinct framed block models')
