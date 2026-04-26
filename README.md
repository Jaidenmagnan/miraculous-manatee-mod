
Installation information
=======

This template repository can be directly cloned to get you started with a new
mod. Simply create a new repository cloned from this one, by following the
instructions provided by [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template).

Once you have your clone, simply open the repository in the IDE of your choice. The usual recommendation for an IDE is either IntelliJ IDEA or Eclipse.

If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
{this does not affect your code} and then start the process again.

Mapping Names:
============
By default, the MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md

Additional Resources: 
==========
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/

Configuration
==========

NeoForge generates mod config files automatically after first launch:
- Server gameplay config: `run/serverconfig/miraculousmanatee-server.toml`
- Common shared config: `run/config/miraculousmanatee-common.toml`
- Client visual config: `run/config/miraculousmanatee-client.toml`

Scope summary
----------
- `SERVER` (world/gameplay): entity natural spawn settings (`manatee.*`, `penguin.*`), blubber blaster cooldown, manatee taming chance
- `COMMON` (shared non-visual): troubleshooting/logging toggle
- `CLIENT` (visual-only): blubber projectile rendering toggle

In-game config screen
----------
- Open `Mods` in-game, select `Miraculous Manatee`, then click `Config`.
- The screen uses NeoForge's config UI and edits the same TOML-backed `ModConfigSpec` values.
- No custom parser is used; TOML remains the source of truth.

Reload behavior
----------
- `SERVER` spawn options (`manatee.*`, `penguin.*`) are marked for world restart.
- `CLIENT` and `COMMON` toggles marked as restart-required should be applied after game restart.
- In-game edits persist to:
  - `run/serverconfig/miraculousmanatee-server.toml`
  - `run/config/miraculousmanatee-common.toml`
  - `run/config/miraculousmanatee-client.toml`
