BlockEvents.modification(e => {
  ['raw_copper', 'raw_gold', 'raw_iron', 'redstone'].forEach(material => {
    e.modify('buddingcrystals:budding_' + material, block => {
    block.destroySpeed = -0.5;
    });
  });
})