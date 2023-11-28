const GrassBlock = Java.loadClass('net.minecraft.world.level.block.GrassBlock')

BlockEvents.rightClicked(event => {
    // let block = event.block.blockState.block
    // if (block instanceof GrassBlock) {
    //     Utils.server.tell([Text.gray(" [Server] Debug Bonemeal A: "), block])
    //     //TODO: search for correct 1.19.2 method, the following is 1.20.1     
    //     block.performBonemeal(event.block.level, Utils.random, event.block.pos, event.block.blockState);
    //     Utils.server.tell([Text.gray(" [Server] Debug Bonemeal B: "), block]) // Code never reaching here
    // }
})