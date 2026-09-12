package com.outerringfoundry.mapsurvey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Restores a temporary distance on next startup if Minecraft saved options mid-survey. */
public final class RecoveryJournal {
    private final Path file;
    public RecoveryJournal(Path file) { this.file = file; }
    public void begin(int previous, int applied) throws IOException {
        validate(previous, applied);
        Files.createDirectories(file.getParent());
        // CREATE_NEW preserves any unresolved prior record. Write before changing the option.
        Files.writeString(file, "1\n" + previous + "\n" + applied + "\n", StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }
    public int recoveredDistance(int current) throws IOException {
        if (!Files.exists(file)) return current;
        if (Files.size(file) > 64) throw new IOException("Oversized survey recovery record");
        String[] lines = Files.readString(file, StandardCharsets.UTF_8).split("\n", -1);
        try {
            if (lines.length != 4 || !lines[0].equals("1") || !lines[3].isEmpty())
                throw new IllegalArgumentException("Unknown recovery format");
            int previous = Integer.parseInt(lines[1]), applied = Integer.parseInt(lines[2]);
            validate(previous, applied);
            return current == applied ? previous : current;
        } catch (IllegalArgumentException e) { throw new IOException("Invalid survey recovery record; preserved", e); }
    }
    public void clear() throws IOException { Files.deleteIfExists(file); }
    public boolean exists() { return Files.exists(file); }
    private static void validate(int previous, int applied) {
        if (previous < 2 || previous > 256 || applied < 2 || applied > 32)
            throw new IllegalArgumentException("Invalid recovery distance");
    }
}
