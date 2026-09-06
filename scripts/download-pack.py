#!/usr/bin/env python3
"""Download locked artifacts and verify upstream hashes before making them available."""
import argparse,hashlib,json,os,urllib.request
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def main():
    p=argparse.ArgumentParser();p.add_argument('--directory',required=True);p.add_argument('--lock',default=str(ROOT/'pack/downloads.lock.json'));p.add_argument('--output-lock',default=str(ROOT/'pack/mods.lock.json'));args=p.parse_args()
    lock=json.loads(Path(args.lock).read_text());folder=Path(args.directory);folder.mkdir(parents=True,exist_ok=True)
    if not lock.get('artifacts'):raise ValueError('Empty lockfile')
    for a in lock['artifacts']:
        name=a['filename'];assert Path(name).name==name and name.endswith('.jar')
        target=folder/name
        if not target.exists():
            temporary=target.with_suffix('.jar.part')
            request=urllib.request.Request(a['url'],headers={'User-Agent':'OuterRingFoundry/CiviaIndustria'})
            with urllib.request.urlopen(request,timeout=60) as response,temporary.open('wb') as out:
                while chunk:=response.read(1024*1024):out.write(chunk)
            data=temporary.read_bytes()
            assert len(data)==a['size'] and hashlib.sha512(data).hexdigest()==a['upstream_hashes']['sha512'],'Downloaded artifact hash mismatch'
            temporary.replace(target)
        data=target.read_bytes()
        assert len(data)==a['size'] and hashlib.sha512(data).hexdigest()==a['upstream_hashes']['sha512'],'Artifact hash mismatch'
        a['sha256']=hashlib.sha256(data).hexdigest();print(name,a['sha256'],flush=True)
    Path(args.output_lock).write_text(json.dumps(lock,indent=2)+'\n')
if __name__=='__main__':main()
