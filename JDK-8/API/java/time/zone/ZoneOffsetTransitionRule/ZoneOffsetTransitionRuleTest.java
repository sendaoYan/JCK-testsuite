/*
 * @test
 * @summary java.time.zone.ZoneOffsetTransitionRule API conformance: rule accessors.
 * @run main ZoneOffsetTransitionRuleTest
 */

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransitionRule;
import java.time.zone.ZoneOffsetTransitionRule.TimeDefinition;
import java.time.zone.ZoneOffsetTransition;

public class ZoneOffsetTransitionRuleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZoneOffsetTransitionRule() {
        ZoneOffsetTransitionRule rule =
            ZoneOffsetTransitionRule.of(Month.MARCH, 31, DayOfWeek.SUNDAY,
                                        LocalTime.of(2, 0), false, TimeDefinition.WALL,
                                        ZoneOffset.ofHours(1), ZoneOffset.ofHours(1),
                                        ZoneOffset.ofHours(2));
        check(rule.getMonth() == Month.MARCH, "month");
        check(rule.getDayOfMonthIndicator() == 31, "day indicator");
        check(rule.getDayOfWeek() == DayOfWeek.SUNDAY, "day of week");
        check(rule.getOffsetBefore().equals(ZoneOffset.ofHours(1)), "offset before");
        check(rule.getOffsetAfter().equals(ZoneOffset.ofHours(2)), "offset after");
    }

    public static void main(String[] args) {
        testZoneOffsetTransitionRule();
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
                 null, std, std, summer), NullPointerException.class, "timeDefnition", "null timeDefinition");
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
