package com.civitasindustria.test;

import com.civitasindustria.domain.*;
import java.nio.ByteBuffer;
import java.util.*;

/** Authority and economic eligibility boundaries, independent of Minecraft. */
public final class ResidenceChecks {
    private int checks;
    private void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    private void rejects(Runnable action) {
        boolean rejected = false;
        try { action.run(); } catch (IllegalArgumentException e) { rejected = true; }
        check(rejected, "Invalid residence operation accepted");
    }
    public static int run() { ResidenceChecks suite = new ResidenceChecks(); suite.verify(); return suite.checks; }
    private void verify() {
        UUID owner = UUID.randomUUID(), guest = UUID.randomUUID();
        Parcel parcel = new Parcel(UUID.randomUUID(), owner, -64, 0, -64, -1, 100, -1,
                "home", Set.of(guest), Set.of(Parcel.Flag.PUBLIC_ACCESS, Parcel.Flag.INTERACT));
        ResidenceRegistry r = new ResidenceRegistry();
        var home = r.declare(owner, "minecraft:overworld", parcel, -1, 50, -1);
        check(home.cell().equals(new CellPos(-1, -1)) && r.size() == 1, "Negative home cell");
        rejects(() -> r.declare(UUID.randomUUID(), "minecraft:overworld", parcel, -1, 50, -1));
        rejects(() -> r.declare(owner, "minecraft:overworld", parcel, -1, 101, -1));
        check(r.home(owner).orElseThrow().equals(home), "Rejected declaration preserves existing home");
        r.declare(guest, "minecraft:overworld", parcel, -1, 50, -1);
        Parcel revoked = new Parcel(parcel.id(), owner, -64, 0, -64, -1, 100, -1, "home", Set.of(), parcel.flags());
        check(!r.home(guest).orElseThrow().authorized(revoked) && home.authorized(revoked), "Trust revocation and public flags");
        check(!home.authorized(null), "Deleted parcel cannot qualify");
        r.declare(owner, "minecraft:the_nether", parcel, -2, 50, -2);
        check(r.size() == 2 && r.parcelCount("minecraft:overworld", parcel.id()) == 1
                && r.parcelCount("minecraft:the_nether", parcel.id()) == 1, "Cross-dimensional home replacement updates both indexes");
        check(r.removeParcel("minecraft:overworld", parcel.id()) == 1 && r.home(owner).isPresent()
                && r.home(guest).isEmpty(), "Deletion is indexed and dimension-scoped");
        Set<UUID> tenants = new HashSet<>();
        for (int i = 0; i < 8; i++) tenants.add(UUID.randomUUID());
        Parcel full = new Parcel(UUID.randomUUID(), owner, 0, 0, 0, 10, 100, 10, "full", tenants, Set.of());
        for (UUID tenant : tenants) r.declare(tenant, "minecraft:overworld", full, 1, 50, 1);
        var old = r.home(owner).orElseThrow();
        rejects(() -> r.declare(owner, "minecraft:overworld", full, 1, 50, 1));
        check(r.home(owner).orElseThrow().equals(old), "Full target parcel cannot erase old home");
        UUID tenant = tenants.iterator().next();
        r.declare(tenant, "minecraft:overworld", full, 2, 50, 2);
        check(r.parcelCount("minecraft:overworld", full.id()) == 8, "Moving home within a full parcel keeps its slot");
        check(r.withdraw(tenant) && !r.withdraw(tenant), "Withdrawal is idempotent");
        r.declare(owner, "minecraft:overworld", full, 1, 50, 1);
        check(r.parcelCount("minecraft:the_nether", parcel.id()) == 0, "Empty old parcel index removed");

        r.observe(owner, 0, true);
        for (long t = 20; t <= 12_000; t += 20) r.observe(owner, t, true);
        var qualified = r.home(owner).orElseThrow();
        check(qualified.recentlyQualified(12_000) && !qualified.recentlyQualified(11_999), "Ten minutes qualify with rollback refusal");
        r.declare(owner, "minecraft:overworld", full, 1, 50, 1);
        check(r.home(owner).orElseThrow().equals(qualified), "Identical declaration does not reset or refresh activity");
        check(qualified.recentlyQualified(12_000 + ResidenceRegistry.GRACE_TICKS)
                && !qualified.recentlyQualified(12_001 + ResidenceRegistry.GRACE_TICKS), "Grace boundary is exact");
        r.observe(owner, 12_001 + ResidenceRegistry.GRACE_TICKS, true);
        check(r.home(owner).orElseThrow().qualifyingTicks() == 0, "Expired home must requalify without an offline backlog");
        r.observe(owner, 100, true);
        check(r.home(owner).orElseThrow().qualifyingTicks() == 0, "Rollback resets progress");
        r.observe(owner, 120, true);
        r.observe(owner, 140, false);
        r.observe(owner, 160, true);
        check(r.home(owner).orElseThrow().qualifyingTicks() == 20, "Absent or nonsurvival sample breaks accrual");
        r.stopObserving(owner); r.observe(owner, 180, true);
        check(r.home(owner).orElseThrow().qualifyingTicks() == 20, "Logout prevents short-gap accrual");
        byte[] valid = r.encode();
        ResidenceRegistry loaded = ResidenceRegistry.decode(valid);
        check(Arrays.equals(valid, loaded.encode()) && loaded.home(owner).equals(r.home(owner)), "Deterministic reload preserves identity and activity");
        loaded.observe(owner, 200, true);
        check(loaded.home(owner).orElseThrow().qualifyingTicks() == 20, "Reload cannot count an unobserved interval");
        loaded.observe(owner, 220, true);
        check(loaded.home(owner).orElseThrow().qualifyingTicks() == 40, "Observed activity resumes after reload");
        byte[] future = valid.clone(); ByteBuffer.wrap(future).putInt(4, 99);
        rejects(() -> ResidenceRegistry.decode(future));
        rejects(() -> ResidenceRegistry.decode(Arrays.copyOf(valid, valid.length - 1)));
        rejects(() -> ResidenceRegistry.decode(Arrays.copyOf(valid, valid.length + 1)));
        byte[] oversized = valid.clone(); ByteBuffer.wrap(oversized).putInt(8, ResidenceRegistry.MAX_RECORDS + 1);
        rejects(() -> ResidenceRegistry.decode(oversized));
        ResidenceRegistry single = new ResidenceRegistry();
        single.declare(owner, "minecraft:overworld", full, 1, 50, 1);
        byte[] one = single.encode();
        byte[] duplicate = Arrays.copyOf(one, one.length * 2 - 12);
        System.arraycopy(one, 12, duplicate, one.length, one.length - 12);
        ByteBuffer.wrap(duplicate).putInt(8, 2);
        rejects(() -> ResidenceRegistry.decode(duplicate));
        byte[] overCapacity = Arrays.copyOf(one, 12 + 9 * (one.length - 12));
        for (int i = 0; i < 9; i++) {
            int offset = 12 + i * (one.length - 12);
            System.arraycopy(one, 12, overCapacity, offset, one.length - 12);
            ByteBuffer.wrap(overCapacity).putLong(offset, i);
        }
        ByteBuffer.wrap(overCapacity).putInt(8, 9);
        rejects(() -> ResidenceRegistry.decode(overCapacity));
        rejects(() -> new ResidenceRegistry.Home(owner, "bad dimension", parcel.id(), 0, 0, 0, 0, -1));
        rejects(() -> new ResidenceRegistry.Home(owner, "minecraft:overworld", parcel.id(), 0, 0, 0, 1, -1));
        check(Arrays.equals(valid, r.encode()), "Decode failures never mutate active authority");
    }
}
