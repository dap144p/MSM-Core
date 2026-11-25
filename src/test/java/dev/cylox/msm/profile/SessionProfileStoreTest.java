package dev.cylox.msm.profile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class SessionProfileStoreTest {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("msm-profile-test");
        Path file = directory.resolve("profiles.properties");
        SessionProfileStore store = new SessionProfileStore(file);
        if (!store.load().isEmpty()) throw new AssertionError("Missing file must load as empty");
        List<SessionProfile> profiles = Arrays.asList(
                new SessionProfile("Player_B", "play.example.net:25565", SessionAuthMode.MICROSOFT),
                new SessionProfile("Player_C", "mc.example.org", SessionAuthMode.OFFLINE_LOCAL));

        store.save(profiles);
        if (!profiles.equals(store.load())) throw new AssertionError("Saved profiles did not round-trip");
        store.save(Collections.<SessionProfile>emptyList());
        if (!store.load().isEmpty()) throw new AssertionError("Cleared profile list was not saved");

        Files.write(file, Arrays.asList(
                "profile.count=1",
                "profile.0.username=LegacyPlayer",
                "profile.0.server=legacy.example.net"), StandardCharsets.ISO_8859_1);
        List<SessionProfile> legacyProfiles = store.load();
        if (legacyProfiles.size() != 1
                || legacyProfiles.get(0).getAuthMode() != SessionAuthMode.MICROSOFT) {
            throw new AssertionError("Legacy profiles must default to Microsoft authentication");
        }

        boolean rejectedBlankUsername = false;
        try {
            new SessionProfile(" ", "play.example.net");
        } catch (IllegalArgumentException expected) {
            rejectedBlankUsername = true;
        }
        if (!rejectedBlankUsername) throw new AssertionError("Blank username must be rejected");

        boolean rejectedInvalidUsername = false;
        try {
            new SessionProfile("Player Name", "play.example.net");
        } catch (IllegalArgumentException expected) {
            rejectedInvalidUsername = true;
        }
        if (!rejectedInvalidUsername) throw new AssertionError("Invalid username must be rejected");

        boolean rejectedNullAuthMode = false;
        try {
            new SessionProfile("Player", "play.example.net", null);
        } catch (IllegalArgumentException expected) {
            rejectedNullAuthMode = true;
        }
        if (!rejectedNullAuthMode) throw new AssertionError("Missing auth mode must be rejected");

        testInvalidFiles(store, file, directory);
        Files.delete(file);
        Files.delete(directory);
        System.out.println("SessionProfileStoreTest: PASS");
    }

    private static void testInvalidFiles(SessionProfileStore store, Path file, Path directory) throws Exception {
        expectInvalidFile(store, file, "", "Missing profile count");
        expectInvalidFile(store, file, "profile.0.username=Player\n", "Missing profile count");
        expectInvalidFile(store, file, "profile.count=not-a-number\n", "Invalid profile count");
        expectInvalidFile(store, file, "profile.count=-1\n", "Profile count is out of range");
        expectInvalidFile(store, file, "profile.count=101\n", "Profile count is out of range");
        expectInvalidFile(store, file, "profile.count=1\nprofile.0.username=Player\n", "Invalid saved profile at index 0");
        expectInvalidFile(store, file, "profile.count=" + "\\" + "u12G4\n", "Invalid profile properties");

        String profile = "profile.count=1\nprofile.0.username=Player\nprofile.0.server=example.net\n";
        expectInvalidFile(store, file, profile + "profile.0.authMode=FUTURE_MODE\n", "Invalid saved profile at index 0");
        expectInvalidFile(store, file, profile + "profile.0.authMode=\n", "Invalid saved profile at index 0");
        Files.write(file, (profile + "profile.0.authMode= offline_local  \n").getBytes(StandardCharsets.ISO_8859_1));
        if (store.load().get(0).getAuthMode() != SessionAuthMode.OFFLINE_LOCAL) {
            throw new AssertionError("Known auth modes should accept whitespace and case differences");
        }

        try {
            new SessionProfileStore(directory).load();
            throw new AssertionError("An unreadable store must not load as empty");
        } catch (IOException expected) {
            // A directory cannot be read as a properties file.
        }

        Path link = directory.resolve("broken-link.properties");
        try {
            Files.createSymbolicLink(link, directory.resolve("missing-target.properties"));
        } catch (UnsupportedOperationException | IOException | SecurityException unsupported) {
            return; // Symbolic links may be unavailable to the test user.
        }
        try {
            try {
                new SessionProfileStore(link).load();
                throw new AssertionError("A dangling symbolic link must not load as empty");
            } catch (IOException expected) {
                // An existing but broken link is not a new store.
            }
        } finally {
            Files.deleteIfExists(link);
        }
    }

    private static void expectInvalidFile(SessionProfileStore store, Path file, String contents, String message)
            throws Exception {
        byte[] original = contents.getBytes(StandardCharsets.ISO_8859_1);
        Files.write(file, original);
        try {
            store.load();
            throw new AssertionError("Expected IOException: " + message);
        } catch (IOException expected) {
            if (!expected.getMessage().contains(message)) {
                throw new AssertionError("Unexpected error: " + expected.getMessage(), expected);
            }
        }
        if (!Arrays.equals(original, Files.readAllBytes(file))) {
            throw new AssertionError("Loading an invalid file must not change it");
        }
    }
}
