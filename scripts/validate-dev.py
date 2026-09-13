#!/usr/bin/env python3
"""Run the automated DEV acceptance suite; never promote a pack or certify human gates."""
import argparse
import datetime
import hashlib
import json
import subprocess
import sys
import time
from pathlib import Path
from pack_manifest import built_mod_jar

ROOT = Path(__file__).resolve().parents[1]
MANUAL_GATES = [
    'Authenticated multiplayer, voice, shared claims and combat with real clients',
    'Representative GPU dense-city rendering at 1080p and 1440p',
    'Mixed-direction junction traffic, real terrain/mining and player economy playtests',
    'Combined staging with authenticated clients and physical scheduled traffic',
    'Deliberate production promotion, intended-world pregeneration and backup scheduling',
]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--artifacts', required=True, type=Path)
    parser.add_argument('--installer', required=True, type=Path)
    parser.add_argument('--runtime-cache', type=Path)
    parser.add_argument('--output', required=True, type=Path)
    args = parser.parse_args()
    if sys.version_info < (3, 12):
        parser.error('Python 3.12+ is required for the full backup/restore suite')
    args.output = args.output.resolve()
    args.artifacts = args.artifacts.resolve()
    args.installer = args.installer.resolve()
    if args.output.exists():
        parser.error('Output must be new; previous evidence is never overwritten')
    if not args.artifacts.is_dir() or not args.installer.is_file():
        parser.error('Existing artifact cache and pinned NeoForge installer are required')
    # Compile adapters use this cache across all fixture launchers.
    expected_cache = (ROOT.parent / 'civitas-industria-artifacts').resolve()
    if args.artifacts != expected_cache:
        parser.error(f'The full suite currently requires the shared adapter cache at {expected_cache}')
    args.output.mkdir(parents=True)
    report = {'status': 'running', 'started_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(),
              'scope': 'Automated DEV tests only; production acceptance remains open',
              'source_sha256': source_digest(), 'stages': [], 'open_acceptance_gates': MANUAL_GATES}
    result_path = args.output / 'dev-validation.json'

    def save():
        result_path.write_text(json.dumps(report, indent=2) + '\n')

    def stage(name, command, timeout=1800):
        row = {'name': name, 'status': 'running', 'command': list(map(str, command)),
               'log': str(args.output / (name + '.log'))}
        report['stages'].append(row)
        save()
        started = time.monotonic()
        try:
            with Path(row['log']).open('w') as log:
                subprocess.run(row['command'], cwd=ROOT, stdin=subprocess.DEVNULL,
                               stdout=log, stderr=subprocess.STDOUT, check=True, timeout=timeout)
            row['status'] = 'passed'
            print(name, 'PASS', flush=True)
        except BaseException as error:
            row['status'] = 'failed'
            row['error'] = str(error)
            report['status'] = 'failed'
            raise
        finally:
            row['seconds'] = round(time.monotonic() - started, 2)
            save()

    def script(name, filename, *arguments, timeout=1800):
        stage(name, [sys.executable, ROOT / 'scripts' / filename, *arguments], timeout)

    save()
    script('verify-artifacts', 'verify-pack.py', '--directory', args.artifacts, '--side', 'client', '--all-artifacts')
    script('tooling-tests', 'check-tooling.py')
    script('content', 'validate-content.py')
    stage('clean-build', ['./gradlew', '--no-daemon', 'clean', 'compileJava', 'test', 'build'])
    jar = built_mod_jar(ROOT)
    report['civitas_jar_sha256'] = hashlib.sha256(jar.read_bytes()).hexdigest()
    script('integration-matrix', 'integration-matrix.py', '--artifacts', args.artifacts,
           '--output', args.output / 'matrix')
    script('save-write', 'smoke-server.py', '--runtime-write')
    script('save-read', 'smoke-server.py', '--runtime-read')
    script('save-refusal', 'check-save-refusal.py')
    fixture = args.output / 'matrix/run-matrix-full-server'
    for name, filename in [('railway-restart', 'test-railway-restart.py'),
                           ('physical-route', 'test-physical-route.py'),
                           ('shared-railway', 'test-shared-railway.py')]:
        script(name, filename, '--fixture', fixture, '--output', args.output / name)
    script('combined-staging', 'run-staging.py', '--combined', '--fixture', fixture,
           '--output', args.output / 'combined-staging')
    cache = ['--runtime-cache', args.runtime_cache.resolve()] if args.runtime_cache else []
    script('distribution', 'validate-release.py', '--artifacts', args.artifacts,
           '--installer', args.installer, '--world', ROOT / 'run/smoke-world',
           '--output', args.output / 'distribution', *cache)
    if source_digest() != report['source_sha256']:
        report['status'] = 'failed'
        save()
        raise RuntimeError('Source changed during validation; results cannot certify a single build')
    report['status'] = 'automated-dev-passed; external acceptance open'
    report['finished_utc'] = datetime.datetime.now(datetime.timezone.utc).isoformat()
    save()
    print('PASS: automated DEV suite; external acceptance gates remain open', flush=True)


def source_digest():
    digest = hashlib.sha256()
    paths = [p for base in ('src', 'scripts', '.github', 'pack/overrides') for p in (ROOT / base).rglob('*')
             if p.is_file() and '__pycache__' not in p.parts]
    paths += [ROOT / name for name in ('build.gradle', 'settings.gradle', 'gradle.properties',
                                      'pack/mods.lock.json', 'pack/runtime.lock.json')]
    for path in sorted(paths):
        digest.update(path.relative_to(ROOT).as_posix().encode() + b'\0')
        digest.update(hashlib.sha256(path.read_bytes()).digest())
    return digest.hexdigest()


if __name__ == '__main__':
    main()
