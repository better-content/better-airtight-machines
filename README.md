# Better Airtight Machines

## Native source fixture safety

The actual ForgeGradle GameTest RunConfig uses a fresh `build/gametest/<UUID>`, never the
ordinary client/server run directory or an existing world. Absent-only comment settings
preserve native defaults and existing settings; preparation copies authored structures.
The full gate requires exactly three completed/passed native tests, startup/shutdown and
own structured status in both actual latest/debug logs, with ERROR/FATAL and failed-operation
WARN/stack rejection. Fast negative controls check diagnostics, populations and property
preservation. All old builds/worlds/evidence remain retained; source guards do not constitute
runtime acceptance or authorize a build/native producer.

Native-only preparation completes exactly `config/adpother-common.toml` and
`config/forgeendertech-common.toml` from the read-only, hash-pinned authored snapshot in
`gradle/native-default-configs/`. The Python preparer verifies clean tracked canonical Root
input bytes and original Git blobs, then binds the actual UUID/source-fixture receipt before
`CREATE_NEW` writes. Matching existing bytes are preserved; mismatches/symlinks/foreign or
used fixtures fail without overwriting them. `native-default-config-inputs.json` records the
actual destination, before/after hashes and provenance before launch. The canonical Root is
`/home/dev/better-content-modpack` (or explicit `BC_CANONICAL_PACK_ROOT` with identical verified
tracked inputs); unrelated Root HEAD changes do not replace immutable input provenance.

This is fixture input fidelity, not a proven watcher-race cure. No warm `Sources.cfg` files,
watcher masking, native setting/budget changes or production runtime dependency are added.
Minecraft-free input/receipt/negative controls and rejecting runtime/sources archive checks
are wired into the normal fast gate. Ordinary operator directories and the original eight
JVM/three Native assertions are unchanged. New gates still need separately admitted owning
build/Native execution; a source-only check is not runtime qualification.

## Playtest diagnostics

`bc.playtest.better_airtight_machines.containment` reports server-side upgrade install,
remove, already-sealed/not-sealed rejection, and break-refund requests at DEBUG. Inventory
refund versus native drop request is explicit; a drop request is not observed item receipt
or durable persistence. Ordinary unrelated clicks stay silent. No inventory contents,
player names or item NBT are logged.

The owner-local Java17 helper shares120 DEBUG/TRACE details/minute (burst20),256 aggregation
keys and2048-character records. Native END ticks publish30-second summaries; server stop
clears aggregation without rearming TRACE. INFO startup includes effective enabled state,
version, required integrations and exact owner-artifact source metadata when present
(UNKNOWN otherwise). Standalone remains native INFO; the pack selects scoped DEBUG through
its logger profile/`bc.playtest.level`. No per-block/render output is added. The six-case
common contract and two pure containment-field tests run in the ordinary source gate.

## Scope and authority

This repository owns its mod-specific behavior and authoring inputs. Read [local instructions](AGENTS.md)
and the [shared documentation/policy index](../../better-content-modpack/docs/README.md).


Adds an `better_airtight_machines:airtight_upgrade` item that seals Create Basins and PneumaticCraft Fluid Mixers and Thermopneumatic Processing Plants against Latent ChemLib gas escape. Apply the upgrade by using it on a supported machine; sneak-use it empty-handed to remove it.

Requires Latent ChemLib, Heat Sync, ChemLib, Create, and PneumaticCraft. The pack recipe and teaching annotation are maintained by the modpack.

Build with `./gradlew verifyFast`, run runtime and GameTest validation with `./gradlew verifyFull`, and stage the reobfuscated jar with `./gradlew stageRuntimeJar`.
