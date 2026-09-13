#!/usr/bin/env python3
"""Isolated economy restart and damaged-file startup-refusal acceptance."""
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
    report = {'scope': 'Isolated core server; economy balances, reserves, treasury, stock, prices and damaged-file refusal',
              'processes': [], 'output': str(output)}

    def launch(name, phase, refusal=False):
        logfile = output / (name + '.log')
        with logfile.open('w') as log:
            code = run(['./gradlew', '--no-daemon', '--console=plain', 'runServer',
                        f'-PciEconomyRestart={phase}', f'-PciServerDir={output}'], ROOT, log, 240)
        text = logfile.read_text()
        if refusal:
            passed = ('Economy data was not loaded; refusing to overwrite' in text
                      and 'Encountered an unexpected exception' in text
                      and 'Done (' not in text and 'CIVITAS ECONOMY RESTART PASS' not in text)
        else:
            passed = (code == 0 and f'CIVITAS ECONOMY RESTART PASS: phase={phase}, reserve=' in text
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
    target = output / 'world/data/civitas_economy.dat'
    original = target.read_bytes()
    raw = gzip.decompress(original)
    field = b'\x03\x00\x0bdataVersion' + struct.pack('>i', 1)
    if raw.count(field) != 1:
        raise AssertionError('Expected one version-1 economy envelope')
    cases = {'future': gzip.compress(raw.replace(field, field[:-4] + struct.pack('>i', 99))),
             'truncated': original[:16]}
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
            raise AssertionError('Failed to restore disposable economy file')
    # Verify the restored bytes still load through an actual server startup.
    launch('restored', 'verify')
    report['original_economy_sha256'] = hashlib.sha256(original).hexdigest()
    (output / 'economy-restart-results.json').write_text(json.dumps(report, indent=2) + '\n')


if __name__ == '__main__':
    main()
