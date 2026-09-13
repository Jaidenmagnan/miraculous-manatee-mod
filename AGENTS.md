# Miraculous Manatee Development

## Environment

- Minecraft 1.21, NeoForge 21.1, and Java 21.
- Use the checked-in Gradle wrapper; do not use a system Gradle installation.
- Do not edit generated files under `build/`.

## Commands

- Build: `./gradlew build`
- Run the development client: `./gradlew runClient`
- Run the dedicated server: `./gradlew runServer`
- Generate data: `./gradlew runData`

Run the narrowest relevant check after a change. Run `./gradlew build` before considering a contribution complete.

## Architecture

- Common code must never reference classes under `client/`.
- Register content through the existing classes under `registry/`.
- Use `MiraculousManateeMod.id(...)` for mod resource locations.
- Keep registry names synchronized with their assets and data files.
- GeckoLib entity and item assets are resolved by registry name.
- Never rename an existing config key; doing so resets users' configuration.
- Prefer data-driven world generation and tags over hard-coded lists.

## Change Guidelines

- Read neighboring implementations before introducing a new pattern.
- Keep changes minimal and avoid speculative abstractions.
- Reuse existing dependencies; do not add one without justification.
- Update `en_us.json` for user-visible names and configuration options.
- Keep Blockbench source files under `blockbench/`.
- Do not commit files produced under `run/`, `build/`, or `repo/`.

## Definition of Done

- Code compiles with Java 21.
- `./gradlew build` passes.
- Client-only code remains isolated.
- Required models, textures, translations, and data files are present.
- Documentation is updated when behavior or development commands change.
# CLAUDE.md

Repository map for AI assistants. Read this before exploring so you can go straight to the right file.
See `README.md` for the player-facing description, config table, and "adding something new" recipes.

## KEEP THIS FILE CURRENT

**Whenever you make a significant change, update this file in the same commit.** Significant means:
adding, removing, renaming or moving a Java class, package, resource folder, registered block/item/entity,
config key, worldgen feature, dependency, Gradle task, or CI step. Also update it when a workflow rule below
stops being true. Keep entries short: one line per file or folder, saying what it owns. Do not duplicate
README content; link to it. Update `README.md` too when the change is player- or contributor-visible.

## What this is

NeoForge mod for Minecraft 1.21 (`neo_version` 21.1.150). Mod id `miraculousmanatee`, base package
`net.neetcoders.miraculousmanatee`. Adds manatees, an evil manatee, penguins, blubber items/blocks, the
Manatee Springs biome (replaces vanilla swamp via TerraBlender), five magical spring plants, an Elder Manatee
boss summoned wither-style from a blubber T crowned with three manatee heads, and a key/portal progression
loop that leads into the mod's own Manatee Dimension.
Dependencies: GeckoLib (entity/item models + animations) and TerraBlender (biome placement). Versions live
in `gradle.properties`. There are no unit tests or gametests; verification is `./gradlew build` and playtesting.

## Build and run

```bash
./gradlew build          # jar in build/libs/, also what CI runs (.github/workflows/build.yml)
./gradlew runClient      # dev client
./gradlew runServer      # dedicated server, --nogui
./gradlew runData        # datagen into src/generated/resources (currently unused, folder absent)
```

Java 21 toolchain is auto-provisioned (foojay). The Gradle launcher itself needs a modern JDK on `JAVA_HOME`.
First build decompiles Minecraft (minutes). `run/`, `build/`, `.gradle/`, `.idea/` are gitignored.
`src/main/templates/META-INF/neoforge.mods.toml` is a template; `${...}` placeholders are filled from
`gradle.properties` by the `generateModMetadata` task. Edit the template, never the generated copy.

## Source layout (`src/main/java/net/neetcoders/miraculousmanatee/`)

