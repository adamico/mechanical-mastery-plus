# Sal ammoniac ore in the Shop

Reborn's Shop sells neither Theurgy's sal ammoniac ore (`theurgy:sal_ammoniac_ore`, `theurgy:deepslate_sal_ammoniac_ore`) nor the Sal Ammoniac Crystal. It only buys Sal Ammoniac, the fluid.

## Why this is out of scope

The energy chain makes its own Sal Ammoniac and needs nothing from the Shop to start:

- **The Sal Ammoniac Accumulator works on water alone.** Theurgy has an accumulation recipe with no solute, which gives a little Sal Ammoniac from water. A Sal Ammoniac Crystal in the Accumulator only makes it faster.
- **A team makes its own crystal.** Theurgy crafts a Sal Ammoniac Crystal from a bucket of Sal Ammoniac. So a team goes from water to the slow Accumulator, then a bucket, then a crystal, then the fast Accumulator, and every Liquefaction Cauldron recipe gets its solvent from that loop.
- **The crystal has another source too.** It is in Basic Mechanical Essence's mix (#292).

An ore offer would also be a new kind of offer. The Shop's ores are Fieldworks ore nodes behind the `reborn_ore_buy` Permit, which a Harvester mines without end. Theurgy's ore is a block mined once for a few crystals, and those crystals only shorten a start the team can already make.

## Prior requests

- #295: "Reborn's Shop sells no sal ammoniac ore"
