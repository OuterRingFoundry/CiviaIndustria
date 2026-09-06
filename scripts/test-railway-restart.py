#!/usr/bin/env python3
"""Three actual server processes: save 20 carriage authorities, travel/extract, verify again."""
import argparse,json,os,shutil,signal,subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def main():
 p=argparse.ArgumentParser();p.add_argument('--output',required=True,type=Path);a=p.parse_args();a.output=a.output.resolve()
 if a.output.exists():raise ValueError('Railway test directory must be new')
 fixture=ROOT/'run-matrix-full-server'
 a.output.mkdir(parents=True);shutil.copytree(fixture/'world',a.output/'world');shutil.copytree(fixture/'mods',a.output/'mods');shutil.copytree(ROOT/'pack/overrides',a.output,dirs_exist_ok=True)
 (a.output/'eula.txt').write_text('eula=true\n');(a.output/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25591\nlevel-name=world\nonline-mode=true\nview-distance=4\nsimulation-distance=4\nmax-players=1\n')
 for phase in ('write','read','verify'):
  logfile=a.output/(phase+'.log')
  with logfile.open('w') as log:
   process=subprocess.Popen(['./gradlew','--no-daemon','runServer',f'-PciRailwayRestart={phase}',f'-PciServerDir={a.output}'],cwd=ROOT,stdin=subprocess.DEVNULL,stdout=log,stderr=subprocess.STDOUT,start_new_session=True)
   try:code=process.wait(timeout=300)
   except subprocess.TimeoutExpired:
    os.killpg(process.pid,signal.SIGTERM)
    try:process.wait(timeout=20)
    except subprocess.TimeoutExpired:os.killpg(process.pid,signal.SIGKILL);process.wait()
    raise

  text=logfile.read_text()
  if code or f'CIVITAS RAILWAY RESTART PASS: phase={phase}, trains=20' not in text or 'All dimensions are saved' not in text or 'BUILD SUCCESSFUL' not in text:raise RuntimeError('Railway phase failed; preserve '+str(logfile))
  print(phase,'PASS',flush=True)
 report={'scope':'Twenty actual Create train/carriage authorities; API graph travel, not scheduled physical rails','trains':20,'processes':3,'travel_blocks_each':2048,'initial_count_each':'5000000000 + train index','extracted_each_after_first_restart':64,'final_count_and_position_verified_after_second_restart':True,'output':str(a.output)}
 (a.output/'railway-results.json').write_text(json.dumps(report,indent=2)+'\n')
if __name__=='__main__':main()
