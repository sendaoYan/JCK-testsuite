/*
 * @test
 * @summary java.time.zone.ZoneOffsetTransitionRule.TimeDefinition conformance: white-box structure + black-box behavior.
 * @run main TimeDefinitionTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransitionRule.TimeDefinition;
import java.time.zone.ZoneOffsetTransitionRule;

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
        testDeepTimeDefinition();
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


    static void testDeepTimeDefinition() {
        xeq(java.util.Arrays.toString(TimeDefinition.values()), "[UTC, WALL, STANDARD]", "values order");
        LocalDateTime ldt = LocalDateTime.of(2024, 3, 31, 2, 0);
        ZoneOffset std = ZoneOffset.ofHours(1);
        xeq(TimeDefinition.UTC.createDateTime(ldt, std, ZoneOffset.ofHours(2)).toString(), "2024-03-31T04:00", "UTC def adds wall offset");
        xeq(TimeDefinition.WALL.createDateTime(ldt, std, ZoneOffset.ofHours(2)).toString(), "2024-03-31T02:00", "WALL def keeps wall");
        xeq(TimeDefinition.STANDARD.createDateTime(ldt, std, ZoneOffset.ofHours(2)).toString(), "2024-03-31T03:00", "STANDARD def converts std to wall");
        check(TimeDefinition.STANDARD.createDateTime(ldt, std, std).toString().equals("2024-03-31T02:00"), "STANDARD with std==offset");
        check(TimeDefinition.UTC.createDateTime(ldt, ZoneOffset.UTC, ZoneOffset.UTC).toString().equals("2024-03-31T02:00"), "UTC def at UTC zone");
        xeq(TimeDefinition.STANDARD.createDateTime(ldt, ZoneOffset.ofHours(-5), ZoneOffset.ofHours(2)).toString(),
            "2024-03-31T09:00", "STANDARD def big delta +7h");
        check(TimeDefinition.valueOf("UTC") == TimeDefinition.UTC, "valueOf");
        check(TimeDefinition.valueOf("WALL") == TimeDefinition.WALL, "valueOf WALL");
        xact(() -> TimeDefinition.valueOf("BOGUS"), IllegalArgumentException.class,
            "No enum constant java.time.zone.ZoneOffsetTransitionRule.TimeDefinition.BOGUS", "valueOf bogus");
        check(TimeDefinition.UTC.toString().equals("UTC"), "toString");
    }

}
