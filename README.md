# DrakesBosses

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

Requires Java 21 and Paper `1.21.11`.
