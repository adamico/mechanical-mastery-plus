StartupEvents.registry('block', event => {
  for (let i = 1; i < 5; i++) {
    event.create('common_quarry_block_' + i);
    event.create('uncommon_quarry_block_' + i);
  }

  for (let i = 1; i < 4; i++) {
    event.create('rare_quarry_block_' + i);
  }

  for (let i = 1; i < 3; i++) {
    event.create('legendary_quarry_block_' + i);
  }
});