// A basin fed by funnels or chutes holds at most 16 of one item, so a basin recipe needing more can't be automated.
// Logs every such recipe on server start, and on demand with /kubejs custom_command basin_limit_check.
const BASIN_LIMIT = 16;
// Asked of the recipe manager by type, since some mods' recipes (Mechanical Extruder's) throw when asked their own.
const AllRecipeTypes = Java.loadClass('com.simibubi.create.AllRecipeTypes');
const BASIN_RECIPE_TYPES = {'create:mixing': AllRecipeTypes.MIXING, 'create:compacting': AllRecipeTypes.COMPACTING};

function checkBasinRecipes(server) {
	let found = 0;
	for (let type in BASIN_RECIPE_TYPES) {
		server.getRecipeManager().getAllRecipesFor(BASIN_RECIPE_TYPES[type].getType()).forEach(recipe => {
			let counts = {};
			recipe.getIngredients().forEach(ingredient => {
				let key = String(ingredient.toJson());
				counts[key] = (counts[key] || 0) + 1;
			});

			for (let key in counts) {
				if (counts[key] > BASIN_LIMIT) {
					found++;
					console.warn(`Basin limit: ${recipe.getId()} (${type}) needs ${counts[key]}x ${key}`);
				}
			}
		});
	}
	console.info(`Basin limit check: ${found} ingredient(s) over ${BASIN_LIMIT}`);
	return found;
}

ServerEvents.loaded(event => {
	checkBasinRecipes(event.server);
});

ServerEvents.customCommand('basin_limit_check', event => {
	let found = checkBasinRecipes(event.server);
	if (event.player) event.player.tell(`Basin limit check: ${found} ingredient(s) over ${BASIN_LIMIT}, see logs/kubejs/server.log`);
});
