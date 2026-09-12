package com.outerringfoundry.mapsurvey;

import java.nio.file.*;
import java.io.IOException;

public final class SurveyTests {
    private static int checks;
    public static void main(String[] args) throws Exception {
        Object world = new Object();
        var lease = new SurveySession(world, 6, 16, 5, Long.MAX_VALUE - 1_000_000_000L);
        long start = Long.MAX_VALUE - 1_000_000_000L;
        check(lease.check(world, 16, start + 4_999_999_999L) == SurveySession.End.NONE, "not early, nanoTime wrap");
        check(lease.check(world, 16, start + 5_000_000_000L) == SurveySession.End.EXPIRED, "exact deadline after wrap");
        check(lease.secondsLeft(start + 4_500_000_000L) == 1, "countdown rounds up");
        check(lease.secondsLeft(start + 6_000_000_000L) == 0, "countdown nonnegative");
        check(lease.check(null, 16, start) == SurveySession.End.CONTEXT_CHANGED, "disconnect");
        check(lease.check(new Object(), 16, start) == SurveySession.End.CONTEXT_CHANGED, "new world/dimension");
        check(lease.check(world, 8, start) == SurveySession.End.SETTING_CHANGED, "manual edit cancels");
        check(lease.restore(16) == 6, "restore previous");
        check(lease.restore(8) == 8, "keep manual edit");
        check(SurveySession.target(32, 10, 32) == 10, "server cap");
        check(SurveySession.target(8, 32, 32) == 8, "requested cap");
        check(SurveySession.target(32, 64, 16) == 16, "small memory client cap");
        expectIllegal(() -> SurveySession.target(33, 32, 32));
        expectIllegal(() -> SurveySession.target(1, 32, 32));
        expectIllegal(() -> SurveySession.target(16, 0, 32));
        expectIllegal(() -> new SurveySession(world, 6, 16, 121, 0));
        expectIllegal(() -> new SurveySession(world, 6, 16, 4, 0));
        Path dir = Files.createTempDirectory("survey-recovery-test-");
        Path file = dir.resolve("recovery.txt");
        var journal = new RecoveryJournal(file);
        try {
            check(journal.recoveredDistance(6) == 6, "missing recovery no-op");
            journal.begin(6, 16);
            check(journal.recoveredDistance(16) == 6, "crash after temporary options saved");
            check(journal.recoveredDistance(6) == 6, "crash before options saved");
            check(journal.recoveredDistance(8) == 8, "crash after manual edit");
            expectIO(() -> journal.begin(8, 20));
            for (String bad : new String[]{"2\n6\n16\n", "1\n6\n", "1\n-5\n16\n", "x".repeat(100)}) {
                Files.writeString(file, bad);
                expectIO(() -> journal.recoveredDistance(16));
                check(Files.readString(file).equals(bad), "bad recovery retained");
            }
            journal.clear();
            check(!journal.exists(), "normal cleanup");
        } finally { Files.deleteIfExists(file); Files.deleteIfExists(dir); }
        System.out.println("SURVEY DOMAIN PASS: " + checks + " checks");
    }
    private static void check(boolean condition, String label) { if (!condition) throw new AssertionError(label); checks++; }
    private static void expectIllegal(Runnable action) {
        try { action.run(); throw new AssertionError("Expected invalid argument"); }
        catch (IllegalArgumentException expected) { checks++; }
    }
    private interface IOAction { void run() throws IOException; }
    private static void expectIO(IOAction action) throws IOException {
        try { action.run(); throw new AssertionError("Expected IO refusal"); }
        catch (IOException expected) { checks++; }
    }
}
