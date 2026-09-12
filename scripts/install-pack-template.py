#!/usr/bin/env python3
"""Install the exact four-core pack into a new directory. Requires Python 3.11+ and Java 21."""
import argparse
import hashlib
import json
import shutil
import subprocess
import sys
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent

def run(name, *args):
    subprocess.run([sys.executable, str(ROOT / 'scripts' / name), *map(str, args)], check=True)

def main():
    if sys.version_info < (3, 11):
        raise SystemExit('Python 3.11 or newer is required.')
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', required=True, type=Path, help='New instance directory; must not exist')
    parser.add_argument('--side', choices=['client', 'server'], default='server')
    parser.add_argument('--cache', type=Path, default=ROOT / 'downloads')
    parser.add_argument('--mods-only', action='store_true', help='Skip server runtime installation')
    parser.add_argument('--runtime-cache', type=Path, help='Optional verified instance with this exact NeoForge runtime')
    args = parser.parse_args()
    if args.output.exists():
        raise SystemExit('Output already exists. Choose a new directory.')
    release = json.loads((ROOT / 'release.json').read_text())
    own = release['customJar']
    jar = ROOT / 'mods' / own['filename']
    if hashlib.sha256(jar.read_bytes()).hexdigest() != own['sha256']:
        raise SystemExit('Civitas JAR checksum mismatch.')
    if args.side == 'server' and not args.mods_only:
        if not shutil.which('java'):
            raise SystemExit('Install Java 21 and put java on PATH first.')
        version = subprocess.run(['java', '-version'], capture_output=True, text=True, check=True)
        if 'version "21.' not in version.stderr + version.stdout:
            raise SystemExit('Use Java 21 for this pack.')
    args.cache.mkdir(parents=True, exist_ok=True)
    # Acquire every pinned dependency, including both side-specific sets.
    run('download-pack.py', '--directory', args.cache, '--lock', ROOT / 'pack/mods.lock.json',
        '--output-lock', args.cache / 'verified-downloads.lock.json')
    run('assemble-pack.py', '--side', args.side, '--artifacts', args.cache, '--civitas', jar, '--output', args.output)
    if args.side == 'server' and not args.mods_only:
        runtime = json.loads((ROOT / 'pack/runtime.lock.json').read_text())['installer']
        installer = args.cache / runtime['filename']
        if not installer.exists():
            temporary = installer.with_suffix('.jar.part')
            request = urllib.request.Request(runtime['url'], headers={'User-Agent': 'OuterRingFoundry/CiviaIndustria'})
            with urllib.request.urlopen(request, timeout=120) as response, temporary.open('wb') as target:
                shutil.copyfileobj(response, target)
            if hashlib.sha256(temporary.read_bytes()).hexdigest() != runtime['sha256']:
                raise SystemExit('NeoForge installer checksum mismatch.')
            temporary.replace(installer)
        extra = ['--runtime-cache', args.runtime_cache] if args.runtime_cache else []
        run('install-runtime.py', '--instance', args.output, '--installer', installer, *extra)
        print('Server installed. Set memory in user_jvm_args.txt; read the Minecraft EULA,')
        print('then create eula.txt with eula=true only if you agree. Start with run.bat or bash run.sh nogui.')
    else:
        print('Mods/configuration installed. Use Minecraft 1.21.1 with NeoForge 21.1.249 and Java 21.')

if __name__ == '__main__':
    main()
