"""Loopback-only UDP endpoints for concurrently running disposable test instances."""
from pathlib import Path


def isolate_voice(instance: Path, port: int):
    if not 1024 <= port <= 65535:
        raise ValueError('Fixture UDP port out of range')
    path = instance / 'config/voicechat/voicechat-server.properties'
    path.parent.mkdir(parents=True, exist_ok=True)
    lines = path.read_text().splitlines() if path.exists() else []
    keys = {'port', 'bind_address'}
    lines = [line for line in lines if line.partition('=')[0].strip() not in keys]
    path.write_text('\n'.join([*lines, f'port={port}', 'bind_address=127.0.0.1']) + '\n')
    return path.relative_to(instance).as_posix()
