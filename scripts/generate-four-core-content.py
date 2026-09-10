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

shaped('civitas_industria:precision_component','civitas_industria:precision_component',[' I ','CRC',' I '],{'I':'#c:plates/iron','C':'#c:ingots/copper','R':'minecraft:redstone'},4)
shaped('civitas_industria:factory_controller','civitas_industria:factory_controller',['SCS','MFM','HEH'],{'S':'#c:plates/steel','C':'civitas_industria:precision_component','M':'create:precision_mechanism','F':'adpother:gold_filter_frame','H':'immersiveengineering:heavy_engineering','E':'immersiveengineering:rs_engineering'})
shaped('adpother:iron_filter_frame','adpother:iron_filter_frame',['PGP','G G','PAP'],{'P':'#c:plates/iron','G':'#c:glass_panes','A':'create:andesite_alloy'})
shaped('adpother:gold_filter_frame','adpother:gold_filter_frame',['PGP','SFS','PCP'],{'P':'#c:plates/gold','G':'#c:glass_panes','S':'#c:plates/steel','F':'adpother:iron_filter_frame','C':'civitas_industria:precision_component'})
shaped('adpother:diamond_filter_frame','adpother:diamond_filter_frame',['DGD','SFS','DMD'],{'D':'#c:gems/diamond','G':'#c:glass_panes','S':'#c:plates/steel','F':'adpother:gold_filter_frame','M':'create:precision_mechanism'})
shaped('adpother:aerometer','adpother:aerometer',[' G ','PCP',' R '],{'G':'#c:glass_panes','P':'#c:plates/copper','C':'minecraft:compass','R':'minecraft:redstone'})
shaped('adchimneys:metal_chimney','adchimneys:metal_chimney',['P P','P P','P P'],{'P':'#c:plates/iron'},8)
shaped('adchimneys:metal_vent','adchimneys:metal_vent',['PCP',' B ','PCP'],{'P':'#c:plates/iron','C':'adchimneys:metal_chimney','B':'minecraft:iron_bars'},4)
shaped('adchimneys:metal_pump','adchimneys:metal_pump',['SVS','CMC','SPS'],{'S':'#c:plates/steel','V':'adchimneys:metal_vent','C':'immersiveengineering:component_iron','M':'create:mechanical_pump','P':'civitas_industria:precision_component'},2)
shaped('adchimneys:duct','adchimneys:duct',['PPP','   ','PPP'],{'P':'#c:plates/iron'},8)
shaped('adchimneys:pipe','adchimneys:pipe',['P P','P P','P P'],{'P':'#c:plates/copper'},12)
shaped('civitas_industria:remediation_station','civitas_industria:remediation_station',['SFS','PBP','SCS'],{'S':'#c:plates/steel','F':'adpother:gold_filter_frame','P':'create:fluid_pipe','B':'minecraft:bucket','C':'civitas_industria:precision_component'})
shaped('civitas_industria:freight_terminal','civitas_industria:freight_terminal',['PCP','BMB','SRS'],{'P':'#c:plates/iron','C':'civitas_industria:precision_component','B':'minecraft:barrel','M':'immersiveengineering:component_iron','S':'#c:ingots/steel','R':'create:railway_casing'})
processing('steel_sheet','pressing',['#c:ingots/steel'],'immersiveengineering:plate_steel')
processing('aluminum_sheet','pressing',['#c:ingots/aluminum'],'immersiveengineering:plate_aluminum')
processing('hemp_filter_paper','pressing',['immersiveengineering:hemp_fiber'],'minecraft:paper',2)
processing('sulfur_reagent','mixing',['immersiveengineering:slag','create:limestone','minecraft:bone_meal'],'civitas_industria:remediation_reagent',6,heat_requirement='heated')
processing('standard_components','mixing',['#c:plates/iron','#c:plates/copper','minecraft:redstone'],'civitas_industria:precision_component',6)
processing('slag_gravel','crushing',['immersiveengineering:slag'],'minecraft:gravel',1,processing_time=100)
processing('sulfur_fertilizer','mixing',['immersiveengineering:dust_sulfur','minecraft:bone_meal','minecraft:bone_meal'],'immersiveengineering:fertilizer',4)
# Either mod's metal press yields canonical Create sheets, accepted throughout the pack.
for metal, output in [('iron','create:iron_sheet'),('copper','create:copper_sheet'),('gold','create:golden_sheet'),('brass','create:brass_sheet')]:
    write('immersiveengineering:metalpress/plate_'+metal,{'type':'immersiveengineering:metal_press','energy':2400,'input':{'tag':'c:ingots/'+metal},'mold':'immersiveengineering:mold_plate','result':{'id':output}})
processing('electrum','mixing',['#c:ingots/gold','#c:ingots/silver'],'immersiveengineering:ingot_electrum',2,heat_requirement='heated')
processing('constantan','mixing',['#c:ingots/copper','#c:ingots/nickel'],'immersiveengineering:ingot_constantan',2,heat_requirement='heated')
for pollutant, materials in {
    'carbon':['#minecraft:leaves, 8, minecraft:black_dye','minecraft:charcoal, 16, minecraft:black_dye','immersiveengineering:hemp_fabric, 32, minecraft:black_dye'],
    'sulfur':['#minecraft:wool, 8, minecraft:yellow_dye','civitas_industria:remediation_reagent, 32, immersiveengineering:dust_sulfur'],
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
 ('filter','workshop','adpother:iron_filter_frame','Capture before you vent','Install leaves for carbon, wool or reagent for sulfur, and paper for dust.',['adpother:iron_filter_frame','adchimneys:metal_chimney']),
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
