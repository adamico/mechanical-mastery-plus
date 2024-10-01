StartupEvents.registry('fluid', event => {
  event.create('tt_fuel', 'thin')
    .tint(0xFF00FF)
    .displayName('Dwarf Star Soup');
  event.create('tt_raw_fuel', 'thick')
    .tint(0xCC00CC)
    .displayName('Raw Dwarf Star Soup');
  event.create('tt_spent_fuel', 'thick')
    .tint(0x660066)
    .displayName('Spent Dwarf Star Soup');
});