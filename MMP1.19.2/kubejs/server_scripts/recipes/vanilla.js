ServerEvents.recipes(event => {
  let recipeCycle = (itemIds) => {
    itemIds.forEach((element, index, array) => {
      let input = element
      let output = (element === array[array.length - 1]) ? array[0] : array[index + 1]

      event.shapeless(output, [input])
    })
  }

  recipeCycle(['wheat_seeds', 'beetroot_seeds', 'carrot', 'potato', 'melon_seeds', 'pumpkin_seeds', 'cocoa_beans', 'sweet_berries'])
})
