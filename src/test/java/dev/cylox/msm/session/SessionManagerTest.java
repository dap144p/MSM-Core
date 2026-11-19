package dev.cylox.msm.session;

import dev.cylox.msm.profile.SessionProfile;

public final class SessionManagerTest {
    public static void main(String[] args) {
        SessionManager manager = new SessionManager();
        SessionProfile firstProfile = new SessionProfile("PlayerOne", "play.example.net");
        SessionManager.Session first = manager.create(firstProfile);

        check(first.getState() == SessionState.OFFLINE, "New session must be offline");
        check(manager.find(first.getId()).isPresent(), "Created session must be findable");
        expectIllegalArgument(new Action() {
            @Override
            public void run() {
                manager.create(firstProfile);
            }
        }, "Duplicate profile must be rejected");

        manager.updateState(first.getId(), SessionState.CONNECTING);
        manager.updateState(first.getId(), SessionState.ONLINE);
        expectIllegalState(new Action() {
            @Override
            public void run() {
                manager.remove(first.getId());
            }
        }, "Online session must not be removed");
        expectIllegalState(new Action() {
            @Override
            public void run() {
                manager.updateState(first.getId(), SessionState.CONNECTING);
            }
        }, "Invalid state transition must be rejected");

        manager.updateState(first.getId(), SessionState.DISCONNECTED);
        check(manager.remove(first.getId()), "Disconnected session must be removable");
        check(!manager.find(first.getId()).isPresent(), "Removed session must not be findable");

        for (int index = 0; index < SessionManager.MAX_SESSIONS; index++) {
            manager.create(new SessionProfile("Player" + index, "server" + index + ".example.net"));
        }
        check(manager.size() == SessionManager.MAX_SESSIONS, "Manager must accept 20 sessions");
        check(manager.remainingCapacity() == 0, "Capacity must be zero when full");
        expectIllegalState(new Action() {
            @Override
            public void run() {
                manager.create(new SessionProfile("PlayerExtra", "extra.example.net"));
            }
        }, "Session 21 must be rejected");

        System.out.println("SessionManagerTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void expectIllegalArgument(Action action, String message) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    private static void expectIllegalState(Action action, String message) {
        try {
            action.run();
        } catch (IllegalStateException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    private interface Action {
        void run();
    }
}
