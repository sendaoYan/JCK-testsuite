/*
 * @test
 * @summary java.time.zone.ZoneOffsetTransitionRule.TimeDefinition conformance: white-box structure + black-box behavior.
 * @run main TimeDefinitionTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransitionRule.TimeDefinition;

public class TimeDefinitionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.zone.ZoneOffsetTransitionRule$TimeDefinition");
        check(c.isEnum(), "is enum");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(Enum.class.isAssignableFrom(c), "extends Enum");
        check(c.getEnumConstants().length == 3, "three constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(TimeDefinition.values().length == 3, "three constants");
        check(TimeDefinition.valueOf("UTC") == TimeDefinition.UTC, "valueOf UTC");
        check(TimeDefinition.valueOf("WALL") == TimeDefinition.WALL, "valueOf WALL");
        check(TimeDefinition.valueOf("STANDARD") == TimeDefinition.STANDARD, "valueOf STANDARD");

        LocalDateTime dt = LocalDateTime.of(2020, 1, 1, 0, 0);
        ZoneOffset std = ZoneOffset.ofHours(2);
        ZoneOffset wall = ZoneOffset.ofHours(3);
        check(TimeDefinition.UTC.createDateTime(dt, std, wall)
                  .equals(LocalDateTime.of(2020, 1, 1, 3, 0)), "UTC uses wall offset");
        check(TimeDefinition.STANDARD.createDateTime(dt, std, wall)
                  .equals(LocalDateTime.of(2020, 1, 1, 1, 0)), "STANDARD uses standard offset");
        check(TimeDefinition.WALL.createDateTime(dt, std, wall).equals(dt), "WALL returns input");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
