ItemEvents.modification(event => {
  event.modify('projectexpansion:infinite_steak', item => {
      item.setFoodProperties(food => {
          food.hunger(5);
          food.saturation(5);
      });
  });
});