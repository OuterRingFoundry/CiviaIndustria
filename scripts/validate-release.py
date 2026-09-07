#!/usr/bin/env python3
"""Assemble, boot, back up, restore and reboot a new disposable DEV release candidate."""
import argparse,datetime,hashlib,json,subprocess,sys
from pathlib import Path
if sys.version_info<(3,11):raise SystemExit('Release validation requires Python 3.11+ (tomllib); no output directory created')
ROOT=Path(__file__).resolve().parents[1]
def main():
 p=argparse.ArgumentParser();p.add_argument('--artifacts',required=True,type=Path);p.add_argument('--installer',required=True,type=Path);p.add_argument('--runtime-cache',type=Path);p.add_argument('--world',required=True,type=Path);p.add_argument('--output',required=True,type=Path);a=p.parse_args()
 if a.output.exists():raise ValueError('Release destination must be new')
 if not (a.world/'level.dat').exists():raise ValueError('Use the three-dimension smoke-test world')
 a.output.mkdir(parents=True);jar=ROOT/'build/libs/civitas_industria-0.0.1-dev.jar';results=[]
 def run(stage,script,*args):
  with (a.output/(stage+'.log')).open('w') as log:
   subprocess.run([sys.executable,str(ROOT/'scripts'/script),*map(str,args)],cwd=ROOT,stdin=subprocess.DEVNULL,stdout=log,stderr=subprocess.STDOUT,check=True)
  results.append(stage);print(stage,'PASS',flush=True)
 for side in ('server','client'):
  run('assemble-'+side,'assemble-pack.py','--side',side,'--artifacts',a.artifacts,'--civitas',jar,'--output',a.output/side)
 cache_args=['--runtime-cache',a.runtime_cache] if a.runtime_cache else []
 run('install-runtime','install-runtime.py','--instance',a.output/'server','--installer',a.installer,*cache_args)
 run('verify-assembled','verify-instance.py',a.output/'server')
 run('standalone-boot','validate-distribution.py','--instance',a.output/'server','--world',a.world,'--output',a.output/'validated-server')
 run('finalize-dev','finalize-instance.py',a.output/'validated-server')
 run('verify-bootstrapped','verify-instance.py',a.output/'validated-server')
 run('backup','backup-instance.py','backup',a.output/'validated-server',a.output/'backups')
 archive=next((a.output/'backups').glob('hourly-*.tar.gz'))
 run('restore','backup-instance.py','restore',archive,a.output/'restored')
 run('verify-restored','verify-instance.py',a.output/'restored')
 run('restored-boot','validate-distribution.py','--instance',a.output/'restored','--output',a.output/'restored-boot')
 report={'status':'DEV; remaining acceptance gates in docs/VALIDATION_REPORT.md','timestamp_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'civitas_jar':jar.name,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'bytes':jar.stat().st_size,'passed':results,'output':str(a.output),'server_instance':str(a.output/'validated-server'),'client_pack':str(a.output/'client')}
 (a.output/'release-validation.json').write_text(json.dumps(report,indent=2)+'\n');print('PASS: new exact distribution and restored standalone instance')
if __name__=='__main__':main()
