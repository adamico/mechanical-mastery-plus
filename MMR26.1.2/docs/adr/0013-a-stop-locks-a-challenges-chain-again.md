# A stop locks a challenge's chain again

A team can stop the challenge it plays at the lab, once the attempt has reset at least once, to take on another or come back to it better prepared. A stop locks the challenge's chain again, however many resets came before, and the chain opens again when the team starts it anew; a reset still keeps it (ADR 0007). The stop is made when every member online has asked for it at the lab.

A chain that stayed unlocked past a stop would let a team start each challenge, stop it a minute later, and build every chain with no handicap and no Inflation running, which ADR 0007 rules out. A reset keeps the chain because the team is still fighting that challenge; a stop gives it back because the team has left it.

## Considered options

- **No stop.** A bad pick would hold a team in an endless reset loop against a tier it can't beat, though the challenges are meant to be played in any order.
- **A wait or a faster C.O.D. as a stop's cost, the chain kept.** Either only slows down unlocking every chain ahead, and a team that has won a few challenges has little left to charge.
- **A stop at any time.** With the chain locked again an early stop gains nothing, but in protection a team losing a wave could stop and start again to get wave 1 back. After a reset the Assault is already gone, so only then is there nothing left to escape.
- **The owner's word, or every member's vote with a timeout for the absent.** Teams have no owner, and a vote of every member would wait on players who never log in again.

## Consequences

- Only the current attempt's resets count: a team that stops and starts the challenge again must reset once more before it can stop it again.
- A member's ask is dropped when they log out, and a member who logs in while a vote is open counts from then on, so a member alone on the server can stop the challenge. Every ask is cleared when the challenge resets or is won, and a member can take theirs back.
- A stop otherwise ends the challenge as `/cod challenge end` did before it: the handicap and Inflation end, the Port's count goes back to zero, money set aside is given back, an Assault is removed and the Diet forgotten. The team keeps every item.
- Nothing waits after a stop: any challenge not won, the stopped one too, can start at once.
- An operator's `/cod challenge end` is a stop without the vote or the reset, so it locks the chain again too. A played attempt ends only by a win, a reset or a stop.
- Money's chain is the Shop's Offers gated on the `reborn_money` Permit, so a stop takes that Permit from the team's Account, and starting the challenge anew gives it back (#296). The Permits the team bought through it, and the Offers they open, stay, as a built machine does.
