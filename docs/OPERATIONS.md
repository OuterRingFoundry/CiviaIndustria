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
   runs six isolated server profiles; logs and worlds remain in separate run directories.
4. `python scripts/assemble-pack.py --side server --artifacts /absolute/cache
   --civitas build/libs/civitas_industria-0.0.1-dev.jar --output /new/instance`
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
1,000-furnace, 500-cell workload. It deliberately excludes clients, moving trains and
live raids. Both tools refuse existing outputs and leave all logs/worlds for inspection.
