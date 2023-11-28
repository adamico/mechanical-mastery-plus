ServerEvents.recipes(event => {
  event.remove({output: "bigreactors:graphite_dust"});
  event.remove({output: "bigreactors:graphite_ingot", not: {input: 'bigreactors:graphite_block'}});
  event.remove({output: "bigreactors:graphite_block", not: {input: 'bigreactors:graphite_ingot'}});
})