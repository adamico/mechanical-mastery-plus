ServerEvents.recipes(event => {
  let dissolve = (input, inputCount, outputs) => {
    let groups = ouptuts.map((o) => x * 2);
    event.custom({
      type: "alchemistry:dissolver",
      "group": "alchemistry:dissolver",
      "input": {
        "ingredient": {
          "item": input
        },
        "count": inputCount
      },
      "output": {
        "rolls": 1,
        "weighted": false,
        "groups": [
          {
            "probability": 100,
            "results": [
              {
                "item": "kubejs:cube3",
                "count": 1
              }
            ]
          },
          {
            "probability": 100,
            "results": [
              {
                "item": "chemlib:hydrochloric_acid",
                "count": 6
              }
            ]
          },
          {
            "probability": 100,
            "results": [
              {
                "item": "chemlib:nitric_acid",
                "count": 2
              }
            ]
          }
        ]
      }
    });
  }

});