#!/usr/bin/env python3
"""Resolve specified releases from the official Modrinth API; never choose an unpinned update."""
import argparse
import json
from pathlib import Path
import urllib.parse
import urllib.request

ROOT=Path(__file__).resolve().parents[1]
HEADERS={'User-Agent':'OuterRingFoundry/CiviaIndustria development tooling'}
def api(path):
    with urllib.request.urlopen(urllib.request.Request('https://api.modrinth.com/v2/'+path,headers=HEADERS),timeout=30) as response:
        return json.load(response)

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--candidates',default=str(ROOT/'pack/candidates.json'));parser.add_argument('--output',default=str(ROOT/'pack/downloads.lock.json'));args=parser.parse_args()
    candidates=json.loads(Path(args.candidates).read_text());resolved=[]
    for entry in candidates:
        project=api('project/'+entry['project'])
        query=urllib.parse.urlencode({'game_versions':'["1.21.1"]','loaders':'["neoforge"]'})
        versions=api('project/'+project['id']+'/version?'+query)
        matches=[v for v in versions if v['version_number']==entry['version'] and v['version_type']=='release']
        if len(matches)!=1:raise ValueError(f"Exact stable release not found: {entry}")
        v=matches[0];files=[f for f in v['files'] if f['primary']]
        if len(files)!=1:raise ValueError('Ambiguous artifact')
        f=files[0]
        resolved.append({'project_id':project['id'],'project':project['slug'],'title':project['title'],'version_id':v['id'],'version':v['version_number'],'minecraft':'1.21.1','loader':'neoforge','side':entry['side'],'filename':f['filename'],'url':f['url'],'size':f['size'],'upstream_hashes':f['hashes'],'license':project['license'],'source_url':project['source_url'],'project_url':'https://modrinth.com/mod/'+project['slug'],'dependencies':v['dependencies']})
        print(project['slug'],v['version_number'],flush=True)
    Path(args.output).write_text(json.dumps({'schema':1,'artifacts':resolved},indent=2)+'\n')
if __name__=='__main__':main()
