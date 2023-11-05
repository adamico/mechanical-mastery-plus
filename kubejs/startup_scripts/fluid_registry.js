StartupEvents.registry('fluid', event => {
  event.create('molten_glass')
    .thickTexture(0xFFFFFF)
    .bucketColor(0xFFFFFF)
    .displayName('Molten Glass');

  event.create('darkness_essence')
    .thinTexture(0x202020)
    .bucketColor(0x202020)
    .displayName('Darkness Essence')
    .bucketItem
    .group('misc');

    event.create('light_essence')
    .thinTexture(0xE0E0E0)
    .bucketColor(0xE0E0E0)
    .displayName('Light Essence')
    .bucketItem
    .group('misc');
});