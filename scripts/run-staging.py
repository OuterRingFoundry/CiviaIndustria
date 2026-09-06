#!/usr/bin/env python3
"""Run the explicitly scoped synthetic workload in a new disposable server directory."""
import argparse,json,shutil,subprocess
from pathlib import Path
from fixture_config import isolate_voice
from fixture_process import run
ROOT=Path(__file__).resolve().parents[1]
def main():
 p=argparse.ArgumentParser();p.add_argument('--output',required=True,type=Path);p.add_argument('--combined',action='store_true');a=p.parse_args();a.output=a.output.resolve()
 if a.output.exists():raise ValueError('Staging output must be new')
 fixture=ROOT/'run-matrix-full-server'
 if not (fixture/'world/level.dat').exists():raise ValueError('Run the full-server GameTest profile first')
 a.output.mkdir(parents=True);shutil.copytree(fixture/'world',a.output/'world');shutil.copytree(fixture/'mods',a.output/'mods');shutil.copytree(ROOT/'pack/overrides',a.output,dirs_exist_ok=True)
 isolate_voice(a.output,24460)
 (a.output/'eula.txt').write_text('eula=true\n');(a.output/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25590\nlevel-name=world\nonline-mode=true\nview-distance=4\nsimulation-distance=4\nmax-players=1\nsync-chunk-writes=true\n')
 command=['./gradlew','--no-daemon','runServer','-PciStaging',f'-PciServerDir={a.output}']
 if a.combined:command.append('-PciStagingCombined')
 with (a.output/'staging.log').open('w') as log:code=run(command,ROOT,log,900)
 text=(a.output/'staging.log').read_text()
 if code or 'CIVITAS SYNTHETIC STAGING COMPLETE' not in text or 'All dimensions are saved' not in text:raise RuntimeError('Staging failed; retain '+str(a.output))
 report=json.loads((a.output/'staging-report.json').read_text())
 if report['measured_ticks']!=1200:raise ValueError('Incomplete staging measurement')
 print(json.dumps(report,indent=2))
if __name__=='__main__':main()
