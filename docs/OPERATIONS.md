For the active EC2 layout and historical artifact recovery, see [WORKSPACE.md](WORKSPACE.md).

# Reproducible development and operations

This is a DEV integration build. Do not promote it until the release matrix is complete.
Java 21 and Python 3.12+ are required for the full toolchain. Build on the designated
server; the artifact cache belongs beside the source checkout or is selected with
`-PciArtifactDirectory=/absolute/cache`.

1. `python scripts/download-pack.py --help` identifies the official exact-download
   workflow. Pins, upstream checksums, license links and sources are in `pack/mods.lock.json`.
2. `python scripts/verify-pack.py --directory /absolute/cache --side server --all-artifacts`
   verifies the downloaded combined cache and nested dependencies.
3. `./gradlew build runGameTestServer` tests the mod. `scripts/integration-matrix.py`
   runs eight isolated server profiles; logs and worlds remain in separate run directories.
4. `python scripts/assemble-pack.py --side server --artifacts /absolute/cache
   --civitas build/libs/civitas_industria-0.4.0-dev.jar --output /new/instance`
   builds and validates an exact server pack. Use `--side client` for the client pack.
   Existing destinations are refused. Obtain NeoForge 21.1.249 from its official installer;
   no third-party jar is committed or publicly redistributed by this repository.
5. `python scripts/install-runtime.py --instance /new/instance --installer /pinned/installer.jar`
   verifies the installer against `pack/runtime.lock.json`, installs NeoForge and fingerprints
   its libraries and launch scripts. Run `bash run.sh nogui` inside the assembled server.
6. `python scripts/verify-instance.py /instance` verifies jar, script and configuration
   hashes before launch. Canonical properties files are compared by key/value because Minecraft
   rewrites their comments and ordering. After a reviewed first DEV bootstrap,
   `python scripts/finalize-instance.py /instance` records generated configs and libraries.
   This is an explicit baseline change, not an integrity check. Changing a reviewed configuration requires regenerating the
   assembled manifest as a new release, not silently editing a live artifact.

The server defaults are view distance 10, simulation distance 6, maximum 50 players,
synchronous chunk writes, online authentication and no command blocks. Start with an
8 GiB heap and measure; the development server's available RAM is not a sizing target.
Keep DEV, INTEGRATION, STAGING and PRODUCTION in separate directories and use deliberate
promotion. No automatic mod updates or automatic production deployment is configured.

## Backups and restores

`scripts/backup-instance.py backup /instance /outside/backups --tier hourly` creates a
full offline archive, SHA-256 sidecar and atomic final filename. It uses the same POSIX
record lock family as Java's world session lock and refuses a running world. Stop the
server cleanly before invoking it. Symlinks require an explicit policy and are refused.
Do not place backups inside the instance. World, jars, configs, scripts and manifests
are included; diagnostic logs/crash reports are excluded. Tier retention is hourly 24,
daily 14, weekly 8 and monthly 6. Scheduling is not installed on the development host.
For a continuous production service, use a reviewed snapshot/quiescence workflow rather
than pretending a live recursive file copy is an atomic world backup.

`restore /backups/hourly-....tar.gz /new/restore` checks the checksum and extracts using
Python's safe data filter. Existing destinations are refused. Verify the restored pack,
then start it separately and check saved counts and identities before promotion.

Pregenerate only the intended staging/production world, with the pinned Chunky artifact,
a reviewed 5000–6000 block radius and planned future rail corridors. Development scripts
do not pregenerate or alter unknown worlds.

## Additional disposable acceptance fixtures

`scripts/test-railway-restart.py --output /new/railway-test` runs three dedicated-server
processes against a new copy of the full-server GameTest world/mods. Twenty real Create
train authorities travel along test graphs; saved quantities/positions and post-restart
extraction are verified. This is not a substitute for scheduled physical railway testing.
The Java hook is disabled unless the explicit `ciRailwayRestart` Gradle property is set.

