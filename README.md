# AnarchySpawn Plugin
**AnarchySpawn** is a Minecraft Paper 1.21.4 plugin that handles random spawning of players. When players first join or respawn without a bed/anchor, they will be randomly teleported to a safe location within a configurable radius from world coordinates 0,0.

## Features
- Random spawn locations within configurable radius.
- Safe spawn location detection.
- First-join random spawn.
- Random respawn when no bed or respawn anchor is set.
- Dimension restrictions: `/spawn` command cannot be used in the Nether or End.

## Installation
1. Download the latest release [here](https://github.com/Jelly-Pudding/anarchyspawn/releases/latest).
2. Place the `.jar` file in your Minecraft server's `plugins` folder.
3. Restart your server.

## Configuration
In `config.yml`, you can configure:
```yaml
# Maximum distance from 0,0 that players can spawn (in blocks)
spawn-radius: 300

# Maximum number of attempts to find a safe spawn location
max-spawn-attempts: 75

# List of blocks that are considered unsafe to spawn on or next to
unsafe-blocks:
 - LAVA
 - WATER
 - CACTUS
 - FIRE
 - MAGMA_BLOCK
 - SWEET_BERRY_BUSH
 - POWDER_SNOW
 ```

 ## Commands
- `/spawn`: Teleports the player to a random safe location (requires `anarchyspawn.spawn` permission)
- `/anarchyspawn reload`: Reloads the plugin configuration (requires `anarchyspawn.reload` permission)

## Permissions
`anarchyspawn.spawn`: Allows use of the spawn command (default: op)
`anarchyspawn.reload`: Allows reloading the plugin configuration (default: op)

## Support Me
Donations will help me with the development of this project.

One-off donation: https://ko-fi.com/lolwhatyesme

Patreon: https://www.patreon.com/lolwhatyesme
