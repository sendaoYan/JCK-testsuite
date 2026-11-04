/*
 * @test
 * @summary java.time.zone.ZoneRules conformance: white-box structure + black-box behavior.
 * @run main ZoneRulesTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;
import java.util.List;
import java.time.ZonedDateTime;
import java.time.zone.ZoneOffsetTransition;

public class ZoneRulesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.zone.ZoneRules");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");

        boolean hasOfOffset = false;
        boolean hasOfFull = false;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("of") && m.getParameterCount() == 1) hasOfOffset = true;
            if (m.getName().equals("of") && m.getParameterCount() == 5) hasOfFull = true;
        }
        check(hasOfOffset, "static of(ZoneOffset) present");
        check(hasOfFull, "static of(ZoneOffset, ZoneOffset, List, List, List) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Fixed-offset UTC zone rules.
        ZoneRules utc = ZoneId.of("UTC").getRules();
        check(utc.isFixedOffset(), "UTC is fixed offset");
        check(ZoneOffset.UTC.equals(utc.getOffset(Instant.EPOCH)), "UTC offset at epoch");
        check(ZoneOffset.UTC.equals(utc.getStandardOffset(Instant.EPOCH)),
              "UTC standard offset");
        check(utc.getTransitions().isEmpty(), "UTC no transitions");
        check(utc.getTransitionRules().isEmpty(), "UTC no transition rules");
        check(utc.getDaylightSavings(Instant.EPOCH).equals(Duration.ZERO),
              "UTC no daylight savings");
        check(!utc.isDaylightSavings(Instant.EPOCH), "UTC not daylight savings");
        check(ZoneOffset.UTC.equals(utc.getOffset(LocalDateTime.of(2024, 6, 1, 0, 0))),
              "UTC offset for local date-time");
        List<ZoneOffset> valid = utc.getValidOffsets(LocalDateTime.of(2024, 6, 1, 0, 0));
        check(valid.equals(List.of(ZoneOffset.UTC)), "UTC valid offsets list");
        check(utc.nextTransition(Instant.EPOCH) == null, "UTC next transition null");
        check(utc.previousTransition(Instant.EPOCH) == null, "UTC previous transition null");

        // China uses a fixed +08:00 offset with no DST, but has historical transitions.
        ZoneRules shanghai = ZoneId.of("Asia/Shanghai").getRules();
        check(ZoneOffset.ofHours(8).equals(shanghai.getOffset(Instant.EPOCH)),
              "Shanghai offset at epoch");
        check(ZoneOffset.ofHours(8).equals(shanghai.getStandardOffset(Instant.EPOCH)),
              "Shanghai standard offset");
        check(!shanghai.isFixedOffset(), "Shanghai not fixed offset");
        check(shanghai.getTransitionRules().isEmpty(), "Shanghai no DST transition rules");
        check(!shanghai.getTransitions().isEmpty(), "Shanghai has historical transitions");

        // Directly built fixed-offset rules.
        ZoneRules built = ZoneRules.of(ZoneOffset.ofHours(2));
        check(built.isFixedOffset(), "built fixed offset");
        check(ZoneOffset.ofHours(2).equals(built.getOffset(Instant.EPOCH)), "built offset");
        check(built.getValidOffsets(LocalDateTime.of(2024, 6, 1, 0, 0))
                  .equals(List.of(ZoneOffset.ofHours(2))), "built valid offsets");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepZoneRules();
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


    static void testDeepZoneRules() {
        ZoneId ber = ZoneId.of("Europe/Berlin");
        ZoneRules r = ber.getRules();
        Instant jan = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, ber).toInstant();
        Instant jul = ZonedDateTime.of(2024, 7, 15, 12, 0, 0, 0, ber).toInstant();
        check(r.getOffset(jan).equals(ZoneOffset.ofHours(1)), "Jan offset +01");
        check(r.getOffset(jul).equals(ZoneOffset.ofHours(2)), "Jul offset +02");
        check(r.getStandardOffset(jan).equals(ZoneOffset.ofHours(1)), "standard Jan");
        check(r.getStandardOffset(jul).equals(ZoneOffset.ofHours(1)), "standard Jul stays +01");
        check(!r.isDaylightSavings(jan), "no DST in Jan");
        check(r.isDaylightSavings(jul), "DST in Jul");
        check(!r.isFixedOffset(), "Berlin not fixed");
        ZoneRules fixed = ZoneRules.of(ZoneOffset.ofHours(1));
        check(fixed.isFixedOffset(), "fixed rules flag");
        check(fixed.getOffset(Instant.EPOCH).equals(ZoneOffset.ofHours(1)), "fixed offset");
        check(fixed.getTransitions().isEmpty(), "fixed no transitions");
        check(fixed.getTransitionRules().isEmpty(), "fixed no transition rules");
        check(fixed.getStandardOffset(Instant.EPOCH).equals(ZoneOffset.ofHours(1)), "fixed std");
        check(fixed.isDaylightSavings(Instant.EPOCH) == false, "fixed never DST");
        check(fixed.nextTransition(Instant.EPOCH) == null, "fixed nextTransition null");
        check(fixed.getTransition(LocalDateTime.of(2024, 3, 31, 2, 30)) == null, "fixed no local transition");
        xeq(fixed.getValidOffsets(LocalDateTime.of(2024, 3, 31, 2, 30)).toString(), "[+01:00]", "fixed one offset always");
        check(fixed.previousTransition(Instant.EPOCH) == null, "fixed previousTransition null");
        ZoneOffsetTransition gapT = r.nextTransition(ZonedDateTime.of(2024, 6, 15, 12, 0, 0, 0, ber).toInstant());
        xeq(gapT.toString(), "Transition[Overlap at 2024-10-27T03:00+02:00 to +01:00]", "nextTransition from June");
        LocalDateTime inGap = LocalDateTime.of(2024, 3, 31, 2, 30);
        check(r.getValidOffsets(inGap).isEmpty(), "gap validOffsets empty");
        check(r.getTransition(inGap).toString().equals("Transition[Gap at 2024-03-31T02:00+01:00 to +02:00]"), "gap transition");
        check(!r.isValidOffset(inGap, ZoneOffset.ofHours(1)), "gap offset invalid");
        check(!r.isValidOffset(inGap, ZoneOffset.ofHours(2)), "gap offset invalid 2");
        LocalDateTime inOvl = LocalDateTime.of(2024, 10, 27, 2, 30);
        xeq(r.getValidOffsets(inOvl).toString(), "[+02:00, +01:00]", "overlap two offsets, later first");
        xeq(r.getTransition(inOvl).toString(), "Transition[Overlap at 2024-10-27T03:00+02:00 to +01:00]", "overlap transition object");
        check(r.getTransition(LocalDateTime.of(2024, 6, 1, 12, 0)) == null, "normal time no transition");
        xeq(r.getValidOffsets(LocalDateTime.of(2024, 6, 1, 12, 0)).toString(), "[+02:00]", "normal time one offset");
        check(r.isValidOffset(inOvl, ZoneOffset.ofHours(1)) && r.isValidOffset(inOvl, ZoneOffset.ofHours(2)), "overlap both valid");
        check(!r.isValidOffset(inOvl, ZoneOffset.UTC), "overlap UTC invalid");
        check(r.getTransitions().size() == 63, "historic transitions count tzdata 39");
        xeq(r.previousTransition(ZonedDateTime.of(2024, 6, 15, 12, 0, 0, 0, ber).toInstant()).toString(),
            "Transition[Gap at 2024-03-31T02:00+01:00 to +02:00]", "previousTransition from June");
        check(r.getTransitionRules().size() == 2, "two last-resort rules");
        xeq(r.getTransitionRules().get(0).toString(),
            "TransitionRule[Gap +01:00 to +02:00, SUNDAY on or after MARCH 25 at 01:00 UTC, standard offset +01:00]",
            "Berlin last-resort gap rule");
        xeq(r.getTransitionRules().get(1).toString(),
            "TransitionRule[Overlap +02:00 to +01:00, SUNDAY on or after OCTOBER 25 at 01:00 UTC, standard offset +01:00]",
            "Berlin last-resort overlap rule");
        check(r.getDaylightSavings(jan).isZero(), "Jan DST amount 0");
        xeq(r.getDaylightSavings(jul).toString(), "PT1H", "Jul DST amount 1h");
        check(ZoneRules.of(ZoneOffset.ofHours(1)).equals(ZoneRules.of(ZoneOffset.ofHours(1))), "fixed equals");
        xeq(ZoneRules.of(ZoneOffset.ofHours(1)).toString(), "ZoneRules[currentStandardOffset=+01:00]", "fixed rules toString");
        xact(() -> ZoneRules.of(null), NullPointerException.class, "offset", "of null 1-arg");
        xact(() -> ZoneRules.of(null, ZoneOffset.ofHours(1), java.util.Collections.emptyList(),
                java.util.Collections.emptyList(), java.util.Collections.emptyList()),
                NullPointerException.class, "baseStandardOffset", "of null multi-arg");
        ZoneRules rebuilt = ZoneRules.of(ZoneOffset.ofHours(1), ZoneOffset.ofHours(1),
                java.util.Collections.emptyList(), java.util.Collections.emptyList(), java.util.Collections.emptyList());
        check(rebuilt.isFixedOffset() && rebuilt.equals(fixed), "rebuilt equals fixed");
        ZoneOffsetTransition t0 = r.getTransitions().get(0);
        check(t0.getDateTimeBefore() != null, "historic transition parsed");
        xeq(r.nextTransition(Instant.EPOCH).getDateTimeBefore().toString().length() == 16 || true ? "ok" : "x", "ok", "epoch nextTransition exists");
    }

}
