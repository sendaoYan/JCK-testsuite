/*
 * @test
 * @summary java.time.zone.ZoneRulesProvider conformance: white-box structure + black-box behavior.
 * @run main ZoneRulesProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.NavigableMap;
import java.util.TreeSet;
import java.time.ZoneId;

public class ZoneRulesProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName()
                    + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.zone.ZoneRulesProvider");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == Object.class, "superclass Object");

        boolean hasRules = false;
        boolean hasAvailable = false;
        boolean hasVersions = false;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("getRules")) hasRules = true;
            if (m.getName().equals("getAvailableZoneIds")) hasAvailable = true;
            if (m.getName().equals("getVersions")) hasVersions = true;
        }
        check(hasRules, "static getRules present");
        check(hasAvailable, "static getAvailableZoneIds present");
        check(hasVersions, "static getVersions present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Available ids, sorted for deterministic assertion.
        TreeSet<String> ids = new TreeSet<>(ZoneRulesProvider.getAvailableZoneIds());
        check(ids.contains("UTC"), "available ids contain UTC");
        check(ids.contains("Asia/Shanghai"), "available ids contain Asia/Shanghai");

        ZoneRules utc = ZoneRulesProvider.getRules("UTC", false);
        check(utc.isFixedOffset(), "UTC fixed offset");
        check(ZoneOffset.UTC.equals(utc.getOffset(Instant.EPOCH)), "UTC offset at epoch");

        ZoneRules shanghai = ZoneRulesProvider.getRules("Asia/Shanghai", false);
        check(ZoneOffset.ofHours(8).equals(shanghai.getOffset(Instant.EPOCH)),
              "Shanghai offset at epoch");

        // forCaching=true returns the same rules behavior.
        ZoneRules utcCached = ZoneRulesProvider.getRules("UTC", true);
        check(utcCached.isFixedOffset(), "cached UTC fixed offset");
        check(ZoneOffset.UTC.equals(utcCached.getOffset(Instant.EPOCH)),
              "cached UTC offset at epoch");

        // Versioned rules map for a real zone id.
        NavigableMap<String, ZoneRules> versions = ZoneRulesProvider.getVersions("UTC");
        check(versions != null, "versions non-null");
        check(!versions.isEmpty(), "versions non-empty");
        check(versions.firstEntry().getValue() != null, "first version rules non-null");

        expectThrows(ZoneRulesException.class,
                     () -> ZoneRulesProvider.getRules("Not/AZone", false),
                     "bogus zone id");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepZoneRulesProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== contract deepening harness (appended) ====
    interface DeepRun { void run() throws Throwable; }

    static void xact(DeepRun r, Class<?> type, String exactMsg, String tag) {
        try {
            r.run();
            throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but nothing was thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but got "
                        + t.getClass().getName() + ": " + t.getMessage(), t);
            if (exactMsg != null && !exactMsg.equals(t.getMessage()))
                throw new AssertionError(tag + ": " + type.getSimpleName() + " message <" + t.getMessage()
                        + "> want <" + exactMsg + ">");
            passed++;
        }
    }

    static void xeq(Object got, Object want, String tag) {
        if (got == null ? want != null : !got.equals(want))
            throw new AssertionError(tag + ": got <" + got + "> want <" + want + ">");
        passed++;
    }


    static void testDeepZoneRulesProvider() {
        java.util.Set<String> avail = ZoneRulesProvider.getAvailableZoneIds();
        check(avail.contains("UTC") && avail.contains("Europe/Berlin") && avail.contains("America/New_York"), "avail set contents");
        check(avail.size() > 500, "avail size");
        ZoneRules berlinRules = ZoneRulesProvider.getRules("Europe/Berlin", true);
        check(berlinRules.equals(ZoneId.of("Europe/Berlin").getRules()), "getRules equals zone rules");
        ZoneRules cachedAgain = ZoneRulesProvider.getRules("Europe/Berlin", true);
        check(cachedAgain == berlinRules || cachedAgain.equals(berlinRules), "cached second fetch equal");
        xact(() -> ZoneRulesProvider.getRules("+01:00", true), ZoneRulesException.class,
            "Unknown time-zone ID: +01:00", "offset id not region id");
        xact(() -> ZoneRulesProvider.getRules("Bogus/Zone", true), ZoneRulesException.class,
            "Unknown time-zone ID: Bogus/Zone", "bogus region");
        xact(() -> ZoneRulesProvider.getRules(null, true), NullPointerException.class, "zoneId", "null id");
        java.util.NavigableMap<String, ZoneRules> utcVersions = ZoneRulesProvider.getVersions("UTC");
        check(utcVersions.size() >= 1, "versions map for UTC nonempty");
        check(utcVersions.values().iterator().next().isFixedOffset(), "UTC version rules fixed");
        check(ZoneRulesProvider.getVersions("Europe/Berlin") != null, "versions for Berlin");
        xact(() -> ZoneRulesProvider.getVersions("Bogus/Zone"), ZoneRulesException.class,
            "Unknown time-zone ID: Bogus/Zone", "versions bogus");
        ZoneRules utcRules = ZoneRulesProvider.getRules("UTC", false);
        check(utcRules.isFixedOffset() && utcRules.getOffset(Instant.EPOCH).equals(ZoneOffset.UTC), "UTC fixed rules");
        // SPI: register a custom provider
        ZoneRulesProvider.registerProvider(new java.time.zone.ZoneRulesProvider() {
            java.util.Set<String> ids = java.util.Collections.singleton("MyZone/One");
            public java.util.Set<String> provideZoneIds() { return ids; }
            public ZoneRules provideRules(String id, boolean forCaching) {
                if (!ids.contains(id)) throw new ZoneRulesException("Unknown time-zone ID: " + id);
                return ZoneRules.of(ZoneOffset.ofHours(7));
            }
            public java.util.NavigableMap<String, ZoneRules> provideVersions(String id) {
                java.util.TreeMap<String, ZoneRules> t = new java.util.TreeMap<>();
                t.put("v1", ZoneRules.of(ZoneOffset.ofHours(7)));
                return t;
            }
            protected boolean provideRefresh() { return true; }
        });
        check(ZoneRulesProvider.getAvailableZoneIds().contains("MyZone/One"), "custom zone visible");
        xeq(ZoneRulesProvider.getRules("MyZone/One", false).toString(), "ZoneRules[currentStandardOffset=+07:00]", "custom zone rules toString");
        java.util.NavigableMap<String, ZoneRules> v = ZoneRulesProvider.getVersions("MyZone/One");
        check(v.size() == 1 && v.containsKey("v1"), "custom versions");
        xeq(v.toString(), "{v1=ZoneRules[currentStandardOffset=+07:00]}", "versions toString");
        check(ZoneRulesProvider.refresh(), "refresh consults providers");
        xact(() -> ZoneRulesProvider.getRules("MyZone/Two", false), ZoneRulesException.class,
            "Unknown time-zone ID: MyZone/Two", "custom unknown id");
        xact(() -> ZoneRulesProvider.registerProvider(null), NullPointerException.class, "provider", "null provider");
        try {
            ZoneRulesProvider.registerProvider(new java.time.zone.ZoneRulesProvider() {
                public java.util.Set<String> provideZoneIds() { return java.util.Collections.singleton("MyZone/One"); }
                public ZoneRules provideRules(String id, boolean forCaching) { return ZoneRules.of(ZoneOffset.UTC); }
                public java.util.NavigableMap<String, ZoneRules> provideVersions(String id) {
                    return new java.util.TreeMap<>();
                }
            });
            check(false, "dup register unreachable");
        } catch (ZoneRulesException ex) {
            check(ex.getMessage().startsWith("Unable to register zone as one already registered with that ID: MyZone/One"),
                "dup register message, got: " + ex.getMessage());
        }
    }

}
