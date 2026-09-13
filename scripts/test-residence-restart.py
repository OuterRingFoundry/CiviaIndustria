#!/usr/bin/env python3
"""Isolated residence restart and damaged-file startup-refusal acceptance."""
import argparse
import gzip
import hashlib
import json
import shutil
import struct
from pathlib import Path
from fixture_process import run

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--output', required=True, type=Path)
    parser.add_argument('--fixture', required=True, type=Path, help='Successful core GameTest directory')
    args = parser.parse_args()
    output = args.output.resolve()
    fixture = args.fixture.resolve()
    output.mkdir(parents=True, exist_ok=False)
    shutil.copytree(fixture / 'world', output / 'world')
    shutil.copytree(fixture / 'mods', output / 'mods')
    (output / 'eula.txt').write_text('eula=true\n')
    (output / 'server.properties').write_text(
        'server-ip=127.0.0.1\nserver-port=25593\nlevel-name=world\n'
        'online-mode=true\nview-distance=2\nsimulation-distance=2\nmax-players=1\n')
    report = {'scope': 'Isolated core server; residence identity, replacement and damaged-file refusal',
              'processes': [], 'output': str(output)}

    def launch(name, phase, refusal=False):
        logfile = output / (name + '.log')
        with logfile.open('w') as log:
            code = run(['./gradlew', '--no-daemon', '--console=plain', 'runServer',
                        f'-PciResidenceRestart={phase}', f'-PciServerDir={output}'], ROOT, log, 240)
        text = logfile.read_text()
        if refusal:
            passed = ('Residence data was not loaded; refusing to overwrite' in text
                      and 'Encountered an unexpected exception' in text
                      and 'Done (' not in text and 'CIVITAS RESIDENCE RESTART PASS' not in text)
        else:
            passed = (code == 0 and f'CIVITAS RESIDENCE RESTART PASS: phase={phase}, homes=1' in text
                      and 'All dimensions are saved' in text and 'BUILD SUCCESSFUL' in text
                      and 'Encountered an unexpected exception' not in text)
        passed = passed and 'Exception stopping the server' not in text
        if not passed:
            raise RuntimeError(f'{name}: failed; preserve {logfile}')
        record = {'name': name, 'exit': code, 'log': str(logfile),
                  'log_sha256': hashlib.sha256(logfile.read_bytes()).hexdigest(), 'passed': passed}
        report['processes'].append(record)
        print(name, 'PASS', flush=True)

    for phase in ('write', 'read', 'verify'):
        launch(phase, phase)
    target = output / 'world/data/civitas_residences.dat'
    original = target.read_bytes()
    raw = gzip.decompress(original)
    field = b'\x03\x00\x0bdataVersion' + struct.pack('>i', 1)
    if raw.count(field) != 1:
        raise AssertionError('Expected one version-1 residence envelope')
    cases = {'future': gzip.compress(raw.replace(field, field[:-4] + struct.pack('>i', 99))),
             'truncated': original[:16]}
    # The binary snapshot has a second, independent schema; mismatch must also refuse startup.
    magic = struct.pack('>ii', 0x43495253, 1)
    if raw.count(magic) != 1:
        raise AssertionError('Expected one residence snapshot header')
    cases['mismatch'] = gzip.compress(raw.replace(magic, struct.pack('>ii', 0x43495253, 99)))
    try:
        for name, damaged in cases.items():
            target.write_bytes(damaged)
            launch('refuse-' + name, 'verify', refusal=True)
            if target.read_bytes() != damaged:
                raise AssertionError(f'{name}: original damaged bytes changed')
            report['processes'][-1]['damaged_bytes_preserved'] = True
    finally:
        target.write_bytes(original)
        if target.read_bytes() != original:
            raise AssertionError('Failed to restore disposable residence file')
    # Verify the restored bytes still load through an actual server startup.
    marker = output / 'residence-fixture.json'
    checkpoint = json.loads(marker.read_text())
    checkpoint['phase'] = 'read'
    marker.write_text(json.dumps(checkpoint))
    launch('restored', 'verify')
    report['original_residence_sha256'] = hashlib.sha256(original).hexdigest()
    (output / 'residence-restart-results.json').write_text(json.dumps(report, indent=2) + '\n')


if __name__ == '__main__':
    main()
