ServerEvents.recipes(event => {
  const recipes = [
    {
      input: { tag: 'minecraft:logs' },
      outputs: [
        { item: 'minecraft:charcoal' },
        { fluid: 'immersiveengineering:creosote', amount: 125 }
      ],
      experience: 0.15,
      energy: 2000,
      id: 'thermal:machine/pyrolyzer/pyrolyzer_logs'
    },
    {
      input: { item: 'minecraft:coal' },
      outputs: [
        { item: 'immersiveengineering:coal_coke' },
        { item: 'thermal:tar', chance: 0.25 },
        { fluid: 'immersiveengineering:creosote', amount: 250 }
      ],
      experience: 0.15,
      energy: 4000,
      id: 'thermal:machine/pyrolyzer/pyrolyzer_coal'
    }
  ];
  recipes.forEach((recipe) => {
    const re = event.custom({
      type: 'thermal:pyrolyzer',
      ingredient: recipe.input,
      result: recipe.outputs,
      experience: recipe.experience,
      energy: recipe.energy
    });
    if (recipe.id) {
      re.id(recipe.id);
    }
  });
});