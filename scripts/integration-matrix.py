#!/usr/bin/env python3
"""Run isolated NeoForge GameTest profiles using previously verified exact artifacts."""
import argparse,hashlib,json,shutil,subprocess,time
from pathlib import Path
from fixture_config import isolate_voice
from fixture_process import run
from gametest_results import evaluate, required_test_count
ROOT=Path(__file__).resolve().parents[1]
PROFILES={'core':[], 'create':['create'], 'ie':['immersiveengineering'], 'kubejs':['kubejs','rhino','architectury-api'], 'industry':['create','immersiveengineering'], 'full-server':None}
def main():
 p=argparse.ArgumentParser();p.add_argument('--artifacts',required=True,type=Path);p.add_argument('--profiles',nargs='+',choices=PROFILES,default=list(PROFILES));p.add_argument('--output',type=Path,help='New directory for isolated worlds, logs and report');args=p.parse_args();args.artifacts=args.artifacts.resolve()
 if args.output:
  args.output=args.output.resolve();args.output.mkdir(parents=True,exist_ok=False)
 expected_tests=required_test_count(ROOT/'src/main/java/com/civitasindustria/test')
 report_path=args.output/'integration-results.json' if args.output else ROOT/'pack/integration-results.json'
 lock=json.loads((ROOT/'pack/mods.lock.json').read_text());results={}
 for name in args.profiles:
  directory=args.output/f'run-matrix-{name}' if args.output else ROOT/f'run-matrix-{name}';mods=directory/'mods';mods.mkdir(parents=True,exist_ok=True)
  selected=[a for a in lock['artifacts'] if a['side'] in ('both','server') and (PROFILES[name] is None or a['project'] in PROFILES[name])]
  expected={a['filename'] for a in selected}
  if {f.name for f in mods.glob('*.jar')}-expected:raise ValueError('Unexpected existing matrix artifacts; preserve and inspect '+str(mods))
  for a in selected:
   source=args.artifacts/a['filename']
   if hashlib.sha256(source.read_bytes()).hexdigest()!=a['sha256']:raise ValueError('Artifact hash '+a['filename'])
   shutil.copy2(source,mods/a['filename'])
  if name=='full-server':shutil.copytree(ROOT/'pack/overrides',directory,dirs_exist_ok=True)
  if name=='full-server':isolate_voice(directory,24462)
  logfile=directory/'matrix.log';start=time.time()
  with logfile.open('w') as out:
   try:code=run(['./gradlew','--no-daemon','runGameTestServer',f'-PciGameTestDir={directory}',f'-PciArtifactDirectory={args.artifacts}'],ROOT,out,900)
   except subprocess.TimeoutExpired:code=124
  text=logfile.read_text();verdict=evaluate(text,code,expected_tests);passed=verdict['passed']
  results[name]={**verdict,'exit':code,'seconds':round(time.time()-start,1),'log':str(logfile),'artifacts':sorted(expected)}
  report_path.write_text(json.dumps(results,indent=2)+'\n');print(name,'PASS' if passed else 'FAIL',flush=True)
 if not all(row['passed'] for row in results.values()):raise SystemExit(1)
if __name__=='__main__':main()
