ServerEvents.recipes(event => {
    event.remove({ type: 'thermal:lapidary_fuel' })
    var data = {
        recipes: [
            { input: 'kubejs:cube1', energy: 150000 },
			{ input: 'kubejs:cube2', energy: 600000 },
			{ input: 'kubejs:cube3', energy: 2400000 }
        ]
    }
    data.recipes.forEach((recipe) => {
        event.recipes.thermal.lapidary_fuel(recipe.input).energy(recipe.energy);
    })
})