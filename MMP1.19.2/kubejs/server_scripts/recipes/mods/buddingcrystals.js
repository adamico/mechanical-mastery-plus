// priority: 0

// credits to https://gist.githubusercontent.com/sarim/a03f0ee07c9b63cb87fb6cfe10e43f28/raw/510f06898df2ba9af5fdc4fb8d8b9c2aa68efc6c/squatting-grow.js

console.info('Squat growing addon server script loaded.')

const GROW_RANGE = 3;
const GROW_CHANCE = 0.5;
const GROW_FOOD_DECREASE_CHANCE = 0.01;
const GROW_BLOCKS_TAGS = [
  { tag: 'minecraft.crops', growAttempt: 200 },
  { tag: 'minecraft.saplings', growAttempt: 75 }
];
const GROW_BLOCKS_IDS = [
  { id: 'minecraft:cactus', growAttempt: 300 },
  { id: 'minecraft:sugar_cane', growAttempt: 300 },
  { id: 'minecraft:sweet_berry_bush', growAttempt: 75 },
  { id: 'minecraft:budding_amethyst', growAttempt: 50 },
  { id: 'buddingcrystals:budding_*', growAttempt: 50 },
];

let crouchingPlayers = [];

PlayerEvents.tick(event => {
  let isCrouching = event.player.crouching;
  let playerUuid = event.player.stringUuid;
  let index = crouchingPlayers.indexOf(playerUuid);
  
  if (isCrouching && index === -1) {
    // console.log('Player is crouching');
    crouchingPlayers.push(playerUuid);
    
    growBlocksAround(event.player);
  } else if (!isCrouching && index !== -1) {
    // console.log('Player is standing');
    crouchingPlayers.splice(index, 1);
  }
});

/**
* Check if a block ID matches the wildcard pattern
* @param {string} blockId - The block ID to check.
* @param {string} pattern - The pattern to match against, which may include wildcards.
* @return {boolean} - True if the block ID matches the pattern, otherwise false.
*/
function matchesPattern(blockId, pattern) {
  const regex = new RegExp("^" + pattern.split("*").join(".*") + "$");
  return regex.test(blockId);
}

/**
* @param {Internal.Player} player - The player around whom the blocks will grow.
*/
function growBlocksAround(player) {
  let level = player.level;
  let pos = player.blockPosition();
  
  // Skip growth attempts if food level is too low
  if (player.foodLevel <= 6) {
    player.sendSystemMessage("Too hungry for making grow effect");
    return; 
  }
  
  for (let x = -GROW_RANGE; x <= GROW_RANGE; x++) {
    for (let z = -GROW_RANGE; z <= GROW_RANGE; z++) {
      for (let y = -1; y <= 1; y++) {
        let blockPos = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
        let blockState = level.getBlockState(blockPos);
        let block = blockState.getBlock();
        let tags = blockState.tags.toArray();
        
        let growAttempts = 0;
        let matchFound = false;
        
        // Check against TAGS
        for (const entry of GROW_BLOCKS_TAGS) {
          if (tags.some(tag => tag.location().toLanguageKey() === entry.tag)) {
            growAttempts = entry.growAttempt;
            matchFound = true;
            break;
          }
        }
        
        // Check against IDS
        if (!matchFound) {
          for (const entry of GROW_BLOCKS_IDS) {
            if (matchesPattern(block.arch$registryName(), entry.id)) {
              growAttempts = entry.growAttempt;
              matchFound = true;
              break;
            }
          }
        }
        
        if (GROW_CHANCE > Math.random() && 
        !blockState.isAir() && matchFound) {
          console.log("growing " + block.arch$registryName());
          
          for (let i = 0; i < growAttempts; i++)
          block.randomTick(blockState, level, blockPos, level.random);
          
          if (Math.random() < GROW_FOOD_DECREASE_CHANCE) {
            player.setFoodLevel(player.foodLevel - 1);
          }
          
          level.sendParticles(player, "happy_villager", false, blockPos.x + 0.5, blockPos.y + 0.8, blockPos.z + 0.5, 2, 0, 0, 0.5, 0.5);
          level.playSound(null, blockPos, "item.bone_meal.use", "master", 0.1, 1.0);
        }
      }
    }
  }
}