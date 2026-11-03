/*
 * @test
 * @summary java.util.stream.Collector.Characteristics conformance: white-box structure + black-box behavior.
 * @run main CharacteristicsTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collector;

public class CharacteristicsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void expectThrows(Class<? extends Throwable> ex, Runnable r, String msg) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Collector$Characteristics");
        check(c.isEnum(), "Characteristics is an enum");
        check(Modifier.isPublic(c.getModifiers()), "Characteristics is public");
        check(c.getEnumConstants().length == 3, "three constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Collector.Characteristics[] v = Collector.Characteristics.values();
        check(v.length == 3, "3 constants");
        check(Arrays.toString(v).equals("[CONCURRENT, UNORDERED, IDENTITY_FINISH]"),
              "declaration order CONCURRENT, UNORDERED, IDENTITY_FINISH");
        check(Collector.Characteristics.valueOf("CONCURRENT") == Collector.Characteristics.CONCURRENT,
              "valueOf CONCURRENT");
        check(Collector.Characteristics.valueOf("UNORDERED") == Collector.Characteristics.UNORDERED,
              "valueOf UNORDERED");
        check(Collector.Characteristics.valueOf("IDENTITY_FINISH") == Collector.Characteristics.IDENTITY_FINISH,
              "valueOf IDENTITY_FINISH");
        expectThrows(IllegalArgumentException.class,
                () -> Collector.Characteristics.valueOf("BOGUS"),
                "valueOf invalid -> IllegalArgumentException");
        check(Collector.Characteristics.UNORDERED != Collector.Characteristics.IDENTITY_FINISH,
              "distinct constants");
        check(Collector.Characteristics.CONCURRENT.ordinal() == 0, "CONCURRENT ordinal 0");
        check(Collector.Characteristics.UNORDERED.ordinal() == 1, "UNORDERED ordinal 1");
        check(Collector.Characteristics.IDENTITY_FINISH.ordinal() == 2, "IDENTITY_FINISH ordinal 2");
        check(Collector.Characteristics.CONCURRENT.name().equals("CONCURRENT"), "name()");

        Set<Collector.Characteristics> set = EnumSet.of(
                Collector.Characteristics.CONCURRENT, Collector.Characteristics.UNORDERED);
        check(set.contains(Collector.Characteristics.CONCURRENT)
                && set.contains(Collector.Characteristics.UNORDERED), "EnumSet of characteristics");
        check(!set.contains(Collector.Characteristics.IDENTITY_FINISH), "EnumSet excludes others");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
