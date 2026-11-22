# MSM Core

Shared, Minecraft-independent core library for the MSM Fabric client.

## Scope

MSM Core owns reusable domain logic and persistence. Minecraft/Fabric UI hooks and client integration remain in `MSM-Fabric-1.14.4`. The Fabric mod packages the Core classes into its final mod JAR, so players install one mod.

## Current implementation

- `profile`: validated local session profile model, persisted authentication-mode selection, and atomic properties-file persistence.
- `session`: in-memory registry for up to 20 managed session slots, immutable session snapshots, and validated lifecycle state transitions.
- Java 8 bytecode compatibility.
- No Minecraft, Fabric, authentication, or network dependency.
- No passwords, Microsoft credentials, access tokens, or session cookies are stored.
- Session slots and their status are runtime-only; selecting an authentication mode does not authenticate or connect a session.

## Authentication modes

- `MICROSOFT`: profile expects a legitimate Microsoft-authenticated Minecraft identity.
- `OFFLINE_LOCAL`: profile is intended for a local/offline-mode server operated or explicitly configured by the player. It is not a way to bypass online-mode authentication.

Existing profile files without an `authMode` property load as `MICROSOFT` for compatibility and safer defaults.

## Planned packages

Independent network connections, authentication handoff, and session controls will be added incrementally under separate packages such as `account`, `network`, and `integration`. Actual Microsoft sign-in and network connections are not implemented yet.

## Build and verify

Requires JDK 17 and Gradle 8.7 (or the root project's wrapper).

```sh
gradle clean check
```

The `check` task runs the profile persistence and session registry smoke tests.
