#!/usr/bin/env python3
"""Distribute our mod and exact upstream download recipe without republishing third-party JARs."""
import argparse,hashlib,json,shutil,zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--civitas',required=True,type=Path);p.add_argument('--output',required=True,type=Path);a=p.parse_args();a.output.mkdir(parents=True,exist_ok=False)
readme='''Civitas Industria 0.5.0 development - focused pack
Minecraft 1.21.1 / NeoForge 21.1.249 / Java 21 / Python 3.11+

Extract into a NEW empty client or dedicated-server instance directory.
Install NeoForge 21.1.249 for that instance, then run from this directory:

  python scripts/download-pack.py --directory mods --output-lock downloaded-mods.lock.json
  python scripts/verify-pack.py --directory mods --side client

Use --side server for a dedicated server. Install the same seven top-level
mods on both sides. Upstream JARs download from pinned official URLs with
SHA-256 verification. Civilis remains unmodified and external.
Do not extract over an existing world or mixed modpack. This installer does
not remove historical mods, alter saves, or accept the Minecraft EULA.

Included families: Create, Create Crafts & Additions, Immersive Engineering,
Advanced Chimneys + ForgeEndertech, Civillis, and Civitas Industria.

Craft a Market counter: deposit 1 diamond for 100 crowns, redeem 100 crowns
for 1 diamond. Fund donates 100 crowns to the shared market treasury, allowing
it to buy goods; sell goods to seed stock, then buy from that finite stock.
The only tradable money is crowns. Daily prices respond modestly to trade
imbalance and deterministic market variation; no random money is created.
/ci money shows balances; /ci money pay <online player> <amount> transfers.
Right-click storage or workbenches for inventory menus. Shift-click transfers.
Industrial raids have a warning bar, three waves, and recovery/retreat.

Development build: consult docs/REDESIGN_STATUS.md for actual validation scope.
'''
files={f'mods/{a.civitas.name}':a.civitas.read_bytes(),'INSTALL.txt':readme.encode()}
for name in ['scripts/download-pack.py','scripts/verify-pack.py','scripts/pack_manifest.py','pack/downloads.lock.json','pack/mods.lock.json','docs/REDESIGN_STATUS.md']:
 files[name]=(ROOT/name).read_bytes()
for f in (ROOT/'pack/overrides').rglob('*'):
 if f.is_file() and f.name!='server.properties':files[str(f.relative_to(ROOT/'pack/overrides'))]=f.read_bytes()
manifest={n:hashlib.sha256(b).hexdigest() for n,b in sorted(files.items())}
files['SHA256.json']=(json.dumps(manifest,indent=2)+'\n').encode()
archive=a.output/'civitas-industria-0.5.0-focused-installer.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
 for name,content in files.items():z.writestr(name,content)
print(archive,hashlib.sha256(archive.read_bytes()).hexdigest())
