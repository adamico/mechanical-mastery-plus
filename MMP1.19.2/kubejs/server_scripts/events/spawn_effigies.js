const mobList = [
	{effigy: 'kubejs:blaze_effigy', mobName: 'minecraft:blaze', drop: 'minecraft:blaze_rod'},
	{effigy: 'kubejs:blizz_effigy', mobName: 'thermal:blizz', drop: 'thermal:blizz_rod'},
	{effigy: 'kubejs:blitz_effigy', mobName: 'thermal:blitz', drop: 'thermal:blitz_rod'},
	{effigy: 'kubejs:basalz_effigy', mobName: 'thermal:basalz', drop: 'thermal:basalz_rod'}
]

let spawnMob = (mob, x, y, z) => {
	mob.x = x
	mob.y = y
	mob.z = z
	mob.spawn()
}

mobList.forEach(mob => {
	let effigy = mob.effigy
	let mobName = mob.mobName
	
	ItemEvents.rightClicked(effigy, event => {
		if (!event.server) return

		const player = event.player;
		const hit = player.rayTrace(8)
		const x = hit.hitX
		const y = hit.hitY + 1
		const z = hit.hitZ

		if (Number.isNaN(x) || Number.isNaN(y) || Number.isNaN(z) ) return
		let mob = event.level.createEntity(mobName)
		spawnMob(mob, x, y, z)
		event.item.count--
	})
})

ServerEvents.entityLootTables(event => {
  // Override zombie loot table that will drop 5 of either carrot (25% chance) or apple (75% chance)
  // event.addEntity('minecraft:zombie', table => {
    // table.addPool(pool => {
      // pool.rolls = 5
      // pool.addItem('minecraft:carrot', 1)
      // pool.addItem('minecraft:apple', 3)
    // })
  // })

	mobList.forEach(mob => {
		event.modifyEntity(mob.mobName, table => {
			table.addPool(pool => {
				pool.rolls = [3, 5]
				pool.addItem(Item.of(mob.drop))
			})
		})
	})
})