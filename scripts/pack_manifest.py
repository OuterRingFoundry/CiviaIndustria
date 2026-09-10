"""Immutable pack fingerprinting with semantic Java properties (Minecraft rewrites comments/order)."""
import hashlib,json,re
from pathlib import Path
PREFIXES=('mods','libraries','config','defaultconfigs','kubejs/server_scripts','kubejs/client_scripts','kubejs/startup_scripts','kubejs/data','kubejs/assets','world/serverconfig','world/datapacks')
def properties(path):
 result={}
 for line in path.read_text(encoding='utf-8').splitlines():
  if not line.strip() or line.lstrip().startswith(('#','!')):continue
  if (len(line)-len(line.rstrip(chr(92))))%2:raise ValueError('Continued property lines require canonicalization: '+str(path))
  key,sep,value=line.partition('=')
  if not sep:raise ValueError('Unsupported properties syntax: '+str(path))
  result[key.strip()]=value.strip()
 return result
def fingerprint(root):
 state={'schema':2,'minecraft':'1.21.1','neoforge':'21.1.249','files':{},'properties':{},'managed_prefixes':list(PREFIXES)}
 for path in sorted(root.rglob('*')):
  name=path.relative_to(root).as_posix()
  if not path.is_file() or path.is_symlink():continue
  if name not in ('mods.lock.json','runtime.lock.json','run.sh','run.bat','user_jvm_args.txt','server.properties') and not any(name.startswith(p+'/') for p in PREFIXES):continue
  if path.suffix=='.properties':state['properties'][name]=properties(path)
  else:
   with path.open('rb') as file:state['files'][name]=hashlib.file_digest(file,'sha256').hexdigest()
 (root/'pack-state.json').write_text(json.dumps(state,indent=2)+'\n');return state


def built_mod_jar(root: Path) -> Path:
    """Resolve the configured artifact exactly; never pick an older JAR by glob order."""
    values = properties(root / 'gradle.properties')
    return root / 'build/libs' / (values['mod_id'] + '-' + values['mod_version'] + '.jar')
