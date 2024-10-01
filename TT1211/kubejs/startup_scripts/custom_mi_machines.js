let SIMPLE_QUARRY;
let ORE_PROCESSOR;

MIMachineEvents.registerRecipeTypes(event => {
  SIMPLE_QUARRY = event.register("simple_quarry")
    .withItemInputs() // enable item inputs
    .withItemOutputs(); // enable item outputs

  ORE_PROCESSOR = event.register("ore_processor")
    .withItemInputs()
    .withItemOutputs()
    .withFluidInputs()
    .withFluidOutputs()
});

MIMachineEvents.registerMachines(event => {
  event.craftingSingleBlock(
      /* GENERAL PARAMETERS FIRST */
      // English name, internal name, recipe type (see above), list of tiers (can be bronze/steel/electric)
      "Simple Quarry", "simple_quarry", SIMPLE_QUARRY, ["bronze", "steel", "electric"],
      /* GUI CONFIGURATION */
      // Background height (or -1 for default value), progress bar, efficiency bar, energy bar
      -1, event.progressBar(77, 34, "extract"), event.efficiencyBar(46, 62), event.energyBar(18, 30),
      /* SLOT CONFIGURATION */
      // Number of slots: item inputs, item outputs, fluid inputs, fluid outputs
      1, 2, 0, 0,
      // Capacity for fluid slots (unused here)
      16,
      // Slot positions: items and fluids.
      // Explanation: 1x1 grid of item slots starting at position (56, 35), then 2x1 grid of item slots starting at position (102, 35).
      items => items.addSlots(56, 35, 1, 1).addSlots(102, 35, 2, 1), fluids => {},
      /* MODEL CONFIGURATION */
      // front overlay?, top overlay?, side overlay?
      true, false, false,
      config => {}
  );

  const oreProcessorHatch = event.hatchOf('item_input', 'item_output', 'fluid_input', 'fluid_output', 'energy_input');
  const oreProcessorCasing = event.memberOfBlock('modern_industrialization:quantum_machine_hull');
  const oreProcessorCoil = event.memberOfBlock('modern_industrialization:superconductor_coil');
  const oreProcessorShape = event.layeredShape('superconductor', [
    ['HHHHH', 'HHHHH', 'HHHHH'],
    ['CCCCC', 'C   C', 'CCCCC'],
    ['CCCCC', 'C   C', 'CCCCC'],
    ['CCCCC', 'C   C', 'CCCCC'],
    ['HHHHH', 'HH#HH', 'HHHHH']
  ])
    .key('H', oreProcessorCasing, oreProcessorHatch)
    .key('C', oreProcessorCoil, event.noHatch())
    .build();

  event.simpleElectricCraftingMultiBlock(
    /* GENERAL PARAMETERS */
    // English name, internal name, recipe type, multiblock shape
    "Ore Processor", "ore_processor", ORE_PROCESSOR, oreProcessorShape,
    /* DISPLAY CONFIGURATION */
    // progress bar
    event.progressBar(77, 34, "arrow"),
    // item inputs, item outputs, fluid inputs, fluid outputs
    itemInputs => itemInputs.addSlots(56, 35, 1, 2), itemOutputs => itemOutputs.addSlot(102, 35),
    fluidInputs => fluidInputs.addSlot(36, 35), fluidOutputs => fluidOutputs.addSlot(122, 35),
    /* MODEL CONFIGURATION */
    // casing of the controller, overlay folder, front overlay?, top overlay?, side overlay?
    "superconductor", "ore_processor", true, false, false
  );
});