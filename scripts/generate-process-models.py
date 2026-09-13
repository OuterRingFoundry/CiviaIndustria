#!/usr/bin/env python3
"""Original bounded baked geometry for the integrated mechanical/chemical workshop."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/civitas_industria'
NS='civitas_industria'
def put(path,data):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
def part(a,b,t,**faces):
 return {'from':a,'to':b,'faces':{f:{'texture':'#'+faces.get(f,t)} for f in ('up','down','north','south','east','west')}}
def model(path,elements):
 names={f['texture'][1:] for e in elements for f in e['faces'].values()}
 put(Path('models')/(path+'.json'),{'parent':'minecraft:block/block','textures':{**{n:f'{NS}:block/process/{n}' for n in sorted(names)},'particle':f'{NS}:block/process/workshop_iron'},'elements':elements})
iron='workshop_iron';brass='workshop_brass';dark='workshop_cast_iron';wood='workshop_timber';tank='workshop_tank'
base=[part([0,0,0],[16,2,16],dark)]
factory=base+[
 part([1,2,2],[15,13,15],iron,north='workshop_vent'),
 part([2,7,1],[7,12,2],brass,north='workshop_gauge'),
 part([9,6,1],[14,11,2],dark,north='workshop_coil'),
 part([2,3,1],[7,5,2],dark,north='filter_mesh'),
 part([1,13,2],[15,14,15],brass),
 part([5,14,5],[11,16,11],dark,up='workshop_bearing'),
 part([0,4,5],[1,11,13],dark,west='workshop_vent'),
 part([15,4,5],[16,11,13],dark,east='workshop_vent')]
for active in (False,True):
 name='factory_controller'+('_active' if active else '')
 model('block/'+name,factory+[part([9,12,1],[13,13,2],brass)])
 p=ROOT/'models/block'/f'{name}.json';data=json.loads(p.read_text())
 data['textures']['status']=f'{NS}:block/factory_'+('active' if active else 'idle')
 data['elements'][-1]['faces']['north']['texture']='#status'
 data['elements'][-1]['faces']['north']['uv']=[0,0,16,8]
 put(Path('models/block')/(name+'.json'),data)
model('block/remediation_station',base+[
 part([2,2,4],[11,13,14],tank),part([1,3,3],[12,5,15],brass),part([1,11,3],[12,13,15],brass),
 part([3,13,5],[10,15,12],dark,up='filter_mesh'),
 part([4,6,3],[9,10,4],dark,north='wash_window'),
 part([12,2,4],[15,11,13],iron,east='workshop_vent'),
 part([12,7,3],[16,11,4],brass,north='workshop_gauge'),
 part([11,4,7],[13,6,10],brass),part([4,15,7],[9,16,9],brass)])
model('block/freight_terminal',base+[
 part([1,2,1],[15,12,15],wood),part([1,12,1],[15,14,15],iron),
 part([2,3,0],[14,8,1],dark,north='workshop_vent'),
 part([2,9,0],[7,12,1],brass,north='workshop_gauge'),
 part([10,9,0],[13,12,1],brass,north='workshop_bearing'),
 part([0,2,3],[1,12,5],iron),part([15,2,3],[16,12,5],iron)])
put(Path('models/block/gypsum_panel.json'),{'parent':'minecraft:block/cube_bottom_top','textures':{'side':f'{NS}:block/process/limestone_surface','bottom':f'{NS}:block/process/filter_cloth','top':f'{NS}:block/process/filter_cloth'}})
put(Path('blockstates/gypsum_panel.json'),{'variants':{'':{'model':f'{NS}:block/gypsum_panel'}}})
put(Path('models/item/gypsum_panel.json'),{'parent':f'{NS}:block/gypsum_panel'})
# Open sacks and lidded canisters are ordinary baked items, with no renderer or tick cost.
for name,contents in {'limestone_dust':'limestone_surface','quicklime':'quicklime_granules','hydrated_lime':'filter_cloth','gypsum_binder':'quicklime_granules'}.items():
 model('item/'+name,[part([3,1,4],[13,10,12],'sorbent_sack'),part([4,10,5],[12,12,11],contents),part([3,9,4],[4,12,12],'sorbent_sack'),part([12,9,4],[13,12,12],'sorbent_sack')])
model('item/sulfate_filter_cake',[part([3,2,3],[13,4,13],dark),part([4,4,4],[12,7,12],'sulfate_cake'),part([5,7,5],[11,8,11],'sulfate_cake')])
model('item/remediation_reagent',[part([4,2,4],[12,11,12],tank,north='filter_cloth'),part([3,11,3],[13,13,13],brass),part([4,1,4],[12,2,12],dark)])
component=[part([2,3,3],[14,5,13],iron),part([4,5,5],[12,7,13],brass,up='workshop_bearing'),part([3,5,3],[7,8,5],dark,north='workshop_coil')]
model('item/incomplete_precision_component',component[:2])
model('item/precision_component',component+[part([2,5,6],[4,7,10],dark),part([12,5,6],[14,7,10],dark)])
# Mechanical animation parts use plain material faces; geometry supplies their silhouettes.
for p in (ROOT/'models/block').rglob('*.json'):
 if p.name.startswith('decorative_') or p.parent.name=='moving':
  data=json.loads(p.read_text())
  replacements={'brass_gear':brass,'steel_casing':iron,'tank_side':tank,'tank_top':'workshop_bearing'}
  for key in replacements:
   if key in data.get('textures',{}):data['textures'][key]=f'{NS}:block/process/{replacements[key]}'
  put(p.relative_to(ROOT),data)
print('Generated process models: three machines, gypsum panels, eight ingredient assemblies')
if __name__=='__main__':pass
