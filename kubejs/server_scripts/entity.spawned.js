const itemMobList = [
	{item: 'kubejs:blaze_effigy', mobName: 'minecraft:blaze'},
	{item: 'kubejs:blizz_effigy', mobName: 'thermal:blizz'},
	{item: 'kubejs:blitz_effigy', mobName: 'thermal:blitz'},
	{item: 'kubejs:basalz_effigy', mobName: 'thermal:basalz'}
]

itemMobList.forEach((itemMob) => {
	let item = itemMob.item
	let mobName = itemMob.mobName
	
	ItemEvents.rightClicked(item, event => {
		if (!event.server){
			return
		}

		const player = event.player
		const hit = player.rayTrace(8)
		const x = hit.hitX
		const y = hit.hitY + 1
		const z = hit.hitZ

		let mob = event.level.createEntity(mobName)
		mob.x = x
		mob.y = y
		mob.z = z
		mob.spawn()
		event.item.count--
	})
})