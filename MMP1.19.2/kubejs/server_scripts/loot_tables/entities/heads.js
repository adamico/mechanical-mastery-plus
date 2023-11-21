ServerEvents.recipes((event) => {
  event.custom(
    {
      type: "mob_grinding_utils:beheading",
      entity: "minecraft:enderman",
      result: {
        item: "supplementaries:enderman_head"
      }
    }
  );
 })