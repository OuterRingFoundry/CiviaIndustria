#!/usr/bin/env python3
"""Two physical scheduled trains, with a real server restart mid-route."""
import argparse,json,shutil
from fixture_process import run
from pathlib import Path
from fixture_config import isolate_voice
ROOT=Path(__file__).resolve().parents[1]
def main():
 p=argparse.ArgumentParser();p.add_argument('--output',required=True,type=Path);p.add_argument('--fixture',type=Path,default=ROOT/'run-matrix-full-server');a=p.parse_args();a.output=a.output.resolve()
 if a.output.exists():raise ValueError('Railway test directory must be new')
 fixture=a.fixture.resolve()
 a.output.mkdir(parents=True);shutil.copytree(fixture/'world',a.output/'world');shutil.copytree(fixture/'mods',a.output/'mods');shutil.copytree(ROOT/'pack/overrides',a.output,dirs_exist_ok=True)
 isolate_voice(a.output,24461)
 (a.output/'eula.txt').write_text('eula=true\n');(a.output/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25591\nlevel-name=world\nonline-mode=true\nview-distance=4\nsimulation-distance=4\nmax-players=1\n')
 for phase in ('write','read','verify'):
  logfile=a.output/(phase+'.log')
  with logfile.open('w') as log:code=run(['./gradlew','--no-daemon','runServer',f'-PciRailwayRoute={phase}',f'-PciServerDir={a.output}'],ROOT,log,600)
  text=logfile.read_text()
  if code or f'CIVITAS PHYSICAL ROUTE PASS: phase={phase}, trains=2' not in text or 'All dimensions are saved' not in text or 'BUILD SUCCESSFUL' not in text:raise RuntimeError('Railway phase failed; preserve '+str(logfile))
  print(phase,'PASS',flush=True)
 report={'scope':'Fixed mine stockpiles physically loaded into two initially empty Create trains, autonomous schedules on 2140-block rail routes; process restart in transit; physical interfaces, commissioned factories and city warehouses; final restart conservation','trains':2,'processes':3,'output':str(a.output),'passed':True}
 (a.output/'physical-route-results.json').write_text(json.dumps(report,indent=2)+'\n')
if __name__=='__main__':main()
