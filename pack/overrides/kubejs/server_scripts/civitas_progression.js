// Machine recipes live in the Civitas mod; its pack-only recipe overrides live in
// kubejs/data. All are ordinary 1.21.1 recipe JSON, shared by JEI and both machines.
ServerEvents.tags('item', event => {
  event.add('civitas_industria:bulk_cargo', ['#c:ingots', '#c:raw_materials', '#c:ores', '#c:plates', 'immersiveengineering:slag'])
  event.add('civitas_industria:heavy_cargo', ['#c:storage_blocks'])
})
