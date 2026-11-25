# MSM-Core

**MSM-Core** is the shared, Minecraft-independent Java library used by **Multi Sessions Manager (MSM)**.

## Project structure

```text
MSM-Core/
├── src/main/java/dev/cylox/msm/
│   ├── profile/    # Profile model, auth-mode metadata, persistence
│   └── session/    # Session registry, immutable snapshots, lifecycle states
└── src/test/java/dev/cylox/msm/
    ├── profile/
    └── session/
```

Keep platform-independent domain logic here. Minecraft/Fabric screens, input handling, and version-specific networking belong in [MSM-Fabric](https://github.com/dap144p/Multi-Sessions-Manager-Fabric).

The two MSM-Fabric builds (Minecraft 1.14.4 and 1.21.8) consume this repository as a Git submodule and embed the required Core classes in their own mod JARs. **Players do not install MSM-Core separately**; they install only the matching MSM-Fabric JAR.

## Current implementation

- **Profiles:** validated username/server data and a persisted authentication-mode selection.
- **Persistence:** Java Properties format with temporary-file writes and atomic replacement when supported.
- **Sessions:** in-memory registry, unique session IDs, immutable session snapshots, profile lookup, and validated lifecycle transitions.
- **Compatibility:** Java 8 bytecode; no Minecraft, Fabric, authentication, or network dependency.
- **Data handling:** no passwords, Microsoft credentials, access tokens, or session cookies are stored.

MSM-Core does not impose an arbitrary maximum number of saved profiles or managed sessions. Actual capacity remains bounded by available memory and Java collection limits. Session state is runtime-only; choosing an authentication mode does not authenticate or connect a session.

## Authentication modes

- `MICROSOFT`: the profile expects a legitimate Microsoft-authenticated Minecraft identity.
- `OFFLINE_LOCAL`: the profile is intended for a local/offline-mode server operated or explicitly configured by the player. It does not bypass online-mode authentication.

Legacy profile files without an `authMode` property default to `MICROSOFT`. An explicitly blank or unknown mode is rejected instead of silently replaced. A malformed profile file raises `IOException`; integrations should report the error and must not overwrite the file with an empty profile list as automatic recovery.

## Not implemented in Core

Microsoft sign-in, token handling, independent network connections, chat, reconnect logic, Minecraft UI, and session controls are integration responsibilities. They are not implemented by this library merely because a profile or runtime session exists.

## Build and verify

Requires JDK 17 and Gradle 8.7 (or the MSM-Fabric root project's Gradle wrapper).

```sh
gradle clean check
```

The `check` task compiles Java 8-compatible classes and runs the profile persistence and session registry tests.