`scripts/run-staging.py --output /new/staging-test` runs the scoped 10,000-decoration,
1,000-furnace, 500-cell workload. Without `--combined` it excludes clients, moving trains and live raids. With
`--combined` it adds graph-travelling trains, fake players, warehouse traffic and raids;
real clients and physical railway schedules remain outside that fixture. Both tools refuse existing outputs and leave all logs/worlds for inspection.


## Reproduce the automated DEV acceptance suite

Use Java 21 and Python 3.12+ (the designated server has Python 3.13 at
`/home/frederick/anaconda3/bin/python`). The exact artifact cache must be beside the
source checkout as `../civitas-industria-artifacts` for all existing launchers.

```sh
python scripts/validate-dev.py \
  --artifacts /data/.tmp/civitas-industria-artifacts \
  --installer /data/.tmp/civitas-industria-runtime/neoforge-21.1.249-installer.jar \
  --runtime-cache /data/.tmp/civitas-industria-builds/dev-schema3-r11/server \
  --output /data/.tmp/civitas-industria-builds/NEW-DEV-VALIDATION
```

The destination must not exist. Run only against this disposable development checkout:
smoke save/refusal checks use its reserved `run/smoke-world`. The command verifies
artifacts and tooling, builds, runs all eight GameTest profiles in fresh directories,
checks three-dimension saves/refusal, runs three railway restart fixtures and combined
staging, then assembles server/client packs and validates standalone boot/backup/restore.
`dev-validation.json` records each stage, its log, the source digest and built JAR hash.
Failures remain recorded; an automated pass leaves human acceptance gates explicitly open.

`python scripts/check-tooling.py` runs the regression suite. Standard unittest file
discovery skips the repository's hyphenated test filenames, so use this explicit runner.
The matrix requires exactly the number of required `@GameTest` methods in the current
source, successful Gradle completion and confirmed world saves. Optional adapter
assertions still skip in profiles without their mod.

Railway/staging scripts accept `--fixture /path/to/run-matrix-full-server` to consume a
fresh matrix world. `integration-matrix.py --output /new/path` retains independent
results without overwriting earlier matrix evidence. GitHub CI now downloads locked
compile dependencies and exercises tooling, domain, eight-profile and save/refusal gates.
Rendering, full distribution tests and large fixtures run on the designated server.


## Remaining acceptance procedures

The [external acceptance runbook](ACCEPTANCE_RUNBOOK.md) defines the next multiplayer,
representative GPU, mixed-direction railway and player-progression sessions, with
specific evidence requirements. These sessions remain unrun. The accepted 0.3.0-dev
server distribution is `/data/.tmp/civitas-pollution-acceptance-r4/distribution`;
use a new disposable instance for each session and preserve the accepted artifacts.


## 0.4.0 expansion development commands

The authorized build endpoint is `frederick@100.98.111.18`; the expansion checkout is
`/data/.tmp/civitas-steampunk-expansion`. Use supplied SSH authentication without
putting passwords or private keys in scripts, commits or documentation. The preserved
0.3.0 directories are historical acceptance evidence.

The focused server command is `python scripts/integration-matrix.py --artifacts
/data/.tmp/civitas-industria-artifacts --profiles core full-server --output /new/checks`.
The workbench/power checks join the existing suite in these two profiles; skipped
optional adapters are not credited as tested in the core profile.

For a **disposable copied** client instance, `./gradlew --no-daemon runClient
-PciWorkshopValidation -PciClientDir=/path/to/copied/client` loads the reserved
`ci-validation` world and constructs the explicit workshop/raider inspection scene.
This opt-in fixture alters that world, supplies test energy/materials, sends real menu
button packets, captures images and stops the client. Never point it at a player world.
Its generated fixture raiders have AI disabled for the three-role lineup. The final
segment enables one breaker and checks an actual defense-credit sabotage; the lineup
screenshots alone do not establish combat AI or balance. Rendering under Xvfb/Mesa remains
a development check, not a representative GPU measurement.
