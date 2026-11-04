/*
 * @test
 * @summary java.time.zone.ZoneOffsetTransition conformance: white-box structure + black-box behavior.
 * @run main ZoneOffsetTransitionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransition;

public class ZoneOffsetTransitionTest {
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
        Class<?> c = Class.forName("java.time.zone.ZoneOffsetTransition");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");

        boolean hasOf = false;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("of") && m.getParameterCount() == 3) hasOf = true;
        }
        check(hasOf, "static of(LocalDateTime, ZoneOffset, ZoneOffset) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ZoneOffsetTransition gap =
            ZoneOffsetTransition.of(LocalDateTime.of(2024, 3, 31, 2, 0),
                                    ZoneOffset.ofHours(1), ZoneOffset.ofHours(2));
        check(gap.getDateTimeBefore().equals(LocalDateTime.of(2024, 3, 31, 2, 0)),
              "gap datetime before");
        check(gap.getDateTimeAfter().equals(LocalDateTime.of(2024, 3, 31, 3, 0)),
              "gap datetime after");
        check(gap.getInstant().equals(Instant.parse("2024-03-31T01:00:00Z")), "gap instant");
        check(gap.toEpochSecond() == gap.getInstant().getEpochSecond(), "toEpochSecond");
        check(gap.getOffsetBefore().equals(ZoneOffset.ofHours(1)), "gap offset before");
        check(gap.getOffsetAfter().equals(ZoneOffset.ofHours(2)), "gap offset after");
        check(gap.getDuration().equals(Duration.ofHours(1)), "gap duration");
        check(gap.isGap(), "is gap");
        check(!gap.isOverlap(), "not overlap");
        // In a gap no offset is valid at the skipped local time.
        check(!gap.isValidOffset(ZoneOffset.ofHours(1)), "gap +1 not valid");
        check(!gap.isValidOffset(ZoneOffset.ofHours(2)), "gap +2 not valid");
        check(gap.toString().contains("Gap"), "gap toString");

        ZoneOffsetTransition overlap =
            ZoneOffsetTransition.of(LocalDateTime.of(2024, 10, 27, 3, 0),
                                    ZoneOffset.ofHours(2), ZoneOffset.ofHours(1));
        check(overlap.isOverlap(), "is overlap");
        check(!overlap.isGap(), "not gap");
        check(overlap.getDateTimeBefore().equals(LocalDateTime.of(2024, 10, 27, 3, 0)),
              "overlap datetime before");
        check(overlap.getDateTimeAfter().equals(LocalDateTime.of(2024, 10, 27, 2, 0)),
              "overlap datetime after");
        check(overlap.isValidOffset(ZoneOffset.ofHours(1)), "overlap +1 valid");
        check(overlap.isValidOffset(ZoneOffset.ofHours(2)), "overlap +2 valid");
        check(!overlap.isValidOffset(ZoneOffset.ofHours(3)), "overlap +3 invalid");
        check(overlap.toString().contains("Overlap"), "overlap toString");

        check(gap.compareTo(overlap) < 0, "gap before overlap by instant");

        ZoneOffsetTransition gap2 =
            ZoneOffsetTransition.of(LocalDateTime.of(2024, 3, 31, 2, 0),
                                    ZoneOffset.ofHours(1), ZoneOffset.ofHours(2));
        check(gap.equals(gap2), "equals equal transitions");
        check(gap.hashCode() == gap2.hashCode(), "hashCode equal transitions");
        check(!gap.equals(overlap), "not equal to overlap");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
