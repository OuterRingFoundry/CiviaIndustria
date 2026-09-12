# Residence and creative inventory continuation — 2026-09-11

Source commit: `ce7d9a3` on `continue-planned-work`.
Local Git checkout: `/home/ubuntu/codexproj/CiviaIndustria`.
Editable build workspace: `frederick@100.98.111.18:/data/.tmp/civitas-residence-creative`.
The server holds the source, resources, Gradle files and test scripts used by this build.
Git history is retained in the local checkout; this handoff identifies its source revision.

## Artifact and behavior

Compiled mod: `build/libs/civitas_industria-0.4.0-dev.jar` in the server workspace.
SHA-256: `dc6fa15efd41558a16817c44415ad2f2222514ad6c59c75f61d37e0296f555e6`.

The Civitas Industry creative tab has a workbench icon, search field and machines-first
listing. All registered inventory items also appear in the relevant vanilla creative
categories. There are 39 Civitas items with Create installed and 37 without it; the
motor and dynamo require Create. Residence registration uses `/ci residence declare`, `status` and `withdraw`;
it is a command feature and adds no block or item.

Residence records use a separate overworld-owned version-1 file. World schema 3 is
unchanged. Shutdown save/unload callbacks now use only an existing runtime so rejected
saved data is not loaded again during shutdown. Creative content is assembled when
tabs rebuild, with no tick polling. Residence activity samples only online players
once per 20 ticks. No historical cell scans or new network payloads were added.

## Evidence and reproduction

Final build: `final-build-r3.log` (101 domain checks).
Native profiles: `/data/.tmp/civitas-residence-creative-checks-r3` (42 required tests
in each of core and full-server).
Process fixture: `/data/.tmp/civitas-residence-restart-r5` (all seven phases passed,
including three byte-preserving startup refusals and restored-file boot).
Client: `/data/.tmp/civitas-creative-client-r3`; log `creative-client-r3.log` in the
build workspace, and three PNG captures in the client directory's `screenshots`.
Real typed search produced four precision matches and two rotation matches, excluding
unrelated items; creative acquisition reached the server inventory. The client uses
Xvfb/Mesa at 1280x720; it is not a GPU or multiplayer benchmark.

Build in the server workspace:

```sh
./gradlew --no-daemon --console=plain build
```

Use new disposable output directories for native/process tests:

```sh
python3 scripts/integration-matrix.py --artifacts /data/.tmp/civitas-industria-artifacts --profiles core full-server --output /data/.tmp/civitas-new-native-checks
python3 scripts/test-residence-restart.py --fixture /data/.tmp/civitas-new-native-checks/run-matrix-core --output /data/.tmp/civitas-new-residence-restart
```

For client checks, copy the known client fixture into a new disposable directory
with its `saves/ci-validation` world, then use:

```sh
LIBGL_ALWAYS_SOFTWARE=1 xvfb-run -a -s '-screen 0 1280x720x24' ./gradlew --no-daemon runClient -PciCreativeValidation -PciClientDir=/data/.tmp/civitas-new-client-check
```

The opt-in fixture changes the disposable player's game mode/inventory and exits the
client. Do not point it at a player or production world. Previous test attempts remain
in their original directories, including the interrupted restart r1 that exposed the
shutdown retry and the passing r2 before the final search-field width correction.

## Further work

Paid civic service depots (supply inventory, coverage, discounts, GUI/models), funded
physical contracts and district services remain pending. No production promotion,
multiplayer economy, representative GPU, railway or broad performance acceptance is
claimed by this update. Credentials are not stored in the repository or this handoff.

Build-input SHA-256 (source, scripts, overrides and pinned build/lock files):
`5a77cf428afd9410cac2bd7f0927f82d9fce0a26fb8c94c56e8467a679384744`. The local/server digest comparison passed.
