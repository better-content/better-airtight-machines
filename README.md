# Better Airtight Machines

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
