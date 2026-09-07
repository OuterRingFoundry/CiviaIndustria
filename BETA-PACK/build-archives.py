#!/usr/bin/env python3
"""Rebuild deterministic public beta archives from this directory."""
import hashlib
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent

def archive(path, entries):
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as target:
        for name, data in sorted(entries):
            entry = zipfile.ZipInfo(name, date_time=(2026, 9, 7, 0, 0, 0))
            entry.compress_type = zipfile.ZIP_DEFLATED
            entry.external_attr = 0o100644 << 16
            target.writestr(entry, data)

metadata = json.loads((ROOT / 'release.json').read_text())
boot = ROOT / 'server-bootstrap'
jar = boot / 'mods' / metadata['customJar']['filename']
assert hashlib.sha256(jar.read_bytes()).hexdigest() == metadata['customJar']['sha256']
index = json.loads((ROOT / 'modrinth.index.json').read_text())
lock = json.loads((boot / 'pack/mods.lock.json').read_text())
assert len(index['files']) == len(lock['artifacts']) == 14
assert index['dependencies'] == {'minecraft': '1.21.1', 'neoforge': '21.1.249'}
for f, a in zip(index['files'], lock['artifacts']):
    assert f['path'] == 'mods/' + a['filename']
    assert f['hashes'] == a['upstream_hashes'] and f['downloads'] == [a['url']]
    assert f['fileSize'] == a['size']
    assert f['env'] == {s: 'required' if a['side'] in ('both', s) else 'unsupported' for s in ['client', 'server']}
entries = [('modrinth.index.json', (ROOT / 'modrinth.index.json').read_bytes()),
           ('overrides/mods/' + jar.name, jar.read_bytes()),
           ('overrides/CIVIA-BETA-THIRD-PARTY.md', (ROOT / 'THIRD-PARTY.md').read_bytes())]
for file in sorted((boot / 'pack/overrides').rglob('*')):
    if file.is_file():
        relative = file.relative_to(boot / 'pack/overrides').as_posix()
        prefix = 'server-overrides/' if relative == 'server.properties' else 'overrides/'
        entries.append((prefix + relative, file.read_bytes()))
archive(ROOT / 'CiviaIndustria-0.1.0-beta.1.mrpack', entries)
entries = [('README.md', (ROOT / 'README.md').read_bytes())]
for file in sorted(boot.rglob('*')):
    if file.is_file() and '__pycache__' not in file.parts:
        entries.append((file.relative_to(ROOT).as_posix(), file.read_bytes()))
archive(ROOT / 'CiviaIndustria-0.1.0-beta.1-server.zip', entries)
files = [ROOT / 'CiviaIndustria-0.1.0-beta.1.mrpack', ROOT / 'CiviaIndustria-0.1.0-beta.1-server.zip', jar]
(ROOT / 'SHA256SUMS').write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest() + '  ' + p.relative_to(ROOT).as_posix() + '\n' for p in files))
print((ROOT / 'SHA256SUMS').read_text())
