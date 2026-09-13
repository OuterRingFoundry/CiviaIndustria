#!/usr/bin/env python3
"""Generate the four-core progression and native filtration configuration."""
import json
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / 'src/main/resources/data'
CONDITIONS = [{'type': 'neoforge:mod_loaded', 'modid': m} for m in ['create', 'immersiveengineering', 'adpother', 'adchimneys']]

def ingredient(value):
    return {'tag': value[1:]} if value.startswith('#') else {'item': value}

def write(id, recipe):
    namespace, name = id.split(':')
    path = DATA / namespace / 'recipe' / (name + '.json')
    path.parent.mkdir(parents=True, exist_ok=True)
    # Existing standalone Civitas recipes remain available when the four-core pack is absent.
    if namespace == 'civitas_industria' and '/' not in name:
        path = ROOT / 'pack/overrides/kubejs/data' / namespace / 'recipe' / (name + '.json')
        path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps({'neoforge:conditions': CONDITIONS, **recipe}, indent=2) + '\n')

def shaped(id, output, pattern, keys, count=1):
    write(id, {'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:ingredient(v) for k,v in keys.items()},'result':{'id':output,'count':count}})

def processing(name, type, inputs, output, count=1, **extra):
    write('civitas_industria:integration/'+name, {'type':'create:'+type,'ingredients':[ingredient(i) for i in inputs],'results':[{'id':output,'count':count}], **extra})

shaped('civitas_industria:precision_component','civitas_industria:precision_component',[' W ','NGN',' P '],{'P':'#c:plates/iron','W':'immersiveengineering:wire_copper','G':'create:cogwheel','N':'#c:nuggets/iron'})
shaped('civitas_industria:factory_controller','civitas_industria:factory_controller',['SCS','MFM','HEH'],{'S':'#c:plates/steel','C':'civitas_industria:precision_component','M':'create:precision_mechanism','F':'adpother:gold_filter_frame','H':'immersiveengineering:heavy_engineering','E':'immersiveengineering:rs_engineering'})
shaped('adpother:iron_filter_frame','adpother:iron_filter_frame',['PGP','G G','PAP'],{'P':'#c:plates/iron','G':'#c:glass_panes','A':'create:andesite_alloy'})
shaped('adpother:gold_filter_frame','adpother:gold_filter_frame',['PGP','SFS','PCP'],{'P':'#c:plates/gold','G':'#c:glass_panes','S':'#c:plates/steel','F':'adpother:iron_filter_frame','C':'civitas_industria:precision_component'})
shaped('adpother:diamond_filter_frame','adpother:diamond_filter_frame',['DGD','SFS','DMD'],{'D':'#c:gems/diamond','G':'#c:glass_panes','S':'#c:plates/steel','F':'adpother:gold_filter_frame','M':'create:precision_mechanism'})
shaped('adpother:aerometer','adpother:aerometer',[' G ','PCP',' R '],{'G':'#c:glass_panes','P':'#c:plates/copper','C':'minecraft:compass','R':'minecraft:redstone'})
shaped('adchimneys:metal_chimney','adchimneys:metal_chimney',['P P','P P','P P'],{'P':'#c:plates/iron'},8)
shaped('adchimneys:metal_vent','adchimneys:metal_vent',['PCP',' B ','PCP'],{'P':'#c:plates/iron','C':'adchimneys:metal_chimney','B':'minecraft:iron_bars'},4)
shaped('adchimneys:metal_pump','adchimneys:metal_pump',['SVS','CMC','SPS'],{'S':'#c:plates/steel','V':'adchimneys:metal_vent','C':'immersiveengineering:component_iron','M':'create:mechanical_pump','P':'civitas_industria:precision_component'},1)
shaped('adchimneys:duct','adchimneys:duct',['PPP','   ','PPP'],{'P':'#c:plates/iron'},8)
shaped('adchimneys:pipe','adchimneys:pipe',['P P','P P','P P'],{'P':'#c:plates/copper'},12)
shaped('civitas_industria:remediation_station','civitas_industria:remediation_station',['SFS','PBP','SCS'],{'S':'#c:plates/steel','F':'adpother:gold_filter_frame','P':'create:fluid_pipe','B':'minecraft:bucket','C':'civitas_industria:precision_component'})
shaped('civitas_industria:freight_terminal','civitas_industria:freight_terminal',['PCP','BMB','SRS'],{'P':'#c:plates/iron','C':'civitas_industria:precision_component','B':'minecraft:barrel','M':'immersiveengineering:component_iron','S':'#c:ingots/steel','R':'create:railway_casing'})
processing('steel_sheet','pressing',['#c:ingots/steel'],'immersiveengineering:plate_steel')
processing('aluminum_sheet','pressing',['#c:ingots/aluminum'],'immersiveengineering:plate_aluminum')
processing('hemp_filter_paper','pressing',['immersiveengineering:hemp_fiber'],'minecraft:paper',2)
processing('sulfur_reagent','compacting',['civitas_industria:hydrated_lime','civitas_industria:hydrated_lime','immersiveengineering:hemp_fiber'],'civitas_industria:remediation_reagent',2)
# Mount the gear and copper winding, add two fasteners, then press the assembly closed.
transitional='civitas_industria:incomplete_precision_component'
sequence=[]
for part in ['create:cogwheel','immersiveengineering:wire_copper','#c:nuggets/iron','#c:nuggets/iron']:
    sequence.append({'type':'create:deploying','ingredients':[ingredient(transitional),ingredient(part)],'results':[{'id':transitional}]})
sequence.append({'type':'create:pressing','ingredients':[ingredient(transitional)],'results':[{'id':transitional}]})
write('civitas_industria:integration/standard_components',{'type':'create:sequenced_assembly','ingredient':ingredient('#c:plates/iron'),'transitional_item':{'id':transitional},'loops':1,'sequence':sequence,'results':[{'id':'civitas_industria:precision_component'}]})
processing('slag_gravel','crushing',['immersiveengineering:slag'],'minecraft:gravel',1,processing_time=100)
# Captured SOx is bound sulfate, not elemental sulfur or automatically food-safe fertilizer.
(DATA/'civitas_industria/recipe/integration/sulfur_fertilizer.json').unlink(missing_ok=True)
processing('limestone_dust','milling',['#civitas_industria:carbonate_rocks'],'civitas_industria:limestone_dust',1,processing_time=100)
processing('quicklime','mixing',['civitas_industria:limestone_dust'],'civitas_industria:quicklime',1,heat_requirement='heated')
write('civitas_industria:integration/quicklime_firing',{'type':'minecraft:smelting','ingredient':ingredient('civitas_industria:limestone_dust'),'result':{'id':'civitas_industria:quicklime'},'experience':.1,'cookingtime':200})
water=lambda amount:{'type':'neoforge:single','fluid':'minecraft:water','amount':amount}
write('civitas_industria:integration/lime_slaking',{'type':'create:mixing','ingredients':[ingredient('civitas_industria:quicklime'),water(250)],'results':[{'id':'civitas_industria:hydrated_lime'}]})
write('civitas_industria:integration/manual_slaking',{'type':'minecraft:crafting_shapeless','ingredients':[ingredient('civitas_industria:quicklime')]*4+[ingredient('minecraft:water_bucket')],'result':{'id':'civitas_industria:hydrated_lime','count':4}})
write('civitas_industria:remediation_reagent',{'type':'minecraft:crafting_shapeless','ingredients':[ingredient('civitas_industria:hydrated_lime')]*2+[ingredient('immersiveengineering:hemp_fiber')],'result':{'id':'civitas_industria:remediation_reagent','count':2}})
write('civitas_industria:integration/gypsum_binder',{'type':'minecraft:smelting','ingredient':ingredient('civitas_industria:sulfate_filter_cake'),'result':{'id':'civitas_industria:gypsum_binder'},'experience':.1,'cookingtime':200})
write('civitas_industria:integration/gypsum_panels',{'type':'create:compacting','ingredients':[ingredient('civitas_industria:gypsum_binder')]*4+[ingredient('minecraft:paper')]*2+[water(250)],'results':[{'id':'civitas_industria:gypsum_panel','count':4}]})
p=DATA/'civitas_industria/tags/item/carbonate_rocks.json';p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps({'values':[{'id':'create:limestone','required':False},'minecraft:calcite']},indent=2)+'\n')
# Either mod's metal press yields canonical Create sheets, accepted throughout the pack.
for metal, output in [('iron','create:iron_sheet'),('copper','create:copper_sheet'),('gold','create:golden_sheet'),('brass','create:brass_sheet')]:
    write('immersiveengineering:metalpress/plate_'+metal,{'type':'immersiveengineering:metal_press','energy':2400,'input':{'tag':'c:ingots/'+metal},'mold':'immersiveengineering:mold_plate','result':{'id':output}})
processing('electrum','mixing',['#c:ingots/gold','#c:ingots/silver'],'immersiveengineering:ingot_electrum',2,heat_requirement='heated')
processing('constantan','mixing',['#c:ingots/copper','#c:ingots/nickel'],'immersiveengineering:ingot_constantan',2,heat_requirement='heated')
for pollutant, materials in {
    'carbon':['#minecraft:leaves, 8, minecraft:black_dye','minecraft:charcoal, 16, minecraft:black_dye','immersiveengineering:hemp_fabric, 32, minecraft:black_dye'],
    'sulfur':['civitas_industria:hydrated_lime, 16, civitas_industria:sulfate_filter_cake','civitas_industria:remediation_reagent, 32, civitas_industria:sulfate_filter_cake'],
    'dust':['minecraft:paper, 8, minecraft:gray_dye','immersiveengineering:hemp_fabric, 32, minecraft:gray_dye'],
}.items():
    p=ROOT/'pack/overrides/config/adpother/Pollutants'/(pollutant+'.cfg');p.parent.mkdir(parents=True,exist_ok=True)
    # ForgeEndertech's per-unit configs use the legacy Forge Configuration format.
    if p.exists():
        continue  # Preserve the canonical config expanded by the pinned native mod.
    p.write_text('# Civitas four-core pack: actual airborne stock feeds regional exposure.\n'+pollutant+' {\n    S:chunkPollutionInfluence=ALWAYS\n    S:filterMaterials <\n'+''.join('        '+m+'\n' for m in materials)+'     >\n}\n')
print('Generated four-core recipes and native filtration configs')

stages = [
 ('workshop',None,'create:andesite_alloy','The first workshop','Build mechanical tools, then steelworks. Route smoke out of enclosed rooms.',['create:andesite_alloy']),
 ('filter','workshop','adpother:iron_filter_frame','Capture before you vent','Install carbon media, lime sorbent for sulfur, and paper or hemp for dust.',['adpother:iron_filter_frame','adchimneys:metal_chimney']),
 ('service','filter','civitas_industria:remediation_reagent','Keep the filters working','Automate filter supplies and remove byproducts. Exhaust changes the surrounding land over time.',['civitas_industria:remediation_reagent','immersiveengineering:hemp_fabric']),
 ('factory','service','civitas_industria:factory_controller','A commissioned factory','Build a foundation, calibrate the controller and connect a serviced exhaust route.',['civitas_industria:factory_controller']),
 ('freight','factory','civitas_industria:freight_terminal','Industry needs a railway','Move bulk materials by physical freight into your city warehouses.',['civitas_industria:freight_terminal']),
]
for name,parent,icon,title,description,items in stages:
    display={'icon':{'id':icon},'title':title,'description':description,'frame':'task','show_toast':True,'announce_to_chat':False}
    if parent is None:display['background']='minecraft:textures/block/stone_bricks.png'
    advancement={'neoforge:conditions':CONDITIONS,'display':display,'criteria':{
        'acquire_'+str(i):{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':item}]}}
        for i,item in enumerate(items)}}
    if parent:advancement['parent']='civitas_industria:industry/'+parent
    path=DATA/'civitas_industria/advancement/industry'/(name+'.json');path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(advancement,indent=2)+'\n')
