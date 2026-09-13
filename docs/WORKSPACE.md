# Active workspace and server archive

The EC2 checkout is `/home/ubuntu/codexproj/CiviaIndustria`. Keep source, Git history,
required documentation, scripts and current delivery evidence here. Run Minecraft
builds and retain large dependencies/worlds on the designated server under `/data/.tmp`.

On 2026-09-11, 131 older artifact/worktree files (212,648,417 bytes, about 203 MiB)
were moved to `frederick@100.98.111.18:/data/.tmp/civitas-ec2-archive-2026-09-11`.
Every copied file passed size and SHA-256 comparison before local removal. The archive
contains `civitas-archive-manifest.json` and `verification.json`. Git records the
archive location, selected paths and manifest/bundle hashes in
[`pack/workspace-archive.json`](../pack/workspace-archive.json).

The inactive beta packaging worktree was clean and removed with `git worktree remove`.
Its branch remains in the active repository. The archive additionally contains
`civitas-beta-history.bundle`, with the complete `publish-beta-pack` branch history.
The archived worktree's `.git` file is a historical pointer; restore a usable checkout
from the branch or bundle instead of using that pointer as a repository.

Older status reports retain their original evidence paths. For any archived EC2 path,
append its path relative to `/home/ubuntu/codexproj/` to the archive directory. Existing
server-side evidence remains at its original location. No server worlds or other
projects were removed. Historical tracked docs remain local because current instructions
and reports link to them; the space savings came from obsolete copies and downloads.

To retrieve an old artifact, copy just the needed path from the archive using `scp`.
To restore the beta branch separately, fetch the bundle and use:

```sh
git clone --branch publish-beta-pack /path/to/civitas-beta-history.bundle beta-restored
```

Retain the manifest beside any further archive transfer and verify it before removing
another local copy. Passwords and private keys are not stored in project documentation.

The preceding 0.4.0 workshop JAR was also preserved in the archive's `prior-delivery/`
folder before the perimeter update. Its SHA-256 is
`1057353cbd5b2a91b145021ae503f2bbe273df82c9ba77dc8bf2d8e141d4f89e`.
This additional delivery copy is separate from the original 131-file manifest.

## Current residence/creative editing checkpoint

The editable build source is `/data/.tmp/civitas-residence-creative` on the designated
server. It includes the source, resources and build/test scripts matching `ce7d9a3`.
Local Git remains the history authority; the server's `RESIDENCE_CREATIVE_HANDOFF.md`
names this revision, artifact hash and reproduction commands. Large test worlds/logs
stay on the server. The final local JAR is in `artifacts/residence-creative/` beside
its checksum and current evidence; earlier iterations are retained separately.
