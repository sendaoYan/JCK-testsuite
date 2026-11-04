/*
 * @test
 * @summary java.time.zone.ZoneOffsetTransitionRule conformance: white-box structure + black-box behavior.
 * @run main ZoneOffsetTransitionRuleTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneOffsetTransitionRule;
import java.time.zone.ZoneOffsetTransitionRule.TimeDefinition;

public class ZoneOffsetTransitionRuleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.zone.ZoneOffsetTransitionRule");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");

        boolean hasOf = false;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("of") && m.getParameterCount() == 9) hasOf = true;
        }
        check(hasOf, "static of(Month, int, DayOfWeek, LocalTime, boolean, TimeDefinition, ZoneOffset, ZoneOffset, ZoneOffset) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ZoneOffsetTransitionRule rule =
            ZoneOffsetTransitionRule.of(Month.MARCH, 31, null,
                                        LocalTime.of(2, 0), false, TimeDefinition.WALL,
                                        ZoneOffset.ofHours(1), ZoneOffset.ofHours(1),
                                        ZoneOffset.ofHours(2));
        check(rule.getMonth() == Month.MARCH, "month");
        check(rule.getDayOfMonthIndicator() == 31, "day indicator");
        check(rule.getDayOfWeek() == null, "day of week null");
        check(rule.getLocalTime().equals(LocalTime.of(2, 0)), "local time");
        check(!rule.isMidnightEndOfDay(), "not midnight end of day");
        check(rule.getTimeDefinition() == TimeDefinition.WALL, "time definition");
        check(rule.getStandardOffset().equals(ZoneOffset.ofHours(1)), "standard offset");
        check(rule.getOffsetBefore().equals(ZoneOffset.ofHours(1)), "offset before");
        check(rule.getOffsetAfter().equals(ZoneOffset.ofHours(2)), "offset after");

        ZoneOffsetTransition t = rule.createTransition(2024);
        check(t.getDateTimeBefore().equals(LocalDateTime.of(2024, 3, 31, 2, 0)),
              "createTransition datetime");
        check(t.getOffsetBefore().equals(ZoneOffset.ofHours(1)), "createTransition before");
        check(t.getOffsetAfter().equals(ZoneOffset.ofHours(2)), "createTransition after");

        // A rule that picks the last Sunday of March via a negative day-of-month indicator.
        ZoneOffsetTransitionRule lastSunday =
            ZoneOffsetTransitionRule.of(Month.MARCH, -1, DayOfWeek.SUNDAY,
                                        LocalTime.of(2, 0), false, TimeDefinition.WALL,
                                        ZoneOffset.ofHours(1), ZoneOffset.ofHours(1),
                                        ZoneOffset.ofHours(2));
        check(lastSunday.getDayOfMonthIndicator() == -1, "negative indicator");
        check(lastSunday.getDayOfWeek() == DayOfWeek.SUNDAY, "day of week Sunday");
        check(lastSunday.createTransition(2024).getDateTimeBefore()
                  .equals(LocalDateTime.of(2024, 3, 31, 2, 0)),
              "last Sunday of March 2024");

        ZoneOffsetTransitionRule equal =
            ZoneOffsetTransitionRule.of(Month.MARCH, 31, null,
                                        LocalTime.of(2, 0), false, TimeDefinition.WALL,
                                        ZoneOffset.ofHours(1), ZoneOffset.ofHours(1),
                                        ZoneOffset.ofHours(2));
        check(rule.equals(equal), "equals equal rules");
        check(rule.hashCode() == equal.hashCode(), "hashCode equal rules");
        check(!rule.equals(lastSunday), "not equal to different rule");
        check(rule.toString() != null, "toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
