ServerEvents.recipes(event => {
  event.replaceInput({id: 'integrateddynamics:crafting/mechanical_squeezer'}, 'minecraft:obsidian', 'kubejs:cube3');

  event.remove({id: 'integrateddynamics:mechanical_squeezer/convenience/minecraft_blaze_powder'});

  let blazeTypes = [
    {name: 'blaze', mod: 'minecraft', fluid: 'lava', fluidAmount: 250, solidOutputCount: 2},
    {name: 'blizz', mod: 'thermal', fluid: 'ender', fluidAmount: 250, solidOutputCount: 2},
    {name: 'basalz', mod: 'thermal', fluid: 'redstone', fluidAmount: 250, solidOutputCount: 2},
    {name: 'blitz', mod: 'thermal', fluid: 'glowstone', fluidAmount: 250, solidOutputCount: 2},
  ];

  blazeTypes.forEach(type => {
    let name = type.name;
    let mod = type.mod;
    let rod = name + '_rod';
    let input = mod + ':' + rod;
    let fluid = mod + ':' + type.fluid;
    let fluidAmount = type.fluidAmount;
    let outputFluid = {fluid: fluid, amount: fluidAmount};
    let powder = name + '_powder';
    let outputSolid = {item: {item: mod + ':' + powder, count: type.solidOutputCount}};
    let outputSolidMaybe = {item: mod + ':' + powder, chance: 0.5}; // 50% chance

    event.custom({ type: "integrateddynamics:mechanical_squeezer",
      item: input, result: {fluid: outputFluid, items: [outputSolid, outputSolidMaybe]}, duration: 80
    })
  });
});