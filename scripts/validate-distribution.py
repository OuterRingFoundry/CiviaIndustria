#!/usr/bin/env python3
"""Smoke-test a shipped server JAR and exact runtime in a new isolated copy, never Gradle."""
import argparse,hashlib,json,queue,shutil,subprocess,threading,time
from pathlib import Path
from fixture_config import isolate_voice
from pack_manifest import properties
ROOT=Path(__file__).resolve().parents[1]
def main():
 p=argparse.ArgumentParser();p.add_argument('--instance',required=True,type=Path);p.add_argument('--output',required=True,type=Path);p.add_argument('--world',type=Path);a=p.parse_args()
 if a.output.exists():raise ValueError('Validation destination must be new')
 if a.world and (a.instance/'world').exists():raise ValueError('Existing world in instance; omit --world to validate it')
 shutil.copytree(a.instance,a.output)
 if a.world:shutil.copytree(a.world,a.output/'world')
 if not (a.output/'world/level.dat').exists():raise ValueError('A saved validation world is required')
 (a.output/'eula.txt').write_text('eula=true\n');(a.output/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25589\nlevel-name=world\nonline-mode=true\nview-distance=4\nsimulation-distance=4\nmax-players=1\nsync-chunk-writes=true\n')
 (a.output/'user_jvm_args.txt').write_text('-Xms2G\n-Xmx8G\n-XX:ActiveProcessorCount=8\n')
 voice=isolate_voice(a.output,24459)
 state=json.loads((a.output/'pack-state.json').read_text())
 for name in ('server.properties','user_jvm_args.txt','eula.txt',voice):
  if name in state.get('properties',{}):state['properties'][name]=properties(a.output/name)
  else:state['files'][name]=hashlib.sha256((a.output/name).read_bytes()).hexdigest()
 (a.output/'pack-state.json').write_text(json.dumps(state,indent=2)+'\n')
 process=subprocess.Popen(['bash','run.sh','nogui'],cwd=a.output,stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,bufsize=1)
 lines=queue.Queue()
 def read():
  for line in process.stdout:lines.put(line)
  lines.put(None)
 threading.Thread(target=read,daemon=True).start();deadline=time.monotonic()+300;ready=version=saved=False;step=0
 commands=[('ci version','| data 3'),*[(f'execute in minecraft:{d} run ci env inspect -15626 -15626','AQI 123.0') for d in ['overworld','the_nether','the_end']]]
 try:
  with (a.output/'distribution-validation.log').open('w') as log:
   while time.monotonic()<deadline:
    try:line=lines.get(timeout=1)
    except queue.Empty:
     if process.poll() is not None:break
     continue
    if line is None:break
    log.write(line);log.flush()
    if 'All dimensions are saved' in line:saved=True
    if not ready and 'Done (' in line:ready=True;process.stdin.write(commands[0][0]+'\n');process.stdin.flush()
    if ready and step<len(commands) and commands[step][1] in line:
     step+=1;process.stdin.write(('stop' if step==len(commands) else commands[step][0])+'\n');process.stdin.flush()
   code=process.wait(timeout=30)
  if code!=0 or not ready or step!=len(commands) or not saved:raise RuntimeError('Distribution validation failed; retained log/world')
  print('PASS: standalone distribution startup, three dimension saves, exact stored pollutants, clean stop')
 finally:
  if process.poll() is None:
   process.stdin.write('stop\n');process.stdin.flush();process.wait(timeout=30)
if __name__=='__main__':main()
