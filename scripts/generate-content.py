#!/usr/bin/env python3
"""Deterministic vanilla-texture models, acquisition recipes, tags and block loot."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources'
NS='civitas_industria'
def put(path,data):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
blocks={
 'remediation_station':'moss_block','civic_core':'gold_block','civic_relay':'copper_block','logistics_node':'iron_block','defense_node':'obsidian','maintenance_depot':'anvil',
 'cargo_crate':'barrel_side','pallet':'oak_planks','warehouse_controller':'lodestone_side','warehouse_port':'hopper_outside','warehouse_casing':'iron_block',
 'cargo_loader':'piston_side','cargo_unloader':'piston_side','freight_terminal':'smithing_table_side','bulk_tank':'cauldron_side','factory_controller':'blast_furnace_front',
 'decorative_gear':'copper_block','decorative_fan':'iron_block','decorative_pump':'copper_block','decorative_gauge':'quartz_block_side','decorative_piston':'piston_side','decorative_vent':'iron_trapdoor'}
lang={}
for name,texture in blocks.items():
 texture='iron_block' if texture=='anvil' else texture
 model=f'{NS}:block/{name}'
 variants={'':{'model':model}}
 if name in ('cargo_loader','cargo_unloader','freight_terminal'):
  variants={f'facing={d}':{'model':model,'y':rot} for d,rot in [('north',0),('east',90),('south',180),('west',270)]}
 if name=='factory_controller':variants={f'active={v}':{'model':model} for v in ('true','false')}
 put(Path(f'assets/{NS}/blockstates/{name}.json'),{'variants':variants})
 put(Path(f'assets/{NS}/models/block/{name}.json'),{'parent':'minecraft:block/cube_all','textures':{'all':f'minecraft:block/{texture}'}})
 put(Path(f'assets/{NS}/models/item/{name}.json'),{'parent':model})
 put(Path(f'data/{NS}/loot_table/blocks/{name}.json'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{NS}:{name}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 if name.startswith('decorative_'):
  put(Path(f'assets/{NS}/models/block/{name}.json'),{'textures':{'base':'minecraft:block/iron_block','particle':'minecraft:block/iron_block'},'elements':[{'from':[0,0,0],'to':[16,3,16],'faces':{d:{'texture':'#base'} for d in ['up','down','north','south','east','west']}}]})
 lang[f'block.{NS}.{name}']=name.replace('_',' ').title()
for name,texture in {'precision_component':'iron_nugget','calibration_kit':'clock','remediation_reagent':'sugar'}.items():
 put(Path(f'assets/{NS}/models/item/{name}.json'),{'parent':'minecraft:item/generated','textures':{'layer0':f'minecraft:item/{texture}'}})
 lang[f'item.{NS}.{name}']=name.replace('_',' ').title()
lang[f'entity.{NS}.industrial_raider']='Industrial Raider'
put(Path(f'assets/{NS}/lang/en_us.json'),lang)
def recipe(name,ingredients,count=1):
 put(Path(f'data/{NS}/recipe/{name}.json'),{'type':'minecraft:crafting_shapeless','ingredients':[{'item':i if ':' in i else 'minecraft:'+i} for i in ingredients],'result':{'id':f'{NS}:{name}','count':count}})
recipe('precision_component',['iron_ingot','iron_ingot','copper_ingot','redstone'],4)
recipe('calibration_kit',[f'{NS}:precision_component','clock','amethyst_shard'])
recipe('remediation_reagent',['bone_meal','charcoal','sand'],4)
for name in blocks:
 if name.startswith('decorative_'):recipe(name,['iron_ingot','copper_ingot'],2)
 elif name in ('pallet','cargo_crate'):recipe(name,['barrel','iron_ingot'] if name=='cargo_crate' else ['oak_planks','oak_planks','iron_nugget'])
 elif name=='warehouse_casing':recipe(name,['iron_ingot','iron_ingot','stone'],4)
 else:
  base={'remediation_station':'moss_block','civic_core':'gold_block','defense_node':'obsidian','bulk_tank':'cauldron','factory_controller':'blast_furnace','warehouse_controller':'chest','warehouse_port':'hopper','cargo_loader':'hopper','cargo_unloader':'hopper'}.get(name,'iron_ingot')
  recipe(name,[base,f'{NS}:precision_component','redstone'])
put(Path('data/minecraft/tags/block/mineable/pickaxe.json'),{'replace':False,'values':[f'{NS}:{n}' for n in blocks]})
put(Path(f'data/{NS}/tags/worldgen/biome/high_ecological_recovery.json'),{'replace':False,'values':['minecraft:forest','minecraft:birch_forest','minecraft:jungle','minecraft:swamp','minecraft:mangrove_swamp']})
put(Path(f'data/{NS}/tags/worldgen/biome/pollution_resistant.json'),{'replace':False,'values':['minecraft:desert','minecraft:badlands','minecraft:basalt_deltas']})
put(Path(f'data/{NS}/tags/block/acid_sensitive.json'),{'replace':False,'values':['minecraft:iron_bars','minecraft:chain','minecraft:exposed_copper','minecraft:weathered_copper']})
put(Path(f'data/{NS}/tags/block/acid_resistant.json'),{'replace':False,'values':['minecraft:glass','minecraft:obsidian','minecraft:waxed_copper_block']})
for namespace,profiles in {'create':{'crushing_wheel':(12,'PM',3),'mechanical_press':(8,'NOISE',3),'mechanical_mixer':(8,'NOISE',2),'millstone':(5,'PM',1)},'immersiveengineering':{'coke_oven':(20,'SOX',8),'crusher':(25,'PM',10),'arc_furnace':(50,'NOX',12),'diesel_generator':(40,'NOX',15)}}.items():
 for name,(load,pollutant,amount) in profiles.items():
  put(Path(f'data/{NS}/environment/emissions/{namespace}_{name}.json'),{'target':f'{namespace}:{name}','industrial_load':load,'active_property':'','emissions':{pollutant:amount}})

put(Path(f'data/{NS}/tags/block/utility.json'),{'replace':False,'values':[{'id':'create:fluid_pipe','required':False},{'id':'create:shaft','required':False}]})
