# Miraculous Manatee Mod

A NeoForge mod for Minecraft 1.21 that adds manatees, penguins, an evil manatee that hunts at night, an Elder
Manatee boss, a key-and-portal progression loop into its own dimension, and the
**Manatee Springs** biome (a swamp replacement with clear spring pools, kelp beds and magical plants).

- **Manatee**: tame it with kelp, then it grazes kelp and lily pads on its own, gets visibly fatter, and stores
  blubber in a 27-slot belly (shift right-click to open, right-click to sit).
- **Blubber**: the manatee's drop. Stack it into piles and blocks, or fire it from the blubber blaster.
- **Evil manatee**: a monster that walks and swims after players, hops, and occasionally rages.
- **Penguin**: a passive land animal that follows fish.
- **Elder Manatee**: a summoned boss with a boss bar, a tidal slam that damages, knocks back and slows
  everything nearby, and a one-shot summon of two evil manatees at half health.
- **Summoning ritual**: manatees and evil manatees drop a **Manatee Head** 25% of the time. Build a T out of
  four blubber blocks - three in a row with one below the middle - and crown the top row with three manatee
  heads. Placing the last head summons the Elder Manatee, wither-style. The T works in any orientation, and
  the ritual will not fire on Peaceful.
- **Manatee Key**: each of the three manatee kinds drops a key piece; craft the three together into a Manatee
  Key, use it on a Manatee Portal Altar (8 prismarine around a blubber block) and a prismarine portal frame
  rises around the altar.
- **Manatee Dimension**: step through the portal into an ocean-block world of prismarine, sand and sea
  lanterns, populated by manatees, evil manatees and drowned.

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

## Publishing to CurseForge

Publishing is automated by `.github/workflows/publish-curseforge.yml`. Before the first release:

1. Create the mod project on CurseForge and copy its numeric project ID.
2. Create a GitHub Actions environment named `curseforge`.
3. Add `CURSEFORGE_PROJECT_ID` as a repository variable and `CURSEFORGE_API_TOKEN` as a repository secret.
   The token must belong to a CurseForge account with permission to upload files to the project.

To publish, update `mod_version` in `gradle.properties`, then publish a GitHub release whose tag is exactly
`v<mod_version>` (for example, `v1.0.0`). The workflow runs the full build and uploads the release jar. The
GitHub release notes become the CurseForge changelog; GitHub prereleases are uploaded as beta files, while
normal releases are uploaded as release files. GeckoLib and TerraBlender are declared as required projects.

## Where things live

```
src/main/java/net/neetcoders/miraculousmanatee/
  MiraculousManateeMod   entry point: wires registers and configs, provides id(path)
  registry/              ModBlocks, ModItems, ModEntities, ModPoiTypes, ModCreativeTabs, ModEventHandlers
  entity/                Manatee, EvilManatee, ElderManatee, ElderManateeSummoning, Penguin, BlubberProjectile, entity/goal/ AI goals
  block/, item/          block and item classes; block/portal/ holds the frame layout and travel logic
  worldgen/              biome + dimension keys, TerraBlender region + surface rules, spring pool feature, config-driven spawns
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
  data/miraculousmanatee/loot_table/, recipe/                       entity + block drops, crafting recipes
  data/miraculousmanatee/dimension/, dimension_type/, worldgen/noise_settings/   the Manatee Dimension
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
3. **Loot/recipe**: add JSON under `data/miraculousmanatee/loot_table/` (entity tables are named after the
   registry name) or `data/miraculousmanatee/recipe/`. No datagen; these are hand-written.
4. **Config option**: add it to the matching `Mod*Config` class and a `config.miraculousmanatee.*` plus
   `miraculousmanatee.configuration.*` entry in `en_us.json`. Never rename existing keys: that silently
   resets users' config files.

## Configuration

NeoForge writes the config files on first launch and there is an in-game screen (Mods > Miraculous Manatee >
Config) that edits the same TOML values.

| File | Scope | Contents |
| --- | --- | --- |
| `serverconfig/miraculousmanatee-server.toml` (per world) | gameplay | natural spawns per mob (`manatee.*`, `penguin.*`, `evilManatee.*`), blubber blaster cooldown, taming chance, manatee fat/grazing, evil manatee rage, elder manatee tidal slam (`elderManatee.slamCooldownTicks`, `elderManatee.slamDamage`) |
| `config/miraculousmanatee-common.toml` | shared, non-visual | verbose logging toggle (reserved, unused) |
| `config/miraculousmanatee-client.toml` | visual only | render blubber projectiles |

Spawn options take effect after a world restart; the common and client toggles after a game restart.
