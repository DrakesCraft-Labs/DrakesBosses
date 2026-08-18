<p align="center">
  <img src="https://raw.githubusercontent.com/DrakesCraft-Labs/DrakesBosses/main/banner.svg" width="100%" alt="DRAKES BOSSES animated banner" />
</p>

# DrakesBosses

> ### 🏰 ¡Únete a la Comunidad Oficial de DrakesCraft!
> 
> * 🎮 **IP del Servidor**: `play.drakescraft.net` *(Java 1.21.11 & Bedrock)*
> * 💬 **Discord Oficial**: [discord.gg/drakescraft](https://discord.gg/rR7FbfCt9Y)
> * 🌐 **Web & Guía**: [drakescraft.net](https://drakescraft.net) — 🛒 **Tienda**: [tienda.drakescraft.net](https://tienda.drakescraft.net)
> 
> *¡Juega con este addon y más de 80 expansiones optimizadas en vivo en nuestra network de supervivencia técnica!*

---

<p align="center">
  <strong>Isolated boss encounters for DrakesCraft.</strong><br>
  Arenas, entry fees, containment, rewards and a small public event API.
</p>

## Responsibility

DrakesBosses owns the complete lifecycle of a boss encounter. It is deliberately
separate from Odysseia so that commerce, kits and general server systems never
need to manage combat-world state.

- Reserves an isolated arena cell in `drakes_bosses`.
- Validates the arena world before charging Dragmas.
- Charges each participant once and refunds only the successful withdrawals if
  a session cannot be initialized.
- Contains entities inside the arena and returns participants and spectators to
  their original locations.
- Delivers boss loot through inventory or its persistent mailbox, never through
  ground drops.
- Emits `BossVictoryEvent` after rewards are resolved. DiosesDrakes consumes
  that event to award idempotent divine favor.

## Boss roster and combat engine

The current engine ships more than a generic health bar. It owns a common
multi-phase combat director, local telegraphs, bounded mobility, temporary
phase shields, phase ruptures and per-boss skill scheduling.

| Pantheon or family | Bosses |
| --- | --- |
| Greek | Circe, Polifemo, Ares, Hades, Poseidon, Zeus, Artemisa, Tifon, Prometeo and Cerbero. |
| Nordic | Thor, Loki, Odin, Heimdall and Kratos. |
| Egyptian | Ra, Isis, Anubis and Set. |
| Cataclysmic | Hidra, End Colossus, Wither Storm and Ancestral Dragon. |
| Original | Corrupted God, Jax and Cosmic Garou. |

Bosses combine reusable and specialized attacks: lightning chains, gravity
wells, cyclones, starfall, arcane missiles, seismic spikes, soul shields,
summons, thrown divine weapons, clones, portals, tsunamis and mobility skills.
Messages are local to the encounter instead of global chat spam.

### Adaptive balance

- Arena sessions can scale health and power for solo or group encounters.
- Damage contribution is tracked per player for reward eligibility.
- High-power weapons trigger bounded adaptive counters rather than instakills.
- Full Infinity armor has a dedicated non-lethal counter with cooldowns.
- Phase transitions grant short invulnerability and visible rupture telegraphs.
- Piglin-based bosses retain their original entity to prevent zombification
  from discarding boss state, equipment, phases or metadata.

## Arena and reward lifecycle

Arena cells are allocated on a fixed grid inside the flat `drakes_bosses`
world. Sessions retain return locations, participants, spectators, fees and
owned entities. Players keep inventory during managed encounters; leaving,
victory, failure or cleanup returns everyone safely and releases the cell.

Rewards never become uncontrolled ground drops. Eligible contributors receive
configured relics, safe Slimefun materials, money/experience or mailbox items.
The persistent mailbox retries items that did not fit and prevents a restart
from losing the unresolved delivery.

## Public integration surface

- `BossArenaService`: controlled encounter creation for other plugins.
- `BossVictoryEvent`: stable post-reward event consumed by DiosesDrakes.
- DiosesDrakes bridge: pantheon identity and divine reward context.
- DrakesArcana boundary: permits configured PvE effects in arena worlds.
- Slimefun integration: resolves configured reward IDs without making
  Slimefun the authority for arena loot.

EliteMobs is being evaluated only as an optional power/AI provider. It must not
own sessions, rewards, economy or natural spawns in DrakesCraft.

## Commands

| Command | Audience | Purpose |
| --- | --- | --- |
| `/bosswarp <boss> solo` | Players | Opens a paid solo encounter. |
| `/bosswarp <boss> grupo <players...>` | Players | Opens a paid group encounter; each participant pays. |
| `/bosswarp precios` | Players | Shows configured entry fees. |
| `/bosswarp spectate <player>` | Players | Watches an active encounter. |
| `/bosswarp staff <boss> <players...>` | Staff | Creates a free isolated test encounter. |
| `/boss spawn <boss>` | Staff | Creates a free isolated test encounter for the executor. |
| `/boss give <player> <boss>` | Staff | Gives a summoner that opens an isolated encounter. |

`/spawnallbosses` remains as a compatibility command but intentionally creates
nothing. A mass spawn in a survival world is unsafe; use staff arenas instead.

## Permissions

| Permission | Default | Purpose |
| --- | --- | --- |
| `drakesbosses.bosswarp.use` | Everyone | Public arena access. |
| `drakesbosses.bosswarp.staff` | OP | Staff arena orchestration. |
| `drakesbosses.bosswarp.free` | OP | Entry-fee bypass for controlled tests. |
| `drakesbosses.admin` | OP | Boss administration and summoners. |

The legacy `odysseia.bosswarp.staff` permission is recognized only during the
transition. New LuckPerms grants must use the `drakesbosses.*` namespace.

## Safe Deployment

1. Back up the current boss world and plugin data.
2. Make sure `drakes_bosses` exists and is a flat world; the plugin will refuse
   a paid entry rather than create or overwrite a world.
3. Remove the boss lifecycle from the active Odysseia JAR in the same restart.
4. Add the DrakesBosses JAR, validate `/bosswarp precios`, then run a staff
   arena with a low-risk boss.
5. Confirm a forced spawn failure returns all paid entries exactly once before
   enabling public use.

## Build

```bash
mvn clean verify
```

Requires Java 21 and Paper `1.21.11`. `mvn clean verify` also runs tests for
arena configuration/pricing, combat profiles, configuration scope and the
Infinity armor counter.

