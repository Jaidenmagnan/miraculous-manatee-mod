# Miraculous Manatee Mod

A NeoForge mod for Minecraft 1.21 that adds manatees, penguins, an evil manatee that hunts at night, and the
**Manatee Springs** biome (a swamp replacement with clear spring pools, kelp beds and magical plants).

- **Manatee**: tame it with kelp, then it grazes kelp and lily pads on its own, gets visibly fatter, and stores
  blubber in a 27-slot belly (shift right-click to open, right-click to sit).
- **Blubber**: the manatee's drop. Stack it into piles and blocks, or fire it from the blubber blaster.
- **Evil manatee**: a monster that walks and swims after players, hops, and occasionally rages.
- **Penguin**: a passive land animal that follows fish.

## Building and running

Requirements: JDK 21 (the Gradle wrapper downloads Gradle itself; dependencies come from Maven).

```bash
./gradlew build          # jar in build/libs/
./gradlew runClient      # dev client with the mod loaded
./gradlew runServer      # dedicated server (--nogui)
```

The first build downloads and decompiles Minecraft, which takes a few minutes. If your IDE loses
dependencies, run `./gradlew --refresh-dependencies`. Game files (worlds, logs, configs) go under `run/`.

Mappings are Mojang's official names, made friendlier by Parchment. See
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md for the mapping license.

## Where things live

```
src/main/java/net/neetcoders/miraculousmanatee/
  MiraculousManateeMod   entry point: wires registers and configs, provides id(path)
  registry/              ModBlocks, ModItems, ModEntities, ModCreativeTabs, ModEventHandlers
  entity/                Manatee, EvilManatee, Penguin, BlubberProjectile, entity/goal/ AI goals
  block/, item/          block and item classes
  worldgen/              biome key, TerraBlender region + surface rules, spring pool feature, config-driven spawns
  config/                ModServerConfig / ModCommonConfig / ModClientConfig (NeoForge ModConfigSpec)
  client/                renderers and client-only event handlers (never referenced from common code)

src/main/resources/
  assets/miraculousmanatee/
    geo/entity/<name>.geo.json, geo/item/<name>.geo.json            GeckoLib models
    animations/entity/<name>.animation.json, animations/item/...    GeckoLib animations
    textures/entity/, textures/block/, textures/item/
    blockstates/, models/, lang/en_us.json
  data/miraculousmanatee/worldgen/                                  biome, configured/placed features
  data/miraculousmanatee/neoforge/biome_modifier/                   attaches ConfigurableSpringsSpawnsModifier
blockbench/                                                         Blockbench source projects (not shipped)
```

Entity and item renderers use GeckoLib's defaulted models, so assets are found **by registry name**: a new
entity `foo` needs `geo/entity/foo.geo.json`, `textures/entity/foo.png` and
`animations/entity/foo.animation.json`. Export from Blockbench straight to those paths.

### Adding something new

1. **Block/item**: register in `ModBlocks` / `ModItems`, add it to the tab in `ModCreativeTabs`, add
   blockstate/model/texture JSON and an `en_us.json` entry.
2. **Entity**: entity class in `entity/`, register the type in `ModEntities`, attributes and spawn placement in
   `ModEventHandlers`, renderer in `client/renderer/` registered from `ClientEventHandlers`, spawn egg in
   `ModItems`, assets as above.
3. **Config option**: add it to the matching `Mod*Config` class and a `config.miraculousmanatee.*` plus
   `miraculousmanatee.configuration.*` entry in `en_us.json`. Never rename existing keys: that silently
   resets users' config files.

## Configuration

NeoForge writes the config files on first launch and there is an in-game screen (Mods > Miraculous Manatee >
Config) that edits the same TOML values.

| File | Scope | Contents |
| --- | --- | --- |
| `serverconfig/miraculousmanatee-server.toml` (per world) | gameplay | natural spawns per mob (`manatee.*`, `penguin.*`, `evilManatee.*`), blubber blaster cooldown, taming chance, manatee fat/grazing, evil manatee rage |
| `config/miraculousmanatee-common.toml` | shared, non-visual | verbose logging toggle (reserved, unused) |
| `config/miraculousmanatee-client.toml` | visual only | render blubber projectiles |

Spawn options take effect after a world restart; the common and client toggles after a game restart.
