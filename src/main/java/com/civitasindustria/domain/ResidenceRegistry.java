package com.civitasindustria.domain;

import java.io.*;
import java.util.*;

/** Server-wide home authority. No world references or historical residence sweeps. */
public final class ResidenceRegistry {
    public static final int VERSION = 1, MAX_RECORDS = 100_000, PARCEL_CAPACITY = 8;
    public static final long QUALIFYING_TICKS = 12_000, GRACE_TICKS = 1_728_000;
    public static final int SAMPLE_TICKS = 20, MAX_BYTES = 32 * 1024 * 1024;
    private static final int MAGIC = 0x43495253;

    public record Home(UUID player, String dimension, UUID parcel, int x, int y, int z,
                       long qualifyingTicks, long lastActivity) {
        public Home {
            Objects.requireNonNull(player); Objects.requireNonNull(parcel);
            if (dimension == null || dimension.length() > 256
                    || !dimension.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")
                    || x < -30_000_000 || x > 30_000_000 || z < -30_000_000 || z > 30_000_000
                    || y < -2048 || y > 2047 || qualifyingTicks < 0 || qualifyingTicks > QUALIFYING_TICKS
                    || lastActivity < -1 || (lastActivity == -1 && qualifyingTicks != 0))
                throw new IllegalArgumentException("Invalid residence record");
        }
        public CellPos cell() { return CellPos.fromBlock(x, z); }
        public boolean authorized(Parcel current) {
            return current != null && parcel.equals(current.id()) && current.contains(x, y, z)
                    && (player.equals(current.owner()) || current.trusted().contains(player));
        }
        public boolean recentlyQualified(long now) {
            return now >= 0 && qualifyingTicks == QUALIFYING_TICKS && lastActivity >= 0
                    && now >= lastActivity && now - lastActivity <= GRACE_TICKS;
        }
    }

    private record ParcelKey(String dimension, UUID parcel) {}
    private final Map<UUID, Home> homes = new HashMap<>();
    private final Map<ParcelKey, Set<UUID>> byParcel = new HashMap<>();
    // Observations are deliberately transient: reloads and logouts never accrue time.
    private final Map<UUID, Long> observations = new HashMap<>();

    public Optional<Home> home(UUID player) { return Optional.ofNullable(homes.get(player)); }
    public int size() { return homes.size(); }
    public int parcelCount(String dimension, UUID parcel) {
        return byParcel.getOrDefault(new ParcelKey(dimension, parcel), Set.of()).size();
    }
    public Home declare(UUID player, String dimension, Parcel parcel, int x, int y, int z) {
        Home next = new Home(player, dimension, parcel.id(), x, y, z, 0, -1);
        if (!next.authorized(parcel)) throw new IllegalArgumentException("Home must be inside an owned or explicitly trusted parcel.");
        Home before = homes.get(player);
        if (before != null && before.dimension.equals(dimension) && before.parcel.equals(parcel.id())
                && before.x == x && before.y == y && before.z == z) return before;
        ParcelKey key = new ParcelKey(dimension, parcel.id());
        if (parcelCount(dimension, parcel.id()) >= PARCEL_CAPACITY
                && (before == null || !new ParcelKey(before.dimension, before.parcel).equals(key)))
            throw new IllegalArgumentException("Parcel residence capacity is eight.");
        if (before == null && homes.size() >= MAX_RECORDS) throw new IllegalArgumentException("Residence storage limit.");
        // All validation precedes replacement, including cross-dimensional capacity checks.
        withdraw(player);
        insert(next);
        return next;
    }
    private void insert(Home home) {
        if (homes.size() >= MAX_RECORDS || homes.containsKey(home.player)
                || parcelCount(home.dimension, home.parcel) >= PARCEL_CAPACITY)
            throw new IllegalArgumentException("Duplicate or oversized residence authority");
        homes.put(home.player, home);
        byParcel.computeIfAbsent(new ParcelKey(home.dimension, home.parcel), k -> new HashSet<>()).add(home.player);
    }
    public boolean withdraw(UUID player) {
        observations.remove(player);
        Home before = homes.remove(player);
        if (before == null) return false;
        ParcelKey key = new ParcelKey(before.dimension, before.parcel);
        Set<UUID> members = byParcel.get(key);
        members.remove(player);
        if (members.isEmpty()) byParcel.remove(key);
        return true;
    }
    public int removeParcel(String dimension, UUID parcel) {
        Set<UUID> members = Set.copyOf(byParcel.getOrDefault(new ParcelKey(dimension, parcel), Set.of()));
        members.forEach(this::withdraw);
        return members.size();
    }
    public void stopObserving(UUID player) { observations.remove(player); }

    /** Called only for online players, at a 20-tick cadence. Returns whether saved data changed. */
    public boolean observe(UUID player, long now, boolean eligiblePresence) {
        if (now < 0) throw new IllegalArgumentException("Negative game time");
        Home before = homes.get(player);
        if (before == null) return false;
        if (!eligiblePresence) { stopObserving(player); return false; }
        Long previous = observations.put(player, now);
        long progress = before.qualifyingTicks;
        if (before.lastActivity < 0 || now < before.lastActivity || now - before.lastActivity > GRACE_TICKS) progress = 0;
        if (previous != null && now > previous && now - previous <= SAMPLE_TICKS
                && now >= before.lastActivity)
            progress = Math.min(QUALIFYING_TICKS, progress + now - previous);
        Home next = new Home(player, before.dimension, before.parcel, before.x, before.y, before.z, progress, now);
        if (next.equals(before)) return false;
        homes.put(player, next);
        return true;
    }

    public byte[] encode() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeInt(MAGIC); out.writeInt(VERSION); out.writeInt(homes.size());
            for (UUID id : new TreeSet<>(homes.keySet())) {
                Home h = homes.get(id);
                writeId(out, h.player); out.writeUTF(h.dimension); writeId(out, h.parcel);
                out.writeInt(h.x); out.writeInt(h.y); out.writeInt(h.z);
                out.writeLong(h.qualifyingTicks); out.writeLong(h.lastActivity);
            }
            out.flush();
            if (bytes.size() > MAX_BYTES) throw new IllegalArgumentException("Residence byte limit");
            return bytes.toByteArray();
        } catch (IOException e) { throw new IllegalStateException(e); }
    }
    public static ResidenceRegistry decode(byte[] bytes) {
        if (bytes.length < 12 || bytes.length > MAX_BYTES) throw new IllegalArgumentException("Residence byte limit");
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
            if (in.readInt() != MAGIC || in.readInt() != VERSION) throw new IOException("Unsupported residence schema");
            int count = in.readInt();
            if (count < 0 || count > MAX_RECORDS) throw new IOException("Residence record limit");
            ResidenceRegistry result = new ResidenceRegistry();
            for (int i = 0; i < count; i++) result.insert(new Home(readId(in), in.readUTF(), readId(in),
                    in.readInt(), in.readInt(), in.readInt(), in.readLong(), in.readLong()));
            if (in.available() != 0) throw new IOException("Trailing residence data");
            return result;
        } catch (IOException | RuntimeException e) { throw new IllegalArgumentException("Invalid residence snapshot", e); }
    }
    private static void writeId(DataOutputStream out, UUID id) throws IOException {
        out.writeLong(id.getMostSignificantBits()); out.writeLong(id.getLeastSignificantBits());
    }
    private static UUID readId(DataInputStream in) throws IOException { return new UUID(in.readLong(), in.readLong()); }
}
