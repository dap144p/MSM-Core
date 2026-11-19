# MSM Core

Shared, Minecraft-independent core library for the MSM Fabric client.

## Scope

MSM Core owns reusable domain logic and persistence. Minecraft/Fabric UI hooks and client integration remain in `MSM-Fabric-1.14.4`. The Fabric mod packages the Core classes into its final mod JAR, so players install one mod.

## Current implementation

- `profile`: validated local session profile model and atomic properties-file persistence.
- `session`: in-memory registry for up to 20 managed session slots, immutable session snapshots, and validated lifecycle state transitions.
- Java 8 bytecode compatibility.
- No Minecraft, Fabric, authentication, or network dependency.
- No passwords, Microsoft credentials, access tokens, or session cookies are stored.
- Session slots and their status are runtime-only; they do not establish additional Minecraft connections.

## Planned packages

Future features will be added incrementally under separate packages such as `account`, `network`, and `integration`. Authentication and independent network connections are not implemented yet.

## Build and verify

Requires JDK 17 and Gradle 8.7 (or the root project's wrapper).

```sh
gradle clean check
```

The `check` task runs the profile persistence and session registry smoke tests.
