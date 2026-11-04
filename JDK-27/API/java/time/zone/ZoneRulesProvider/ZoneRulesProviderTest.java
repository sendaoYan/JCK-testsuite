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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
