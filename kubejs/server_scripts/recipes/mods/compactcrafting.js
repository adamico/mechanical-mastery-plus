ServerEvents.recipes(event => {
  const machineShapes = {
    tiny_machine: [
      {
        type: 'compactcrafting:filled',
        component: 'W'
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "W", "W"],
          ["W", "C", "W"],
          ["W", "W", "W"]
        ]
      },
      {
        type: 'compactcrafting:filled',
        component: 'W'
      }
    ],
    small_machine: [
      {
        type: 'compactcrafting:filled',
        component: 'I'
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["I", "I", "I"],
          ["I", "T", "I"],
          ["I", "I", "I"]
        ]
      },
      {
        type: 'compactcrafting:filled',
        component: 'I'
      }
    ],
    normal_machine: [
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "W", "W", "W", "W"],
          ["W", "G", "G", "G", "W"],
          ["W", "G", "G", "G", "W"],
          ["W", "G", "G", "G", "W"],
          ["W", "W", "W", "W", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "G", "G", "G", "W"],
          ["G", "G", "G", "G", "G"],
          ["G", "G", "G", "G", "G"],
          ["G", "G", "G", "G", "G"],
          ["W", "G", "G", "G", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "G", "G", "G", "W"],
          ["G", "G", "G", "G", "G"],
          ["G", "G", "S", "G", "G"],
          ["G", "G", "G", "G", "G"],
          ["W", "G", "G", "G", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "G", "G", "G", "W"],
          ["G", "G", "G", "G", "G"],
          ["G", "G", "G", "G", "G"],
          ["G", "G", "G", "G", "G"],
          ["W", "G", "G", "G", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "W", "W", "W", "W"],
          ["W", "G", "G", "G", "W"],
          ["W", "G", "G", "G", "W"],
          ["W", "G", "G", "G", "W"],
          ["W", "W", "W", "W", "W"]
        ]
      }
    ],
    large_machine: [
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "W", "W", "W", "W"],
          ["W", "O", "O", "O", "W"],
          ["W", "O", "O", "O", "W"],
          ["W", "O", "O", "O", "W"],
          ["W", "W", "W", "W", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "O", "O", "O", "W"],
          ["O", "O", "O", "O", "O"],
          ["O", "O", "O", "O", "O"],
          ["O", "O", "O", "O", "O"],
          ["W", "O", "O", "O", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "O", "O", "O", "W"],
          ["O", "O", "O", "O", "O"],
          ["O", "O", "N", "O", "O"],
          ["O", "O", "O", "O", "O"],
          ["W", "O", "O", "O", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "O", "O", "O", "W"],
          ["O", "O", "O", "O", "O"],
          ["O", "O", "O", "O", "O"],
          ["O", "O", "O", "O", "O"],
          ["W", "O", "O", "O", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "W", "W", "W", "W"],
          ["W", "O", "O", "O", "W"],
          ["W", "O", "O", "O", "W"],
          ["W", "O", "O", "O", "W"],
          ["W", "W", "W", "W", "W"]
        ]
      }
    ],
    giant_machine: [
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "W", "W", "W", "W", "W", "W"],
          ["W", "D", "D", "D", "D", "D", "W"],
          ["W", "D", "W", "W", "W", "D", "W"],
          ["W", "D", "W", "D", "W", "D", "W"],
          ["W", "D", "W", "W", "W", "D", "W"],
          ["W", "D", "D", "D", "D", "D", "W"],
          ["W", "W", "W", "W", "W", "W", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "D", "D", "D", "D", "D", "W"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["W", "D", "D", "D", "D", "D", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "D", "W", "W", "W", "D", "W"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["W", "D", "D", "D", "D", "D", "W"],
          ["W", "D", "D", "D", "D", "D", "W"],
          ["W", "D", "D", "D", "D", "D", "W"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["W", "D", "W", "W", "W", "D", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "D", "W", "D", "W", "D", "W"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["W", "D", "D", "D", "D", "D", "W"],
          ["D", "D", "D", "L", "D", "D", "D"],
          ["W", "D", "D", "D", "D", "D", "W"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["W", "D", "W", "D", "W", "D", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "D", "W", "W", "W", "D", "W"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["W", "D", "D", "D", "D", "D", "W"],
          ["W", "D", "D", "D", "D", "D", "W"],
          ["W", "D", "D", "D", "D", "D", "W"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["W", "D", "W", "W", "W", "D", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "D", "D", "D", "D", "D", "W"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["D", "D", "D", "D", "D", "D", "D"],
          ["W", "D", "D", "D", "D", "D", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "W", "W", "W", "W", "W", "W"],
          ["W", "D", "D", "D", "D", "D", "W"],
          ["W", "D", "W", "W", "W", "D", "W"],
          ["W", "D", "W", "D", "W", "D", "W"],
          ["W", "D", "W", "W", "W", "D", "W"],
          ["W", "D", "D", "D", "D", "D", "W"],
          ["W", "W", "W", "W", "W", "W", "W"]
        ]
      }
    ],
    max_machine: [
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "W", "W", "W", "W", "W", "W"],
          ["W", "N", "N", "N", "N", "N", "W"],
          ["W", "N", "W", "W", "W", "N", "W"],
          ["W", "N", "W", "N", "W", "N", "W"],
          ["W", "N", "W", "W", "W", "N", "W"],
          ["W", "N", "N", "N", "N", "N", "W"],
          ["W", "W", "W", "W", "W", "W", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "N", "N", "N", "N", "N", "W"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["W", "N", "N", "N", "N", "N", "W"],
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "N", "W", "W", "W", "N", "W"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["W", "N", "N", "N", "N", "N", "W"],
          ["W", "N", "N", "N", "N", "N", "W"],
          ["W", "N", "N", "N", "N", "N", "W"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["W", "N", "W", "W", "W", "N", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "N", "W", "N", "W", "N", "W"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["W", "N", "N", "N", "N", "N", "W"],
          ["N", "N", "N", "G", "N", "N", "N"],
          ["W", "N", "N", "N", "N", "N", "W"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["W", "N", "W", "N", "W", "N", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "N", "W", "W", "W", "N", "W"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["W", "N", "N", "N", "N", "N", "W"],
          ["W", "N", "N", "N", "N", "N", "W"],
          ["W", "N", "N", "N", "N", "N", "W"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["W", "N", "W", "W", "W", "N", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "N", "N", "N", "N", "N", "W"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["N", "N", "N", "N", "N", "N", "N"],
          ["W", "N", "N", "N", "N", "N", "W"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["W", "W", "W", "W", "W", "W", "W"],
          ["W", "N", "N", "N", "N", "N", "W"],
          ["W", "N", "W", "W", "W", "N", "W"],
          ["W", "N", "W", "N", "W", "N", "W"],
          ["W", "N", "W", "W", "W", "N", "W"],
          ["W", "N", "N", "N", "N", "N", "W"],
          ["W", "W", "W", "W", "W", "W", "W"]
        ]
      }
    ],
    me_controller: [
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ['C', 'C', 'C', 'C', 'C'],
          ['C', 'Q', 'Q', 'Q', 'C'],
          ['C', 'Q', 'Q', 'Q', 'C'],
          ['C', 'Q', 'Q', 'Q', 'C'],
          ['C', 'C', 'C', 'C', 'C']
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ['C', 'Q', 'Q', 'Q', 'C'],
          ['Q', 'S', 'S', 'S', 'Q'],
          ['Q', 'S', 'E', 'S', 'Q'],
          ['Q', 'S', 'S', 'S', 'Q'],
          ['C', 'Q', 'Q', 'Q', 'C']
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ['C', 'Q', 'Q', 'Q', 'C'],
          ['Q', 'S', 'E', 'S', 'Q'],
          ['Q', 'E', 'F', 'E', 'Q'],
          ['Q', 'S', 'E', 'S', 'Q'],
          ['C', 'Q', 'Q', 'Q', 'C']
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ['C', 'Q', 'Q', 'Q', 'C'],
          ['Q', 'S', 'S', 'S', 'Q'],
          ['Q', 'S', 'E', 'S', 'Q'],
          ['Q', 'S', 'S', 'S', 'Q'],
          ['C', 'Q', 'Q', 'Q', 'C']
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ['C', 'C', 'C', 'C', 'C'],
          ['C', 'Q', 'Q', 'Q', 'C'],
          ['C', 'Q', 'Q', 'Q', 'C'],
          ['C', 'Q', 'Q', 'Q', 'C'],
          ['C', 'C', 'C', 'C', 'C']
        ]
      }
    ],
    mekanism_steel_casing: [
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["S", "O", "O", "O", "S"],
          ["O", "S", "O", "S", "O"],
          ["O", "O", "S", "O", "O"],
          ["O", "S", "O", "S", "O"],
          ["S", "O", "O", "O", "S"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["O", "S", "O", "S", "O"],
          ["S", "G", "G", "G", "S"],
          ["O", "G", "G", "G", "O"],
          ["S", "G", "G", "G", "S"],
          ["O", "S", "O", "S", "O"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["O", "O", "S", "O", "O"],
          ["O", "G", "G", "G", "O"],
          ["S", "G", "G", "G", "S"],
          ["O", "G", "G", "G", "O"],
          ["O", "O", "S", "O", "O"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["O", "S", "O", "S", "O"],
          ["S", "G", "G", "G", "S"],
          ["O", "G", "G", "G", "O"],
          ["S", "G", "G", "G", "S"],
          ["O", "S", "O", "S", "O"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["S", "O", "O", "O", "S"],
          ["O", "S", "O", "S", "O"],
          ["O", "O", "S", "O", "O"],
          ["O", "S", "O", "S", "O"],
          ["S", "O", "O", "O", "S"]
        ]
      }
    ],
    fission_reactor_casing: [
      {
        type: 'compactcrafting:filled',
        component: 'L'
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["L", "L", "L"],
          ["L", "S", "L"],
          ["L", "L", "L"]
        ]
      },
      {
        type: 'compactcrafting:filled',
        component: 'L'
      }
    ],
    boiler_casing: [
      {
        type: 'compactcrafting:filled',
        component: 'S'
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["S", "S", "S"],
          ["S", "I", "S"],
          ["S", "S", "S"]
        ]
      },
      {
        type: 'compactcrafting:filled',
        component: 'S'
      }
    ],
    turbine_casing: [
      {
        type: 'compactcrafting:filled',
        component: 'S'
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["S", "S", "S"],
          ["S", "O", "S"],
          ["S", "S", "S"]
        ]
      },
      {
        type: 'compactcrafting:filled',
        component: 'S'
      }
    ],
    induction_casing: [
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["S", "C", "C", "C", "S"],
          ["C", "S", "S", "S", "C"],
          ["C", "S", "S", "S", "C"],
          ["C", "S", "S", "S", "C"],
          ["S", "C", "C", "C", "S"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["S", "C", "C", "C", "S"],
          ["C", "N", "N", "N", "C"],
          ["C", "N", "N", "N", "C"],
          ["C", "N", "N", "N", "C"],
          ["S", "C", "C", "C", "S"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["S", "C", "C", "C", "S"],
          ["C", "N", "N", "N", "C"],
          ["C", "N", "E", "N", "C"],
          ["C", "N", "N", "N", "C"],
          ["S", "C", "C", "C", "S"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["S", "C", "C", "C", "S"],
          ["C", "N", "N", "N", "C"],
          ["C", "N", "N", "N", "C"],
          ["C", "N", "N", "N", "C"],
          ["S", "C", "C", "C", "S"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["S", "C", "C", "C", "S"],
          ["C", "S", "S", "S", "C"],
          ["C", "S", "S", "S", "C"],
          ["C", "S", "S", "S", "C"],
          ["S", "C", "C", "C", "S"]
        ]
      }
    ],
    fusion_reactor_frame: [
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["S", "O", "O", "O", "S"],
          ["O", "S", "S", "S", "O"],
          ["O", "S", "S", "S", "O"],
          ["O", "S", "S", "S", "O"],
          ["S", "O", "O", "O", "S"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["S", "O", "O", "O", "S"],
          ["O", "N", "N", "N", "O"],
          ["O", "N", "N", "N", "O"],
          ["O", "N", "N", "N", "O"],
          ["S", "O", "O", "O", "S"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["S", "O", "O", "O", "S"],
          ["O", "N", "N", "N", "O"],
          ["O", "N", "S", "N", "O"],
          ["O", "N", "N", "N", "O"],
          ["S", "O", "O", "O", "S"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["S", "O", "O", "O", "S"],
          ["O", "N", "N", "N", "O"],
          ["O", "N", "N", "N", "O"],
          ["O", "N", "N", "N", "O"],
          ["S", "O", "O", "O", "S"]
        ]
      },
      {
        type: 'compactcrafting:mixed',
        pattern: [
          ["S", "O", "O", "O", "S"],
          ["O", "S", "S", "S", "O"],
          ["O", "S", "S", "S", "O"],
          ["O", "S", "S", "S", "O"],
          ["S", "O", "O", "O", "S"]
        ]
      }
    ]
  };
  
  const recipes = [
    {
      //tiny machine
      recipeSize: 3,
      layers: machineShapes.tiny_machine,
      catalyst: {
        id: 'minecraft:ender_pearl',
        Count: 1
      },
      components: {
        'W': {
          type: 'compactcrafting:block',
          block: 'compactmachines:wall'
        },
        'C': {
          type: 'compactcrafting:block',
          block: 'minecraft:copper_block'
        }
      },
      outputs: [{
        id: 'compactmachines:machine_tiny',
        Count: 1
      }]
    },
    {
      //small machine
      recipeSize: 3,
      layers: machineShapes.small_machine,
      catalyst: {
        id: 'minecraft:ender_pearl',
        Count: 1
      },
      components: {
        'I': {
          type: 'compactcrafting:block',
          block: 'minecraft:iron_block'
        },
        'T': {
          type: 'compactcrafting:block',
          block: 'compactmachines:machine_tiny'
        }
      },
      outputs: [{
        id: 'compactmachines:machine_small',
        Count: 1
      }]
    },
    {
      //normal machine
      recipeSize: 5,
      layers: machineShapes.normal_machine,
      catalyst: {
        id: 'minecraft:ender_pearl',
        Count: 1
      },
      components: {
        'W': {
          type: 'compactcrafting:block',
          block: 'compactmachines:wall'
        },
        'G': {
          type: 'compactcrafting:block',
          block: 'minecraft:gold_block'
        },
        'S': {
          type: 'compactcrafting:block',
          block: 'compactmachines:machine_small'
        }
      },
      outputs: [{
        id: 'compactmachines:machine_normal',
        Count: 1
      }]
    },
    {
      //large machine
      recipeSize: 5,
      layers: machineShapes.large_machine,
      catalyst: {
        id: 'minecraft:ender_pearl',
        Count: 1
      },
      components: {
        'W': {
          type: 'compactcrafting:block',
          block: 'compactmachines:wall'
        },
        'O': {
          type: 'compactcrafting:block',
          block: 'minecraft:obsidian'
        },
        'N': {
          type: 'compactcrafting:block',
          block: 'compactmachines:machine_normal'
        }
      },
      outputs: [{
        id: 'compactmachines:machine_large',
        Count: 1
      }]
    },
    {
      //giant machine
      recipeSize: 7,
      layers: machineShapes.giant_machine,
      catalyst: {
        id: 'minecraft:ender_pearl',
        Count: 1
      },
      components: {
        'W': {
          type: 'compactcrafting:block',
          block: 'compactmachines:wall'
        },
        'D': {
          type: 'compactcrafting:block',
          block: 'minecraft:diamond_block'
        },
        'L': {
          type: 'compactcrafting:block',
          block: 'compactmachines:machine_large'
        }
      },
      outputs: [{
        id: 'compactmachines:machine_giant',
        Count: 1
      }]
    },
    {
      //max machine
      recipeSize: 7,
      layers: machineShapes.max_machine,
      catalyst: {
        id: 'minecraft:ender_pearl',
        Count: 1
      },
      components: {
        'W': {
          type: 'compactcrafting:block',
          block: 'compactmachines:wall'
        },
        'N': {
          type: 'compactcrafting:block',
          block: 'minecraft:netherite_block'
        },
        'G': {
          type: 'compactcrafting:block',
          block: 'compactmachines:machine_giant'
        }
      },
      outputs: [{
        id: 'compactmachines:machine_maximum',
        Count: 1
      }]
    },
    {
      //me_controller
      recipeSize: 5,
      layers: machineShapes.me_controller,
      catalyst: {
        id: 'ae2:quartz_vibrant_glass',
        Count: 1
      },
      components: {
        'F': {
          type: 'compactcrafting:block',
          block: 'ae2:fluix_block'
        },
        'S': {
          type: 'compactcrafting:block',
          block: 'ae2:smooth_sky_stone_block'
        },
        'E': {
          type: 'compactcrafting:block',
          block: 'ae2:energy_acceptor'
        },
        'Q': {
          type: 'compactcrafting:block',
          block: 'ae2:quantum_ring'
        },
        'C': {
          type: 'compactcrafting:block',
          block: 'ae2:chiseled_quartz_block'
        }
      },
      outputs: [{
        id: 'ae2:controller',
        Count: 1
      }]
    },
    {
      //mekanism_steel_casing
      recipeSize: 5,
      layers: machineShapes.mekanism_steel_casing,
      catalyst: {
        id: 'kubejs:cube2',
        Count: 1
      },
      components: {
        'S': {
          type: 'compactcrafting:block',
          block: 'mekanism:block_steel'
        },
        'O': {
          type: 'compactcrafting:block',
          block: 'thermal:lead_block'
        },
        'G': {
          type: 'compactcrafting:block',
          block: 'mekanism:block_osmium'
        }
      },
      outputs: [{
        id: 'mekanism:steel_casing',
        Count: 4
      }]
    },
    {
      //fission reactor casing
      recipeSize: 3,
      layers: machineShapes.fission_reactor_casing,
      catalyst: {
        id: 'kubejs:cube3',
        Count: 1
      },
      components: {
        'S': {
          type: 'compactcrafting:block',
          block: 'mekanism:steel_casing'
        },
        'L': {
          type: 'compactcrafting:block',
          block: 'thermal:lead_block'
        }
      },
      outputs: [{
        id: 'mekanismgenerators:fission_reactor_casing',
        Count: 4
      }]
    },
    {
      //boiler casing
      recipeSize: 3,
      layers: machineShapes.boiler_casing,
      catalyst: {
        id: 'kubejs:cube3',
        Count: 1
      },
      components: {
        'S': {
          type: 'compactcrafting:block',
          block: 'mekanism:steel_casing'
        },
        'I': {
          type: 'compactcrafting:block',
          block: 'minecraft:iron_block'
        }
      },
      outputs: [{
        id: 'mekanism:boiler_casing',
        Count: 4
      }]
    },
    {
      //turbine casing
      recipeSize: 3,
      layers: machineShapes.turbine_casing,
      catalyst: {
        id: 'kubejs:cube3',
        Count: 1
      },
      components: {
        'S': {
          type: 'compactcrafting:block',
          block: 'mekanism:steel_casing'
        },
        'O': {
          type: 'compactcrafting:block',
          block: 'mekanism:block_osmium'
        }
      },
      outputs: [{
        id: 'mekanismgenerators:turbine_casing',
        Count: 4
      }]
    },
    {
      //induction casing
      recipeSize: 5,
      layers: machineShapes.induction_casing,
      catalyst: {
        id: 'kubejs:cube4',
        Count: 1
      },
      components: {
        'S': {
          type: 'compactcrafting:block',
          block: 'mekanism:steel_casing'
        },
        'C': {
          type: 'compactcrafting:block',
          block: 'mekanismgenerators:fission_reactor_casing'
        },
        'N': {
          type: 'compactcrafting:block',
          block: 'minecraft:netherite_block'
        },
        'E': {
          type: 'compactcrafting:block',
          block: 'mekanism:ultimate_energy_cube'
        }
      },
      outputs: [{
        id: 'mekanism:induction_casing',
        Count: 4
      }]
    },
    {
      //fusion reactor frame
      recipeSize: 5,
      layers: machineShapes.fusion_reactor_frame,
      catalyst: {
        id: 'kubejs:cube4',
        Count: 1
      },
      components: {
        'S': {
          type: 'compactcrafting:block',
          block: 'mekanism:steel_casing'
        },
        'N': {
          type: 'compactcrafting:block',
          block: 'minecraft:netherite_block'
        },
        'O': {
          type: 'compactcrafting:block',
          block: 'mekanism:block_refined_obsidian'
        }
      },
      outputs: [{
        id: 'mekanismgenerators:fusion_reactor_frame',
        Count: 4
      }]
    }
  ];
  
  recipes.forEach(recipe => {
    let output = recipe.outputs[0].id;
    console.info(`Processing recipe for: ${output}`);
    event.remove({output: output});
    event.custom({
      type: 'compactcrafting:miniaturization',
      recipeSize: recipe.recipeSize,
      layers: recipe.layers,
      catalyst: recipe.catalyst,
      components: recipe.components,
      outputs: recipe.outputs
    });
  });
});