| Path | Owns |
| --- | --- |
| `MiraculousManateeMod.java` | Entry point. Registers all `DeferredRegister`s and the three configs. `id(path)` builds mod `ResourceLocation`s. |
| `registry/ModBlocks.java` | `blubber_pile`, `blubber_block`, five spring plants (`luminous_cattail`, `mistveil_fern`, `springheart_bloom`, `azure_dewcap`, `moonlit_lotus`) via `registerSpringPlant`, `manatee_portal_altar`, `manatee_portal`, `manatee_head`. |
| `registry/ModItems.java` | `blubber`, `blubber_blaster`, three key pieces + `manatee_key`, block items for every block including `manatee_head` (no portal block item), four spawn eggs. |
| `registry/ModEntities.java` | `manatee` (WATER_CREATURE), `evil_manatee` (MONSTER), `elder_manatee` (MONSTER, no natural spawn; spawn egg or the summoning ritual), `penguin` (CREATURE), `blubber_projectile`. |
| `registry/ModPoiTypes.java` | `manatee_portal` `PoiType` over every portal block state; lets `ManateePortalTravel` find existing portals. |
| `registry/ModCreativeTabs.java` | Single tab `main`; contents are added in `ModEventHandlers`. |
| `registry/ModEventHandlers.java` | Mod-bus events: manatee belly `ItemHandler` capability, entity attributes, spawn placements, creative tab contents. |
| `entity/Manatee.java` | Tameable water animal: kelp taming, grazing, fat level, 27-slot belly inventory, sit. |
| `entity/EvilManatee.java` | Hostile amphibious monster. Dual navigation (water/ground) swapped per tick, custom move control, hop, rage. Large javadoc explains pathfinding. |
| `entity/Penguin.java` | Passive land animal, follows fish. |
| `entity/ElderManatee.java` | Boss subclass of `EvilManatee`: `ServerBossEvent` boss bar, tidal slam AoE (server config), one-shot summon of two evil manatees at half health. |
| `entity/ElderManateeSummoning.java` | Wither-style ritual: lazily built `BlockPattern` (blubber T + three manatee heads), spawns the boss, clears the structure. Javadoc explains the layout. |
| `entity/BlubberProjectile.java` | Projectile fired by the blubber blaster. |
| `entity/goal/ManateeGrazeGoal.java` | AI goal: find reachable water plant, travel, eat. Javadoc documents the phase lifecycle. |
| `block/BlubberPileBlock.java` | Snow-layer style pile; full pile becomes `BlubberBlock`. |
| `block/BlubberBlock.java` | Solid blubber block. |
| `block/ManateeHeadBlock.java` | Horizontal-facing mob head, 8x8x8 shape. Drops from manatees at 25%; `setPlacedBy` runs the summoning check. |
| `block/ManateePortalAltarBlock.java` | Portal keystone; its own class so `ManateeKeyItem` can `instanceof`-check it. |
| `block/ManateePortalBlock.java` | Portal interior block (`implements Portal`), modelled on `NetherPortalBlock`. No block item. |
| `block/portal/ManateePortalShape.java` | Sole owner of the 4x5 frame layout: `isFrame`, `canBuild`, `build`, `interiorMin`, `entryPosition`. |
| `block/portal/ManateePortalTravel.java` | Portal destination: search the target dimension's POIs for an existing portal, else build one. |
| `item/BlubberBlasterItem.java` | GeckoLib-animated gun, cooldown from server config. |
| `item/NamedBlockItem.java` | BlockItem with its own item translation key. |
| `item/ManateeKeyItem.java` | `useOn` an altar builds the portal frame and consumes the key. |
| `worldgen/ModWorldgen.java` | Registers biome modifier codec and `spring_pool` feature; registers TerraBlender region + surface rules in common setup. |
| `worldgen/ModBiomes.java` | `MANATEE_SPRINGS` and `MANATEE_PLAINS` biome resource keys. |
| `worldgen/ModDimensions.java` | `MANATEE_DIMENSION` level key and `MANATEE_DIMENSION_TYPE` dimension-type key. |
| `worldgen/ModOverworldRegion.java` | TerraBlender region: vanilla layout with swamp replaced by Manatee Springs. |
| `worldgen/ModSurfaceRules.java` | Surface blocks for the biome (sand/gravel/clay beds, moss shoreline). |
| `worldgen/ConfigurableSpringsSpawnsModifier.java` | Biome modifier that reads natural spawns from server config. |
| `worldgen/feature/SpringPoolFeature.java` | Carves bowl-shaped spring pools with a bubble column vent. |
| `config/ModServerConfig.java` | Per-world gameplay: spawn settings per mob (`SpawnConfig` helper), blaster cooldown, taming, fat/grazing, rage, `elderManatee` slam cooldown/damage. |
| `config/ModCommonConfig.java` | `enableVerboseLogging` (reserved, unused). |
| `config/ModClientConfig.java` | `renderBlubberProjectile`. |
| `client/ClientEventHandlers.java` | Client mod-bus: registers entity renderers. `Dist.CLIENT` only. |
| `client/ClientConfigScreenHooks.java` | Registers the NeoForge config screen; called from the entry point behind a client check. |
| `client/renderer/*Renderer.java` | GeckoLib renderers. `EvilManateeRenderer` and `ElderManateeRenderer` reuse the manatee geo/animations with an alt texture plus glow mask layer. |

**Rule:** nothing in `client/` may be referenced from common code except through the `FMLEnvironment.dist`
guard in the entry point, or the dedicated server crashes.

## Resources (`src/main/resources/`)

