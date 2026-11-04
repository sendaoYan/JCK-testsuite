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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
