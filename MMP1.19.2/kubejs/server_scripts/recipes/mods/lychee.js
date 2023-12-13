ServerEvents.tick(event => {
  // sPData Timer
  const { server } = event;
  if (!server) return;

  let sPData = server.persistentData;
  if (!sPData.Timer) sPData.Timer = 0;
  sPData.Timer++;
  if (sPData.Timer % 60 != 0) return;
  sPData.Timer = 0;

  server.allLevels.forEach(level => {
    if (level) {
      if (level.dimension.toString().includes("overworld")) {
        let entities = level.allEntities;
        entities.forEach(entity => {
          if (entity && entity.getItem() != null) {
            let itemEntity = entity.getItem();
            switch (itemEntity) {
              case Item.of("kubejs:hot_tiab"): {
                let pos = entity.blockPosition();
                let block = level.getBlockState(pos);
                let liquid = Block.getId('sliceanddice:fertilizer');
                if (block.is(liquid)) {
                  event.server.runCommandSilent(
                    `execute in ${level.dimension} run particle smoke ${pos.x} ${pos.y + 1} ${pos.z} 0.4 0.8 0.4 0.01 50`
                  );
                  entity.playSound('block.lava.extinguish');
                }
                break;
              }
              case Item.of("minecraft:cobblestone"): {
                let pos = entity.blockPosition();
                let block = level.getBlockState(pos);
                let water = Block.getId('minecraft:water');
                if (block.is(water)) {
                  event.server.runCommandSilent(
                    // particle <name> <pos> <delta> <speed> <count> [force|normal] [<viewers>]
                    `execute in ${level.dimension} run particle cloud ${pos.x} ${pos.y + 1} ${pos.z} 0 2 0 0.05 0`
                  );
                  entity.playSound('block.bubble_column.bubble_pop');
                }
                break;
              }
            }
          }
        });
      }
    }
  });
});

ServerEvents.recipes(event => {
  event.custom({
    "type": "lychee:dripstone_dripping",
    "source_block": "lava",
    "target_block": "supplementaries:hourglass",
    "post": [
      {
        "type": "place",
        "block": "*"
      },
      {
        "type": "drop_item",
        "item": "kubejs:hot_tiab"
      }
    ]
  });

  event.custom({
    "type": "lychee:item_inside",
    "post": [
      {
        "type": "drop_item",
        "item": "tiab:time_in_a_bottle"
      }
    ],
    "item_in": [
      {
        "item": "kubejs:hot_tiab"
      }
    ],
    "block_in": {
      "blocks": [
        "sliceanddice:fertilizer"
      ]
    },
    "time": 30
  });

  event.custom({
    "type": "lychee:item_inside",
    "post": [
      {
        "type": "drop_item",
        "item": "dripstone_block"
      }
    ],
    "item_in": [
      {
        "item": "minecraft:cobblestone"
      }
    ],
    "block_in": {
      "blocks": [
        "minecraft:water"
      ]
    },
    "time": 6
  });

  event.custom({
    "type": "lychee:block_interacting",
    "item_in": [
      {
        "tag": "forge:shovels"
      }
    ],
    "block_in": {
      "tag": "dirt"
    },
    "contextual": {
      "type": "is_sneaking",
      "description": "shift + right click"
    },
    "post": [
      {
        "type": "random",
        "rolls": {
          "min": 1,
          "max": 2
        },
        "entries": [
          {
            "weight": 1,
            "type": "drop_item",
            "item": "glowstone_dust"
          },
          {
            "weight": 4,
            "type": "drop_item",
            "item": "clay_ball",
          },
          {
            "weight": 4,
            "type": "drop_item",
            "item": "flint"
          },
          {
            "weight": 2,
            "type": "drop_item",
            "item": "bone"
          }
        ]
      },
      {
        "type": "damage_item"
      },
      {
        "type": "add_item_cooldown",
        "s": 1
      }
    ]
  });
});
