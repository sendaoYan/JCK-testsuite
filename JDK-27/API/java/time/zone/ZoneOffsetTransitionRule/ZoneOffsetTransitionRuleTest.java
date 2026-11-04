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
        testDeepZOTR();
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


    static void testDeepZOTR() {
        ZoneOffset std = ZoneOffset.ofHours(1);
        ZoneOffset summer = ZoneOffset.ofHours(2);
        ZoneOffsetTransitionRule r = ZoneOffsetTransitionRule.of(
            Month.MARCH, 25, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false, TimeDefinition.WALL, std, std, summer);
        xeq(r.toString(),
            "TransitionRule[Gap +01:00 to +02:00, SUNDAY on or after MARCH 25 at 02:00 WALL, standard offset +01:00]",
            "rule toString");
        check(r.getMonth() == Month.MARCH, "month");
        check(r.getDayOfMonthIndicator() == 25, "dom 25");
        check(r.getDayOfWeek() == DayOfWeek.SUNDAY, "dow");
        check(r.getLocalTime().equals(LocalTime.of(2, 0)), "localTime");
        check(!r.isMidnightEndOfDay(), "midnight flag");
        check(r.getTimeDefinition() == TimeDefinition.WALL, "timeDef");
        check(r.getStandardOffset().equals(std), "stdOffset");
        check(r.getOffsetBefore().equals(std) && r.getOffsetAfter().equals(summer), "before/after");
        ZoneOffsetTransition tr = r.createTransition(2024);
        xeq(tr.toString(), "Transition[Gap at 2024-03-31T02:00+01:00 to +02:00]", "createTransition 2024 Berlin gap");
        check(r.createTransition(2025).getDateTimeBefore().toString().equals("2025-03-30T02:00"), "createTransition 2025");
        ZoneOffsetTransitionRule last = ZoneOffsetTransitionRule.of(
            Month.OCTOBER, 25, DayOfWeek.SUNDAY, LocalTime.of(3, 0), false, TimeDefinition.WALL, std, summer, std);
        ZoneOffsetTransition to = last.createTransition(2024);
        xeq(to.toString(), "Transition[Overlap at 2024-10-27T03:00+02:00 to +01:00]", "createTransition overlap");
        ZoneOffsetTransitionRule domNeg = ZoneOffsetTransitionRule.of(
            Month.OCTOBER, -5, DayOfWeek.SUNDAY, LocalTime.of(3, 0), false, TimeDefinition.WALL, std, summer, std);
        xeq(domNeg.createTransition(2024).toString(), "Transition[Overlap at 2024-10-27T03:00+02:00 to +01:00]", "negative dom last-Sunday");
        xeq(domNeg.createTransition(2025).toString(), "Transition[Overlap at 2025-10-26T03:00+02:00 to +01:00]", "negative dom 2025");
        xeq(domNeg.toString(),
            "TransitionRule[Overlap +02:00 to +01:00, SUNDAY on or before last day minus 4 of OCTOBER at 03:00 WALL, standard offset +01:00]",
            "negative dom toString");
        xact(() -> ZoneOffsetTransitionRule.of(Month.MARCH, 32, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
                TimeDefinition.WALL, std, std, summer), IllegalArgumentException.class,
            "Day of month indicator must be between -28 and 31 inclusive excluding zero", "dom 32");
        xact(() -> ZoneOffsetTransitionRule.of(Month.MARCH, 0, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
                TimeDefinition.WALL, std, std, summer), IllegalArgumentException.class, null, "dom 0");
        xact(() -> ZoneOffsetTransitionRule.of(Month.MARCH, 25, DayOfWeek.SUNDAY, LocalTime.of(2, 0), true,
                TimeDefinition.WALL, std, std, summer), IllegalArgumentException.class,
            "Time must be midnight when end of day flag is true", "eod with non-midnight");
        ZoneOffsetTransitionRule eod = ZoneOffsetTransitionRule.of(Month.MARCH, 25, DayOfWeek.SUNDAY, LocalTime.MIDNIGHT, true,
                TimeDefinition.WALL, std, std, summer);
        xeq(eod.toString(),
            "TransitionRule[Gap +01:00 to +02:00, SUNDAY on or after MARCH 25 at 24:00 WALL, standard offset +01:00]",
            "eod toString prints 24:00");
        check(eod.isMidnightEndOfDay() && eod.getLocalTime().equals(LocalTime.MIDNIGHT), "eod flag and midnight time");
        xeq(eod.createTransition(2024).toString(), "Transition[Gap at 2024-04-01T00:00+01:00 to +02:00]", "eod rolls to next day");
        // no offset-direction validation: reversed direction yields an overlap rule
        ZoneOffsetTransitionRule reversed = ZoneOffsetTransitionRule.of(Month.MARCH, 25, DayOfWeek.SUNDAY,
                LocalTime.of(2, 0), false, TimeDefinition.WALL, std, summer, std);
        check(reversed.createTransition(2024).isOverlap(), "reversed direction overlap");
        // null dayOfWeek means exact day-of-month rule
        ZoneOffsetTransitionRule exactDay = ZoneOffsetTransitionRule.of(Month.MARCH, 25, null, LocalTime.of(2, 0), false,
                TimeDefinition.WALL, std, std, summer);
        xeq(exactDay.toString(), "TransitionRule[Gap +01:00 to +02:00, MARCH 25 at 02:00 WALL, standard offset +01:00]",
            "exact-day toString omits dow");
        check(exactDay.getDayOfWeek() == null, "exact-day dow null");
        xeq(exactDay.createTransition(2024).toString(), "Transition[Gap at 2024-03-25T02:00+01:00 to +02:00]", "exact-day fixed date");
        xeq(exactDay.createTransition(2025).toString(), "Transition[Gap at 2025-03-25T02:00+01:00 to +02:00]", "exact-day 2025 same date");
        xact(() -> ZoneOffsetTransitionRule.of(null, 25, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
                TimeDefinition.WALL, std, std, summer), NullPointerException.class, "month", "null month");
        xact(() -> ZoneOffsetTransitionRule.of(Month.MARCH, 25, DayOfWeek.SUNDAY, null, false,
                TimeDefinition.WALL, std, std, summer), NullPointerException.class, "time", "null time");
        xact(() -> ZoneOffsetTransitionRule.of(Month.MARCH, 25, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
                null, std, std, summer), NullPointerException.class, "timeDefinition", "null timeDefinition");
        xact(() -> ZoneOffsetTransitionRule.of(Month.MARCH, 25, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
                TimeDefinition.WALL, null, std, summer), NullPointerException.class, "standardOffset", "null standardOffset");
        xact(() -> ZoneOffsetTransitionRule.of(Month.MARCH, 25, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
                TimeDefinition.WALL, std, null, summer), NullPointerException.class, "offsetBefore", "null offsetBefore");
        xact(() -> ZoneOffsetTransitionRule.of(Month.MARCH, 25, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
                TimeDefinition.WALL, std, std, null), NullPointerException.class, "offsetAfter", "null offsetAfter");
        check(r.equals(ZoneOffsetTransitionRule.of(Month.MARCH, 25, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
                TimeDefinition.WALL, std, std, summer)), "equals");
        check(r.hashCode() == ZoneOffsetTransitionRule.of(Month.MARCH, 25, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
                TimeDefinition.WALL, std, std, summer).hashCode(), "hashCode");
        check(!r.equals(last), "distinct rules not equals");
    }

}
