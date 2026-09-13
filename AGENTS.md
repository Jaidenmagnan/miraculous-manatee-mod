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
