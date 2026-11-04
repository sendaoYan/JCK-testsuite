/*
 * @test
 * @summary java.time.zone.ZoneOffsetTransition API conformance: gap and overlap offsets.
 * @run main ZoneOffsetTransitionTest
 */

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransition;

public class ZoneOffsetTransitionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZoneOffsetTransition() {
        ZoneOffsetTransition gap =
            ZoneOffsetTransition.of(LocalDateTime.of(2024, 3, 31, 2, 0),
                                    ZoneOffset.ofHours(1), ZoneOffset.ofHours(2));
        check(gap.getOffsetBefore().equals(ZoneOffset.ofHours(1)), "gap offset before");
        check(gap.getOffsetAfter().equals(ZoneOffset.ofHours(2)), "gap offset after");
        check(gap.isGap(), "is gap");
        check(!gap.isOverlap(), "not overlap");
        check(gap.getDateTimeBefore().equals(LocalDateTime.of(2024, 3, 31, 2, 0)),
              "gap datetime before");
        check(gap.getDateTimeAfter().equals(LocalDateTime.of(2024, 3, 31, 3, 0)),
              "gap datetime after");

        ZoneOffsetTransition overlap =
            ZoneOffsetTransition.of(LocalDateTime.of(2024, 10, 27, 3, 0),
                                    ZoneOffset.ofHours(2), ZoneOffset.ofHours(1));
        check(overlap.isOverlap(), "is overlap");
        check(!overlap.isGap(), "not gap");
        check(overlap.getDateTimeAfter().equals(LocalDateTime.of(2024, 10, 27, 2, 0)),
              "overlap datetime after");
    }

    public static void main(String[] args) {
        testZoneOffsetTransition();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
