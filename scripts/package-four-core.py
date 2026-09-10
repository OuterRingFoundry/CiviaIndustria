#!/usr/bin/env python3
"""Build reproducible client/server downloads without redistributing upstream JARs."""
import argparse
import hashlib
import json
import zipfile
from pathlib import Path
from pack_manifest import built_mod_jar

ROOT=Path(__file__).resolve().parents[1]

def archive(path, entries):
    with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as output:
        for name,data in sorted(entries.items()):
            info=zipfile.ZipInfo(name,date_time=(2026,9,10,0,0,0))
            info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=0o100644 << 16
            output.writestr(info,data)

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--output',type=Path,required=True)
    parser.add_argument('--evidence',type=Path,required=True,help='Completed validate-dev output')
    args=parser.parse_args()
    if args.output.exists():raise ValueError('Choose a new package directory')
    jar=built_mod_jar(ROOT);raw=jar.read_bytes();sha=hashlib.sha256(raw).hexdigest()
    evidence=json.loads((args.evidence/'dev-validation.json').read_text())
    if evidence['status']!='automated-dev-passed; external acceptance open' or evidence['civitas_jar_sha256']!=sha:
        raise ValueError('This exact JAR has not passed the automated DEV suite')
    from importlib.machinery import SourceFileLoader
    validation=SourceFileLoader('validate_dev',str(ROOT/'scripts/validate-dev.py')).load_module()
    if evidence['source_sha256']!=validation.source_digest():raise ValueError('Sources changed since acceptance')
    lock=json.loads((ROOT/'pack/mods.lock.json').read_text())
    required={'create','immersiveengineering','pollution-of-the-realms','advanced-chimneys','forgeendertech'}
    if not required<={a['project'] for a in lock['artifacts']}:raise ValueError('Missing core mod/dependency')
    args.output.mkdir(parents=True)
    name='CiviaIndustria-0.2.0-four-core-dev'
    index={'formatVersion':1,'game':'minecraft','versionId':'0.2.0-four-core-dev','name':'Civia Industria — Four Core',
        'summary':'Industrial civilization with integrated Create, IE, Pollution of the Realms and Advanced Chimneys.',
        'dependencies':{'minecraft':'1.21.1','neoforge':'21.1.249'},'files':[]}
    for a in lock['artifacts']:
        index['files'].append({'path':'mods/'+a['filename'],'hashes':a['upstream_hashes'],'downloads':[a['url']],
            'fileSize':a['size'],'env':{s:'required' if a['side'] in ('both',s) else 'unsupported' for s in ['client','server']}})
    release={'version':index['versionId'],'status':'Automated DEV acceptance passed; external gates remain open',
        'customJar':{'filename':jar.name,'sha256':sha},'open_acceptance_gates':evidence['open_acceptance_gates']}
    notice='# Third-party dependencies\n\nUpstream JARs are downloaded from official sources and are not embedded.\n\n'
    for a in lock['artifacts']:
        notice+=f"- [{a['title']}]({a['project_url']}), {a['version']}. Project license: {a['license']['id']}. SHA-256: `{a['sha256']}`.\n"
    notice+='\nEndertech project pages state CC-BY-ND-4.0; their JAR metadata states All rights reserved. This distribution ships download references, not their JARs.\n'
    boot={'mods/'+jar.name:raw,'release.json':json.dumps(release,indent=2).encode(),
        'install.py':(ROOT/'scripts/install-pack-template.py').read_bytes(),'THIRD-PARTY.md':notice.encode()}
    for filename in ['download-pack.py','assemble-pack.py','verify-pack.py','install-runtime.py','verify-instance.py','pack_manifest.py']:
        boot['scripts/'+filename]=(ROOT/'scripts'/filename).read_bytes()
    for filename in ['mods.lock.json','runtime.lock.json']:boot['pack/'+filename]=(ROOT/'pack'/filename).read_bytes()
    client={'modrinth.index.json':json.dumps(index,indent=2).encode(),'overrides/mods/'+jar.name:raw,'overrides/THIRD-PARTY.md':notice.encode()}
    for path in sorted((ROOT/'pack/overrides').rglob('*')):
        if not path.is_file():continue
        relative=path.relative_to(ROOT/'pack/overrides').as_posix()
        boot['pack/overrides/'+relative]=path.read_bytes()
        client[('server-overrides/' if relative=='server.properties' else 'overrides/')+relative]=path.read_bytes()
    readme=(ROOT/'docs/FOUR_CORE_INTEGRATION.md').read_text()+'\n## Installation\n\nClient: import the .mrpack in a Modrinth-compatible launcher.\nServer: extract the server archive, then run `python3 server-bootstrap/install.py --output NEW_SERVER_DIRECTORY`. Use Java 21 and Python 3.11+. Read and accept the Minecraft EULA before starting the server.\n'
    archive(args.output/(name+'.mrpack'),client)
    archive(args.output/(name+'-server.zip'),{'README.md':readme.encode(),**{'server-bootstrap/'+k:v for k,v in boot.items()}})
    (args.output/'README.md').write_text(readme)
    (args.output/'release.json').write_text(json.dumps(release,indent=2)+'\n')
    (args.output/'THIRD-PARTY.md').write_text(notice)
    (args.output/'SHA256SUMS').write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name+'\n' for p in sorted(args.output.iterdir()) if p.suffix in ('.mrpack','.zip')))
    print('Packaged exact four-core client and server downloads:',args.output)
if __name__=='__main__':main()
