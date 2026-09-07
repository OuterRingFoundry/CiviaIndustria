# CiviaIndustria — BETA PACK 0.1.0-beta.1

**[DOWNLOAD THE CLIENT PACK](https://github.com/OuterRingFoundry/CiviaIndustria/raw/refs/heads/main/BETA-PACK/CiviaIndustria-0.1.0-beta.1.mrpack)** · **[DOWNLOAD THE SERVER INSTALLER ZIP](https://github.com/OuterRingFoundry/CiviaIndustria/raw/refs/heads/main/BETA-PACK/CiviaIndustria-0.1.0-beta.1-server.zip)**

Minecraft **1.21.1** · NeoForge **21.1.249** · Java **21**. This is a playable beta for testing; production acceptance is still open.

## Client installation

1. Download the `.mrpack` above.
2. Import it into Prism Launcher, Modrinth App, or another launcher supporting Modrinth packs and NeoForge.
3. Select Java 21, sign into your Minecraft Java Edition account, and launch. Start with 6–8 GiB allocated memory, adjusted for your machine.

The pack includes the compiled Civitas mod, progression scripts, exact loader version, and the complete dependency manifest. The launcher automatically downloads all **11 client dependency JARs**. Their bundled libraries remain included. No manual dependency selection is needed. Internet is required for the first installation.

## Server installation

1. Download and extract the server ZIP. Install **Java 21** and **Python 3.11+**.
2. In the extracted `server-bootstrap` folder, run:

   ```sh
   python3 install.py --output ../civia-beta-server
   ```

   On Windows, use `py -3.11 install.py --output ../civia-beta-server` (or another installed Python 3.11+).
3. Open the new server folder. Set your memory allocation in `user_jvm_args.txt` (for example, `-Xms2G` and `-Xmx8G` on separate lines).
4. Read [Minecraft's EULA](https://www.minecraft.net/eula). If you agree, create `eula.txt` containing `eula=true`.
5. Run `bash run.sh nogui` on Linux, or `run.bat nogui` on Windows.

The installer downloads and verifies every pinned dependency, selects all **13 server dependency JARs**, adds Civitas and the tested server configuration, and installs NeoForge plus its runtime dependencies. It creates a fresh instance and refuses an existing destination. It does not start a server or accept the EULA for you. After a failed setup, use a new destination; verified downloads are reusable.

For multiplayer, use TCP 25565 for Minecraft and UDP 24454 for Simple Voice Chat, with the corresponding hosting/network configuration. The included server properties retain authenticated online mode. See the [voice setup guide](https://modrepo.de/minecraft/voicechat/wiki/server_setup).

A manual client folder can also be assembled with `python3 install.py --side client --output ../civia-beta-client`; install the pinned NeoForge version in your launcher and use that folder as its game directory.

## Included dependencies

Create, Immersive Engineering, KubeJS, Rhino, Architectury, FramedBlocks, Lightman's Currency, Simple Voice Chat, ModernFix, FerriteCore; plus **Embeddium on clients**, and **ServerCore, spark, Chunky on dedicated servers**. Flywheel, Ponder, Registrate and other nested dependencies remain inside the original parent JARs as shipped upstream. The verifier checks required dependencies and nested mod metadata.

See [all versions, authors, licenses and sources](THIRD-PARTY.md), [exact artifact hashes](server-bootstrap/pack/mods.lock.json), and [the loader pin](server-bootstrap/pack/runtime.lock.json). Public downloads use official upstream URLs rather than rehosting third-party JARs. **Dependencies are installed automatically; this public download is not an offline archive.** Java and the Minecraft account are prerequisites.

## What was tested

The exact Civitas JAR and pinned pack passed the designated-server automated suite: 57 domain checks; 13 tooling checks; 33 GameTests in each of six mod profiles; save/restart and corrupt-save refusal; physical freight/shared-signal restart fixtures; standalone boot, backup and restore. Synthetic staging measured about 20 TPS and 10.43 ms p95. A software-rendered client passed world entry, model and ecological tint checks.

[Validation reports](validation/) · [Full test report](https://github.com/OuterRingFoundry/CiviaIndustria/blob/f5f7bcc8f517bac10a877a78e604ac9cfd8781e5/docs/VALIDATION_REPORT.md) · [Passing GitHub CI](https://github.com/OuterRingFoundry/CiviaIndustria/actions/runs/34096323209)

Still open: authenticated multiplayer/voice/claims/combat, representative 1080p/1440p GPU performance, mixed-direction rail junctions and realistic terrain/economy/progression playtests, followed by production operational acceptance. Software-rendered client fixtures and fake players do not establish these results.

## Version and integrity

Pack version **0.1.0-beta.1** wraps the unchanged, tested `civitas_industria-0.0.1-dev.jar`; the internal mod version intentionally remains `0.0.1-dev`. No gameplay code or world schema was changed for packaging. World schema is 3; cargo envelope is 2.

[Release metadata](release.json) · [Download SHA-256 checksums](SHA256SUMS) · [Tested source commit](https://github.com/OuterRingFoundry/CiviaIndustria/tree/f5f7bcc8f517bac10a877a78e604ac9cfd8781e5)
