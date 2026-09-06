#!/usr/bin/env python3
"""Explicitly fingerprint a reviewed DEV instance after its first successful bootstrap."""
import argparse
from pathlib import Path
from pack_manifest import fingerprint
p=argparse.ArgumentParser();p.add_argument('instance',type=Path);a=p.parse_args()
if not (a.instance/'mods.lock.json').exists():raise SystemExit('An assembled instance is required')
state=fingerprint(a.instance);print(f"Fingerprinted {len(state['files'])} immutable files and {len(state['properties'])} properties files")