```
assets/miraculousmanatee/
  geo/entity/{manatee,penguin}.geo.json        GeckoLib models (evil_manatee and elder_manatee share manatee)
  geo/item/blubber_blaster.geo.json
  animations/entity/{manatee,penguin}.animation.json
  animations/item/blubber_blaster.animation.json
  textures/entity/  manatee, penguin, evil_manatee(_glowmask), elder_manatee(_glowmask)
  textures/block/   one png per plant + blubber_block, manatee_portal, manatee_head (32x16, cropped
                    from the head region of textures/entity/manatee.png)
  textures/item/    blubber, blubber_blaster, three key pieces, manatee_key
  blockstates/, models/block/, models/item/     one json per block / item
  lang/en_us.json   all names + config.miraculousmanatee.* and miraculousmanatee.configuration.* keys
data/miraculousmanatee/
  worldgen/biome/            manatee_springs, manatee_plains (the dimension biome)
  worldgen/configured_feature/  magical_spring_plants, spring_azalea_tree, spring_pool
  worldgen/placed_feature/      dense_kelp, magical_spring_plants, spring_pool, trees_manatee_springs
  neoforge/biome_modifier/configurable_springs_spawns.json   attaches the config-driven spawn modifier
  worldgen/noise_settings/manatee_dimension.json   vanilla overworld noise + a prismarine/sand surface rule
  dimension/manatee_dimension.json        the dimension itself (noise generator, fixed manatee_plains biome)
  dimension_type/manatee_dimension.json   overworld-like dimension type with has_raids false
  loot_table/entities/   manatee, evil_manatee, elder_manatee (the three key pieces; manatee and
                         evil_manatee have a second 25%-chance pool dropping manatee_head)
  loot_table/blocks/     manatee_portal_altar, manatee_head
  recipe/               blubber_block, manatee_key, manatee_portal_altar
data/minecraft/tags/worldgen/biome/   is_overworld, spawns_warm_variant_frogs (adds the biome to vanilla tags)
```

GeckoLib assets are resolved **by registry name** (`DefaultedEntityGeoModel`). A new entity `foo` needs
`geo/entity/foo.geo.json`, `animations/entity/foo.animation.json`, `textures/entity/foo.png`.

`blockbench/` holds Blockbench source projects (`.bbmodel`, not shipped; excluded by `build.gradle`).
Export from there directly to the asset paths above.

## Conventions and gotchas

- Registry names are snake_case and must match asset file names and `en_us.json` keys exactly.
- Config keys are user-facing TOML. **Never rename an existing key**; it silently resets user configs.
  Every new key needs both a `config.miraculousmanatee.<key>` and a `miraculousmanatee.configuration.<key>`
  lang entry.
- Spawn settings apply after world restart; common/client toggles after game restart.
- Worldgen JSON is hand-written, not datagen. Keep `configured_feature` and `placed_feature` ids in sync
  with the biome json.
- **Loot tables** live in `data/miraculousmanatee/loot_table/` (singular folder name in 1.21). Entity tables are
  resolved as `<modid>:entities/<name>` from `EntityType.getDefaultLootTable()`, so the file name must match the
  registry name. Blocks that should not drop use `.noLootTable()` instead of a file.
- **Dimension**: the Manatee Dimension is fully data-driven (`dimension/`, `dimension_type/`,
  `worldgen/noise_settings/`, `worldgen/biome/manatee_plains.json`); the only Java is the resource keys in
  `ModDimensions`. Its noise settings are vanilla `overworld.json` with `default_block` set to prismarine,
  ore veins off and a short custom surface rule - keep the `noise_router` block byte-identical to vanilla when
  regenerating it. `manatee_plains` hard-codes its spawners rather than going through the config-driven modifier,
  and is deliberately NOT added to the `is_overworld` tag.
- Mappings are Mojang official + Parchment. Use Mojang names when searching vanilla code.
- Commit messages: plain descriptive subject. Do not add a `Co-Authored-By` trailer (owner preference).

## Checklist for common changes

Follow the README "Adding something new" section, then update the tables above. Quick reference:

- **Block/item**: `ModBlocks`/`ModItems` -> `ModEventHandlers.onBuildCreativeTabContents` -> blockstate,
  block model, item model, texture -> `en_us.json`.
- **Entity**: `entity/` class -> `ModEntities` -> attributes + spawn placement in `ModEventHandlers` ->
  renderer in `client/renderer/` registered in `ClientEventHandlers` -> spawn egg in `ModItems` -> geo,
  animation, texture -> `en_us.json` -> optional spawn config in `ModServerConfig` and the spawns modifier.
- **Config option**: matching `Mod*Config` class -> two lang keys -> read it where used (never cache at
  class-load time; configs load after registration).
- **Worldgen**: feature class under `worldgen/feature/` registered in `ModWorldgen`, JSON under
  `data/.../worldgen/`, reference from `manatee_springs.json`.
