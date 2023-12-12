// priority: 1000

global.essencesCounter = {
  cube1: {
    produced: 0,
    processed: 0
  },
  cube2: {
    produced: 0,
    processed: 0
  },
  cube3: {
    produced: 0,
    processed: 0
  },
  cube4: {
    produced: 0,
    processed: 0
  }
}

/**
 * Creates an item and moves it into the inventory above.
 *
 * Uses the provided itemId argument to generate an ItemStack; if the target
 * inventory's id equals the provided targetInvId argument and the target
 * inventory has free room in any slot, move the generated ItemStack
 * to the target inventory; if there's no room the item is popped from the
 * inventory upper face; in any case increments the produced item counter.
 *
 * @param {Internal.BlockEntity} entity      - instance of the ticking blockEntity
 * @param {string}               itemId      - id of the created ItemStack
 * @param {number}               count       - number of created items
 * @param {string}               targetInvId - id of the target inventory 
*/
global.produceEssence = (entity, itemId, count, targetInvId) => {
  const { level, blockPos } = entity

  let container = level.getBlock(blockPos.above())
  let inventory = container.getInventory()
  let item = Item.of(itemId, count)
  if (container.id == targetInvId) {
    if (isThereRoom(itemId, container)) {
      inventory.insertItem(item, false)
    } else {
      container.popItemFromFace(item, 'up')
    }
    incrementEssenceCounter(itemId.split(":")[1], 'produced', count)
  }
}

/**
 * Removes an item from the inventory below and creates another item in the
 * inventory above.
 *
 * Checks if the source container and the target container ids equal the
 * sourceContainerId and targetContainerId arguments; if the inputId argument
 * equals the ItemStack.id of the first slot of the source container and the
 * target inventory has free room in any slot, move an ItemStack generated from
 * the provided outputId argument to the target inventory; in any case
 * increments the processed item counter.
 *
 * @param {Internal.BlockEntity} entity
 * @param {String} inputId
 * @param {String} outputId
 * @param {String} sourceContainerId
 * @param {String} targetContainerId
*/
global.processEssence = (entity, inputId, outputId, sourceContainerId, targetContainerId) => {
  const { level, blockPos } = entity

  let sourceContainer = level.getBlock(blockPos.below())
  let targetContainer = level.getBlock(blockPos.above())

  if (sourceContainer.id == sourceContainerId && targetContainer.id == targetContainerId) {
    if (inputId == sourceContainer.inventory.getStackInSlot(0).id) {
      if (isThereRoom(outputId, targetContainer)) {
        sourceContainer.inventory.extractItem(0, 1, false)
        targetContainer.inventory.insertItem(outputId, false)
        incrementEssenceCounter(inputId.split(":")[1], 'processed', 1)
      }
    }
  }
}

/**
 * Checks if the inventory has free slots for a specific item.
 *
 * @param {String} itemId
 * @param {Internal.BlockContainerJS} container
 * @returns {Boolean}
*/
let isThereRoom = (itemId, container) => {
  const inventory = container.inventory
  const maxCap = inventory.getSlots() * inventory.getSlotLimit(0)
  return inventory.count(itemId) < maxCap
}

/**
 * Increments the global.essencesCounter produced/processed value
 * @param {String} itemId
 * @param {String} countType
 * @param {Number} count
*/
let incrementEssenceCounter = (itemId, countType, count) => {
  global.essencesCounter[itemId][countType] += count
}

StartupEvents.registry('item', event => {
  event.create('cube1').displayName('Basic Mechanical Essence')
  event.create('cube2').displayName('Regular Mechanical Essence')
  event.create('cube3').displayName('Improved Mechanical Essence')
  event.create('cube4').displayName('Advanced Mechanical Essence')
  event.create('cube4_inert').displayName('Inert Advanced Mechanical Essence')
})

StartupEvents.registry('block', (event) => {
  event.create('essence_producer_4').soundType('netherite_block').blockEntity(blockEntityInfo => {
    blockEntityInfo.serverTick(20, 0, entity => {
      global.produceEssence(entity, 'kubejs:cube4', 1, 'minecraft:chest')
    })
  })

  event.create('essence_processor_4').soundType('netherite_block').blockEntity(blockEntityInfo => {
    blockEntityInfo.serverTick(20, 0, entity => {
      global.processEssence(entity, 'kubejs:cube4', 'kubejs:cube4_inert', 'minecraft:chest', 'minecraft:chest')
    })
  })
})
