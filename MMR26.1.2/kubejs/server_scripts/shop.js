// priority: 0

// The Shop (#213): AdminShop's example Offers go, so only the pack's own are offered. The pack's Offers are
// datapack recipes in kubejs/data/mechanicalmasteryreborn/recipe/shop/, which scripts/check_offers.py checks.
ServerEvents.recipes(event => {
  event.remove({ id: /^adminshop:example_/ })
})
