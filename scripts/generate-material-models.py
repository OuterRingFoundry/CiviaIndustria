#!/usr/bin/env python3
"""Rebuild only original material models. Does not regenerate recipes or gameplay data."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/civitas_industria'
NS='civitas_industria'
def put(path,data):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
def tex(n):return f'{NS}:block/{n}'
def cube(name,side='steel_casing',front=None,top='service_hatch'):
 put(Path('models/block')/(name+'.json'),{'parent':'minecraft:block/orientable','textures':{'side':tex(side),'front':tex(front or side),'top':tex(top)}})
def part(a,b,t='steel_casing'):
 return {'from':a,'to':b,'faces':{f:{'texture':'#'+t} for f in ['up','down','north','south','east','west']}}
def model(name,elements):
 names={f['texture'][1:] for e in elements for f in e['faces'].values()}
 put(Path('models/block')/(name+'.json'),{'textures':{**{n:tex(n) for n in names},'particle':tex('steel_casing')},'elements':elements})
for n,f in {'factory_controller':'factory_idle','factory_controller_active':'factory_active','cargo_loader':'loader_front','cargo_unloader':'unloader_front','freight_terminal':'warehouse_front','warehouse_controller':'warehouse_front','warehouse_port':'loader_front','civic_core':'civic_panel','civic_relay':'civic_panel','logistics_node':'warehouse_front','defense_node':'defense_panel','maintenance_depot':'maintenance_panel','remediation_station':'maintenance_panel'}.items():cube(n,front=f)
cube('cargo_crate','crate_side',top='crate_top');cube('warehouse_casing','warehouse_casing',top='warehouse_casing');cube('bulk_tank','tank_side',top='tank_top')
model('pallet',[part([0,0,z],[16,2,z+3],'crate_side') for z in [1,7,12]]+[part([x,2,0],[x+3,4,16],'crate_top') for x in [0,4,9,13]])
base=[part([1,0,1],[15,2,15])]
# Inventory models include a stationary moving part; placed blocks use only the housing.
rotors={
 'gear':[part([3,7,3],[13,9,13],'brass_gear')]+[part([x,7,z],[x+4,9,z+4],'brass_gear') for x,z in [(6,1),(6,11),(1,6),(11,6)]],
 'fan':[part([2,7,6],[14,8,10],'steel_casing'),part([6,7,2],[10,8,14],'steel_casing'),part([6,6,6],[10,10,10],'brass_gear')],
 'pump':[part([7,4,7],[9,11,9],'brass_gear'),part([4,10,4],[12,12,12],'tank_top')],
 'gauge':[part([7,7,4],[8,13,5],'brass_gear')],
 'piston':[part([6,6,5],[10,10,12],'steel_casing'),part([3,3,2],[13,13,5],'unloader_front')],
 'vent':[part([3,y,4],[13,y+1,12],'steel_casing') for y in [4,7,10]]}
housings={
 'gear':base+[part([6,2,6],[10,7,10])],
 'fan':base+[part([1,2,1],[3,12,15]),part([13,2,1],[15,12,15]),part([3,2,1],[13,12,3]),part([3,2,13],[13,12,15])],
 'pump':base+[part([2,2,2],[14,5,14],'tank_side'),part([0,3,6],[5,6,10],'tank_side')],
 'gauge':base+[part([3,2,5],[13,15,9],'maintenance_panel')],
 'piston':base+[part([4,4,9],[12,12,15],'tank_side')],
 'vent':base+[part([1,2,2],[3,14,14]),part([13,2,2],[15,14,14]),part([3,12,2],[13,14,14]),part([3,2,12],[13,12,14],'maintenance_panel')]}
for name,rotor in rotors.items():
 put(Path('blockstates')/('decorative_'+name+'.json'),{'variants':{f'facing={d}':{'model':f'{NS}:block/decorative_{name}','y':y} for d,y in [('north',0),('east',90),('south',180),('west',270)]}})
 model('decorative_'+name,housings[name]);model('moving/'+name,rotor);model('decorative_'+name+'_inventory',housings[name]+rotor)
 put(Path('models/item')/('decorative_'+name+'.json'),{'parent':f'{NS}:block/decorative_{name}_inventory'})
for n in ['calibration_kit','precision_component','remediation_reagent']:
 put(Path('models/item')/(n+'.json'),{'parent':'minecraft:item/generated','textures':{'layer0':f'{NS}:item/{n}'}})
variants={}
for direction,y in [('north',0),('east',90),('south',180),('west',270)]:
 for active in [False,True]:variants[f'active={str(active).lower()},facing={direction}']={'model':f'{NS}:block/factory_controller'+('_active' if active else ''),'y':y}
put(Path('blockstates/factory_controller.json'),{'variants':variants})
print('Original material models generated')
