package com.outerringfoundry.mapsurvey;

/** Minecraft-independent, finite lease on a render-distance setting. No chunk scans. */
public final class SurveySession {
    public enum End { NONE, EXPIRED, CONTEXT_CHANGED, SETTING_CHANGED }
    private final Object context;
    private final int previous;
    private final int applied;
    private final long started;
    private final long durationNanos;

    public SurveySession(Object context, int previous, int applied, int seconds, long now) {
        if (context == null || previous < 2 || previous > 256 || applied < 2 || applied > 32
                || seconds < 5 || seconds > 120) throw new IllegalArgumentException("Invalid survey lease");
        this.context = context;
        this.previous = previous;
        this.applied = applied;
        this.started = now;
        this.durationNanos = seconds * 1_000_000_000L;
    }
    public static int target(int requested, int advertised, int clientMaximum) {
        if (requested < 2 || requested > 32 || advertised < 2 || clientMaximum < 2)
            throw new IllegalArgumentException("Radius must be 2–32 chunks and a server must be connected");
        return Math.min(requested, Math.min(advertised, Math.min(32, clientMaximum)));
    }
    public End check(Object currentContext, int currentDistance, long now) {
        if (context != currentContext) return End.CONTEXT_CHANGED;
        if (currentDistance != applied) return End.SETTING_CHANGED;
        return now - started >= durationNanos ? End.EXPIRED : End.NONE;
    }
    public int secondsLeft(long now) {
        return (int) Math.max(0, (durationNanos - (now - started) + 999_999_999L) / 1_000_000_000L);
    }
    public int restore(int current) { return current == applied ? previous : current; }
    public int previous() { return previous; }
    public int applied() { return applied; }
}
