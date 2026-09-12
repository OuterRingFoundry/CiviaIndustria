#!/usr/bin/env python3
"""Rebuild only the 0.4 workshop's original geometry and acquisition resources."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources'
NS='civitas_industria'
def put(path,data):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
def box(a,b,t):return {'from':a,'to':b,'faces':{d:{'texture':'#'+t} for d in ('up','down','north','south','east','west')}}
def model(path,parts):
 textures={f['texture'][1:] for p in parts for f in p['faces'].values()}
 put('assets/'+NS+'/models/'+path+'.json',{'parent':'minecraft:block/block','textures':{**{t:NS+':block/process/'+t for t in textures},'particle':NS+':block/process/workshop_iron'},'elements':parts})
iron='workshop_iron';brass='workshop_brass';dark='workshop_cast_iron';coil='workshop_coil';gauge='workshop_gauge';bearing='workshop_bearing'
parts=[box([1,0,2],[15,2,14],dark),box([2,2,3],[5,6,13],iron),box([11,2,3],[14,6,13],iron),box([1,6,2],[15,8,14],iron),box([1,8,5],[5,14,12],dark),box([2,14,6],[5,15,11],brass),box([6,8,5],[15,9,7],brass),box([6,8,10],[15,9,12],brass),box([10,9,6],[13,10,11],dark),box([11,10,7],[12,12,10],brass),box([13,8,12],[15,15,14],iron),box([9,14,11],[15,16,14],dark),box([3,8,1],[7,12,2],gauge),box([11,5,1],[13,7,3],brass)]
model('block/precision_workbench',parts)
variants={}
for direction,y in [('north',0),('east',90),('south',180),('west',270)]:
 for active in ('false','true'):
  for mode in range(3):variants[f'facing={direction},active={active},operation={mode}']={'model':NS+':block/precision_workbench','y':y}
put('assets/'+NS+'/blockstates/precision_workbench.json',{'variants':variants})
model('block/moving/workshop_chuck',[box([5,9,6],[7,13,11],brass),box([7,10,7],[10,12,10],iron),box([6,8,8],[7,14,9],dark)])
model('block/moving/workshop_drill',[box([10,11,12],[12,14,14],brass),box([10.5,8,12.5],[11.5,11,13.5],iron)])
for name in ('electric_motor','rotation_dynamo'):
 model('block/'+name,[box([1,0,2],[15,3,15],dark),box([2,3,5],[14,12,14],iron),box([4,4,3],[12,11,5],brass),box([5,5,1],[11,10,3],bearing),box([3,12,6],[13,14,13],coil),box([13,5,7],[16,10,12],gauge),box([1,5,6],[2,11,13],dark)])
 variants={}
 for facing,rotation in {'north':{},'south':{'y':180},'east':{'y':90},'west':{'y':270},'up':{'x':270},'down':{'x':90}}.items():variants['facing='+facing]={'model':NS+':block/'+name,**rotation}
 put('assets/'+NS+'/blockstates/'+name+'.json',{'variants':variants})
model('block/moving/power_rotor',[box([6,6,0],[10,10,4],iron),box([5,7,1],[11,9,2],brass),box([7,5,1],[9,11,2],brass)])
for name in ('precision_workbench','electric_motor','rotation_dynamo'):
 put('assets/'+NS+'/models/item/'+name+'.json',{'parent':NS+':block/'+name})
 loot={'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':NS+':'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]}
 if name!='precision_workbench':loot['neoforge:conditions']=[{'type':'neoforge:mod_loaded','modid':'create'}]
 put('data/'+NS+'/loot_table/blocks/'+name+'.json',loot)
model('item/precision_shaft',[box([2,6,6],[14,10,10],iron),box([3,5,5],[5,11,11],brass),box([11,5,5],[13,11,11],brass)])
model('item/gear_blank',[box([3,3,6],[13,13,10],iron),box([1,6,6],[15,10,10],brass),box([6,1,6],[10,15,10],brass),box([6,6,5],[10,10,11],dark)])
model('item/mounting_plate',[box([2,3,5],[14,13,7],iron),box([4,5,4],[6,7,5],dark),box([10,9,4],[12,11,5],dark)])
model('item/cutting_insert',[box([4,4,6],[12,12,10],dark),box([4,4,5],[12,7,6],brass),box([8,8,4],[12,12,6],iron)])
def recipe(name,pattern,key,out=None,conditions=None):
 d={'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:{'item':v} for k,v in key.items()},'result':{'id':NS+':'+(out or name)}}
 if conditions:d['neoforge:conditions']=conditions
 put('data/'+NS+'/recipe/'+name+'.json',d)
recipe('precision_workbench',['ICI','PKP','III'],{'I':'minecraft:iron_ingot','C':'minecraft:copper_ingot','P':NS+':precision_component','K':'minecraft:crafting_table'})
recipe('cutting_insert',[' D','I '],{'D':'minecraft:diamond','I':'minecraft:iron_ingot'})
recipe('precision_shaft_manual',[' N ','III',' N '],{'N':'minecraft:iron_nugget','I':'minecraft:iron_ingot'},'precision_shaft')
recipe('gear_blank_manual',[' I ','III',' I '],{'I':'minecraft:iron_ingot'},'gear_blank')
recipe('mounting_plate_manual',['I I',' I '],{'I':'minecraft:iron_ingot'},'mounting_plate')
cond=[{'type':'neoforge:mod_loaded','modid':'create'}]
for name in ('electric_motor','rotation_dynamo'):
 recipe(name,['ICI','PSP','IGI'],{'I':'minecraft:iron_ingot','C':'minecraft:copper_block','P':NS+':precision_component','S':NS+':precision_shaft','G':NS+':gear_blank'},conditions=cond)
# Efficient finished-machine alternative; existing basic assembly remains the bootstrap.
put('data/'+NS+'/recipe/machined_precision_component.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':NS+':precision_shaft'},{'item':NS+':gear_blank'},{'item':NS+':mounting_plate'},{'item':'minecraft:copper_ingot'}],'result':{'id':NS+':precision_component','count':2}})
for mode,(name,tag,ticks) in enumerate([('precision_shaft','c:ingots/iron',100),('gear_blank','c:plates/iron',160),('mounting_plate','c:plates/iron',120)]):
 put('data/'+NS+'/workshop/recipes/'+name+'.json',{'mode':mode,'input_tag':tag,'output':NS+':'+name,'ticks':ticks,'energy_per_tick':32})
put('data/'+NS+'/commissioning/precision_workbench.json',{'target':NS+':precision_workbench','foundation_tag':NS+':heavy_foundations','foundation_radius':1,'calibration_item':NS+':calibration_kit'})
for name,load in [('precision_workbench',24),('electric_motor',12),('rotation_dynamo',12)]:
 p={'target':NS+':'+name,'industrial_load':load,'emissions':{'heat':.4,'noise':.5}}
 if name=='precision_workbench':p['active_property']='active'
 else:p['emissions']={} # Conversion adds load; heat/noise accounting remains explicit workshop activity.
 put('data/'+NS+'/environment/emissions/'+name+'.json',p)
put('data/'+NS+'/loot_table/entities/industrial_raider.json',{'type':'minecraft:entity','pools':[]})
p=ROOT/'assets'/NS/'lang/en_us.json';lang=json.loads(p.read_text());lang.update({'block.'+NS+'.'+k:v for k,v in {'precision_workbench':'Precision Workbench','electric_motor':'Electrical Motor','rotation_dynamo':'Rotational Dynamo'}.items()});lang.update({'item.'+NS+'.'+k:v for k,v in {'precision_shaft':'Precision Shaft','gear_blank':'Machined Gear Blank','mounting_plate':'Perforated Mounting Plate','cutting_insert':'Diamond-tipped Cutting Insert'}.items()});lang['entity.'+NS+'.industrial_raider']='Rivet Scavenger';p.write_text(json.dumps(lang,indent=2)+'\n')
print('Generated precision workshop, converter, machining and acquisition resources')
