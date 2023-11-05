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
    .texture('kubejs:darkness_essence_bucket')
    .group('misc');

    event.create('light_essence')
    .thinTexture(0xE0E0E0)
    .bucketColor(0xE0E0E0)
    .displayName('Refined Mixture')
    .bucketItem
    .texture('kubejs:light_essence_bucket')
    .group('misc');
});