package dev.cylox.msm.profile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class SessionProfileStoreTest {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("msm-profile-test");
        Path file = directory.resolve("profiles.properties");
        SessionProfileStore store = new SessionProfileStore(file);
        List<SessionProfile> profiles = Arrays.asList(
                new SessionProfile("Player_B", "play.example.net:25565", SessionAuthMode.MICROSOFT),
                new SessionProfile("Player_C", "mc.example.org", SessionAuthMode.OFFLINE_LOCAL));

        store.save(profiles);
        if (!profiles.equals(store.load())) throw new AssertionError("Saved profiles did not round-trip");
        store.save(Collections.<SessionProfile>emptyList());
        if (!store.load().isEmpty()) throw new AssertionError("Cleared profile list was not saved");

        List<SessionProfile> manyProfiles = new ArrayList<SessionProfile>();
        for (int index = 0; index < 150; index++) {
            manyProfiles.add(new SessionProfile("Player" + index, "server" + index + ".example.net",
                    SessionAuthMode.OFFLINE_LOCAL));
        }
        store.save(manyProfiles);
        if (!manyProfiles.equals(store.load())) {
            throw new AssertionError("Profile storage must support more than the previous 100-profile limit");
        }

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

        System.out.println("SessionProfileStoreTest: PASS");
    }
}
