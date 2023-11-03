ServerEvents.recipes(event => {

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
    "time": 3
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