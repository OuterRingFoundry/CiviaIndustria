#!/usr/bin/env python3
"""Render actual menus and exchange packets in an isolated integrated client."""
import argparse,json,os,shutil
from pathlib import Path
from fixture_process import run
ROOT=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--fixture',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();a.output=a.output.resolve();a.output.mkdir(parents=True,exist_ok=False)
shutil.copytree(a.fixture/'world',a.output/'saves/ci-validation');shutil.copytree(a.fixture/'mods',a.output/'mods')
(a.output/'options.txt').write_text('tutorialStep:none\nguiScale:3\nrenderDistance:3\nsimulationDistance:3\nmaxFps:30\n')
os.environ['LIBGL_ALWAYS_SOFTWARE']='1'
logfile=a.output/'client-validation.log'
with logfile.open('w') as log:code=run(['xvfb-run','-a','-s','-screen 0 1280x720x24','./gradlew','--no-daemon','runClient',f'-PciClientDir={a.output}','-PciRedesignValidation'],ROOT,log,900)
text=logfile.read_text();captures=['market-counter','bulk-storage','precision-workbench','workshop-scene'];passed=code==0 and 'CIVITAS REDESIGN CLIENT PASS' in text and all((a.output/'screenshots'/f'{x}.png').is_file() for x in captures)
(a.output/'result.json').write_text(json.dumps({'passed':passed,'exit':code,'scope':'Actual integrated client screens, packets and baked models under software rendering; not a GPU performance test','captures':captures},indent=2)+'\n')
if not passed:print('\n'.join(text.splitlines()[-160:]));raise SystemExit('Client validation failed')
print('Redesign integrated client PASS')
