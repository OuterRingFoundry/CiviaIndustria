// Registration-time progression only. No player/server tick handlers.
ServerEvents.recipes(event => {
  event.remove({id: 'civitas_industria:precision_component'})
  event.shaped('4x civitas_industria:precision_component', [' I ', 'CRC', ' I '], {
    I: 'create:iron_sheet', C: 'minecraft:copper_ingot', R: 'minecraft:redstone'
  }).id('civitas_industria:precision_component')
  event.remove({id: 'civitas_industria:factory_controller'})
  event.shaped('civitas_industria:factory_controller', ['SCS', 'FBF', 'SCS'], {
    S: '#c:ingots/steel', C: 'civitas_industria:precision_component',
    F: 'create:precision_mechanism', B: 'minecraft:blast_furnace'
  }).id('civitas_industria:factory_controller')
})
ServerEvents.tags('item', event => {
  event.add('civitas_industria:bulk_cargo', ['#c:ingots', '#c:raw_materials', '#c:ores'])
  event.add('civitas_industria:heavy_cargo', ['#c:storage_blocks'])
})
