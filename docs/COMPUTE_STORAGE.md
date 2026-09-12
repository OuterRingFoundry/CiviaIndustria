# Compute and storage

The former build server is unavailable. Keep a small EC2 source checkout and use
GitHub Actions for full Java/NeoForge builds, native server tests, and client captures.
The EC2 is disk constrained; a Docker container on that host shares its disk capacity.
Moving a file into such a container or pushing it to Git does not free the local copy.

Commit source, recipes, configuration, dependency URLs/checksums, and concise test
evidence. Keep downloaded mod JARs, Java/Gradle caches, generated worlds, logs and
screenshots out of Git history. Validation artifacts retain compiled output and logs
for 14 days; they are temporary evidence, not permanent backup. Pin commit identities
and hashes so outputs can be reproduced. Keep the latest deliverable locally until
its replacement is verified. Preserve user worlds and unmatched files.

Exact immutable dependency JARs can share disk in local staging directories through
hard links after checksum comparison. Never modify a staged JAR in place. Download
and verify its replacement separately. This task recovered 35.7 MiB this way while
preserving file paths. After cloud validation, removing the task-owned temporary
Java 21 extraction recovered about 288 MiB more. The EC2 then had about 930 MiB
free. Java can be installed again when local computation needs it; full mod builds
continue in Actions. Installer archives contain our mod and exact official download
recipes, so restricted third-party JARs are not republished in a public repository.
