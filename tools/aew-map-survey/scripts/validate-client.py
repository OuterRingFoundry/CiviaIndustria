#!/usr/bin/env python3
"""Disposable localhost server with NO map or survey mod; Xaero and survey on client.

Run on the designated build server. Existing AEW libraries/JARs are only read.
The output directory must not already exist. No production server/world is used.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import signal
import subprocess
import time
import uuid

parser = argparse.ArgumentParser()
parser.add_argument('--workspace', type=Path, required=True)
parser.add_argument('--aew', type=Path, required=True)
parser.add_argument('--output', type=Path, required=True)
args = parser.parse_args()
out = args.output.resolve()
out.mkdir()
server_dir, client_dir = out / 'server', out / 'client'
server_dir.mkdir()
(server_dir / 'mods').mkdir()
(client_dir / 'mods').mkdir(parents=True)
(client_dir / 'config').mkdir()
(server_dir / 'libraries').symlink_to(args.aew.resolve() / 'libraries', target_is_directory=True)
(server_dir / 'eula.txt').write_text('eula=true\n')
(server_dir / 'server.properties').write_text('\n'.join([
    'server-ip=127.0.0.1', 'server-port=25594', 'online-mode=false',
    'view-distance=8', 'simulation-distance=5', 'spawn-protection=0',
    'gamemode=creative', 'force-gamemode=true', 'level-type=minecraft:flat',
    'generator-settings={"biome":"minecraft:plains","layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}]}',
    'generate-structures=false', 'sync-chunk-writes=true', 'max-players=2',
    'enable-rcon=false', 'enable-query=false', 'max-tick-time=60000', '']) )
player_uuid = str(uuid.UUID(bytes=hashlib.md5(b'OfflinePlayer:SurveyTester').digest(), version=3))
(server_dir / 'ops.json').write_text(json.dumps([{'uuid': player_uuid, 'name': 'SurveyTester', 'level': 4, 'bypassesPlayerLimit': False}]))
(client_dir / 'options.txt').write_text('renderDistance:8\nsimulationDistance:5\nguiScale:3\nenableVsync:false\nmaxFps:30\ngraphicsMode:0\nskipMultiplayerWarning:true\n')
(client_dir / 'config/aew-map-survey-recovery.txt').write_text('1\n2\n8\n')
dependencies = {}
for name in [' xaeroworldmap-neoforge-1.21.1-1.45.0.jar', ' xaerominimap-neoforge-1.21.1-26.4.2.jar']:
    source = args.aew / 'mods' / name
    shutil.copy2(source, client_dir / 'mods' / name.strip())
    dependencies[name.strip()] = hashlib.sha256(source.read_bytes()).hexdigest()
project = args.workspace.resolve() / 'tools/aew-map-survey'
processes = []

def stop(process):
    if process.poll() is None:
        os.killpg(process.pid, signal.SIGTERM)
        try:
            process.wait(timeout=15)
        except subprocess.TimeoutExpired:
            os.killpg(process.pid, signal.SIGKILL)
            process.wait()

def await_text(file, marker, process, timeout):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        if file.exists() and marker in file.read_text(errors='replace'):
            return
        if process.poll() is not None:
            raise RuntimeError(f'Process exited before {marker}: {process.returncode}')
        time.sleep(1)
    raise TimeoutError(marker)

try:
    with (out / 'server-console.log').open('w') as server_log:
        server = subprocess.Popen(['java', '-Xms512M', '-Xmx2G', '@libraries/net/neoforged/neoforge/21.1.248/unix_args.txt', '--nogui'],
                                  cwd=server_dir, stdin=subprocess.PIPE, stdout=server_log, stderr=subprocess.STDOUT,
                                  text=True, start_new_session=True)
        processes.append(server)
        await_text(out / 'server-console.log', 'Done (', server, 150)
        for command in ['gamerule spawnRadius 0', 'gamerule doMobSpawning false', 'gamerule doDaylightCycle false',
                        'setworldspawn 0 -60 0', 'forceload add 80 0']:
            server.stdin.write(command + '\n')
        server.stdin.flush()
        # Prepare a known distant block; wait for the fixture chunk, then remove its ticket.
        deadline = time.monotonic() + 30
        while 'Changed the block at 80, -60, 0' not in (out / 'server-console.log').read_text(errors='replace'):
            if time.monotonic() >= deadline or server.poll() is not None:
                raise TimeoutError('Prepare distant fixture block')
            server.stdin.write('execute positioned 80 -60 0 if loaded ~ ~ ~ run setblock ~ ~ ~ minecraft:obsidian\n')
            server.stdin.flush()
            time.sleep(1)
        server.stdin.write('forceload remove 80 0\n')
        server.stdin.flush()
        with (out / 'client-console.log').open('w') as client_log:
            client = subprocess.Popen(['xvfb-run', '-a', '-s', '-screen 0 1280x720x24', str(args.workspace.resolve() / 'gradlew'),
                                       '-p', str(project), 'runClient', '-PsurveyValidation', f'-PsurveyRunDir={client_dir}'],
                                      cwd=args.workspace, stdout=client_log, stderr=subprocess.STDOUT,
                                      env={**os.environ, 'LIBGL_ALWAYS_SOFTWARE': '1'}, start_new_session=True)
            processes.append(client)
            await_text(out / 'client-console.log', 'SURVEY CLIENT PASS:', client, 300)
            if client.wait(timeout=60) != 0:
                raise RuntimeError('Client process failed after marker')
        server.stdin.write('stop\n')
        server.stdin.flush()
        if server.wait(timeout=60) != 0:
            raise RuntimeError('Server did not stop cleanly')
    map_files = [p for p in (client_dir / 'xaero/world-map').rglob('*') if p.is_file()]
    if not any(p.suffix == '.zip' for p in map_files):
        raise AssertionError('Xaero did not persist any map region archive')
    result = {'passed': True, 'serverMods': [], 'clientDependencies': dependencies,
              'mapFiles': [str(p.relative_to(client_dir)) for p in map_files],
              'screenshots': [p.name for p in (client_dir / 'screenshots').glob('*.png')],
              'limits': 'Localhost offline validation, Xvfb/Mesa. No production AEW/GPU benchmark or distant cache freshness claim.'}
    (out / 'results.json').write_text(json.dumps(result, indent=2) + '\n')
    print(json.dumps(result), flush=True)
finally:
    if 'server' in locals() and server.poll() is None:
        try:
            server.stdin.write('stop\n')
            server.stdin.flush()
            server.wait(timeout=30)
        except (BrokenPipeError, subprocess.TimeoutExpired):
            pass
    for process in reversed(processes):
        stop(process)
