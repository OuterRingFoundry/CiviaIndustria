#!/usr/bin/env python3
"""Verify exact artifacts, loader metadata, nested mods and required dependencies (Python 3.11+)."""
import argparse,hashlib,io,json,re,tomllib,zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]

def version(value):
    numbers=re.findall(r'\d+',value)
    return tuple(int(n) for n in numbers[:6])+(0,)*(6-len(numbers[:6]))
def accepts(spec,current):
    if not spec or spec=='*':return True
    if spec[0] not in '[(':return version(current)>=version(spec)
    for start,lower,upper,end in re.findall(r'([\[(])([^,\]\)]*)(?:,([^\]\)]*))?([\]\)])',spec):
        c=version(current)
        if not upper and ',' not in spec:return c==version(lower)
        if lower and (c<version(lower) or c==version(lower) and start=='('):continue
        if upper and (c>version(upper) or c==version(upper) and end==')'):continue
        return True
    return False

def platform_accepts(mod_id, spec, current):
    if accepts(spec, current):return True
    # FancyModLoader 4.0.43/4.0.44 bundled with pinned NeoForge 21.1.249:
    # VersionSupportMatrix treats MC 1.21 and NeoForge 21.0.166 as compatible.
    # Keep this scoped to this pack's exact runtime, not arbitrary loader versions.
    fallback={'minecraft':('1.21.1','1.21'),'neoforge':('21.1.249','21.0.166')}.get(mod_id)
    return bool(fallback and current==fallback[0] and accepts(spec,fallback[1]))

def metadata(data,label,depth=0):
    if depth>4 or len(data)>128*1024*1024:raise ValueError('Nested artifact bounds')
    result=[]
    with zipfile.ZipFile(io.BytesIO(data)) as jar:
        names=set(jar.namelist());path=next((p for p in ['META-INF/neoforge.mods.toml','META-INF/mods.toml'] if p in names),None)
        if path:
            info=jar.getinfo(path)
            if info.file_size>1024*1024:raise ValueError('Oversized mod metadata')
            config=tomllib.loads(jar.read(path).decode())
            manifest=jar.read('META-INF/MANIFEST.MF').decode(errors='replace') if 'META-INF/MANIFEST.MF' in names else ''
            jar_version=next((line.split(': ',1)[1] for line in manifest.splitlines() if line.startswith('Implementation-Version: ')),None)
            for mod in config.get('mods',[]):
                v=mod.get('version','');v=jar_version if v=='${file.jarVersion}' else v
                if not v:raise ValueError('Unresolved mod version in '+label)
                result.append({'id':mod['modId'],'version':v,'jar':label,'dependencies':config.get('dependencies',{}).get(mod['modId'],[])})
        elif depth==0:raise ValueError('Not a Forge/NeoForge mod: '+label)
        if 'META-INF/jarjar/metadata.json' in names:
            nested=json.loads(jar.read('META-INF/jarjar/metadata.json'))
            for entry in nested.get('jars',[]):
                path=entry['path']
                if path not in names or jar.getinfo(path).file_size>128*1024*1024:raise ValueError('Invalid nested artifact')
                result.extend(metadata(jar.read(path),label+'!'+path,depth+1))
    return result

def verify(lock,directory,side,allow_all=False):
    if lock.get('schema')!=1 or not lock.get('artifacts'):raise ValueError('Empty or unsupported lockfile')
    artifacts=lock['artifacts'] if allow_all else [a for a in lock['artifacts'] if a['side'] in ('both',side)]
    expected={a['filename'] for a in artifacts}
    if len(expected)!=len(artifacts):raise ValueError('Duplicate artifact filenames')
    actual={p.name for p in directory.glob('*.jar')}
    if actual!=expected:raise ValueError(f'Missing: {sorted(expected-actual)}; unexpected: {sorted(actual-expected)}')
    mods={}
    for a in artifacts:
        if Path(a['filename']).name!=a['filename'] or a['minecraft']!='1.21.1' or a['loader']!='neoforge':raise ValueError('Wrong platform or invalid filename')
        data=(directory/a['filename']).read_bytes()
        if hashlib.sha256(data).hexdigest()!=a['sha256']:raise ValueError('Hash mismatch: '+a['filename'])
        for mod in metadata(data,a['filename']):
            if mod['id'] in mods:raise ValueError('Duplicate mod ID: '+mod['id'])
            mods[mod['id']]=mod
    available={'minecraft':'1.21.1','neoforge':'21.1.249','java':'21',**{id:m['version'] for id,m in mods.items()}}
    for mod in mods.values():
        for dep in mod['dependencies']:
            required=dep.get('type')=='required' or dep.get('mandatory',False)
            applies=dep.get('side','BOTH') in ('BOTH',side.upper())
            if not(required and applies):continue
            id=dep['modId']
            if id not in available:raise ValueError(f"{mod['id']} missing dependency {id}")
            if not platform_accepts(id,dep.get('versionRange',''),available[id]):raise ValueError(f"{mod['id']} incompatible {id} {available[id]}: {dep.get('versionRange')}")
    return mods

def main():
    p=argparse.ArgumentParser();p.add_argument('--directory',required=True,type=Path);p.add_argument('--side',choices=['server','client'],required=True);p.add_argument('--lock',type=Path,default=ROOT/'pack/mods.lock.json');p.add_argument('--all-artifacts',action='store_true');p.add_argument('--report',type=Path);a=p.parse_args()
    mods=verify(json.loads(a.lock.read_text()),a.directory,a.side,a.all_artifacts)
    if a.report:a.report.write_text(json.dumps(mods,indent=2)+'\n')
    print(f'PASS: {len(mods)} top-level and nested mods verified for {a.side}')
if __name__=='__main__':main()
