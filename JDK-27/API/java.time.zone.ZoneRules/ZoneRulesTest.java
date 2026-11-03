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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
