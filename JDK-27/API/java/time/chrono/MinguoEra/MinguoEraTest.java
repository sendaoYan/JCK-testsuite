/*
 * @test
 * @summary java.time.chrono.MinguoEra conformance: white-box structure + black-box behavior.
 * @run main MinguoEraTest
 */

import java.lang.reflect.Modifier;
import java.time.chrono.Era;
import java.time.chrono.MinguoEra;

public class MinguoEraTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); passed++; }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name, e); }
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try { r.run(); } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName() + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.chrono.MinguoEra");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        check(Era.class.isAssignableFrom(c), "implements Era");
        checkMethod(c, "values");
        checkMethod(c, "valueOf", String.class);
        checkMethod(c, "of", int.class);
        checkMethod(c, "getValue");
    }

    static void testBlackBox() {
        check(MinguoEra.BEFORE_ROC.getValue() == 0, "BEFORE_ROC value");
        check(MinguoEra.ROC.getValue() == 1, "ROC value");
        check(MinguoEra.valueOf("ROC") == MinguoEra.ROC, "valueOf ROC");
        check(MinguoEra.valueOf("BEFORE_ROC") == MinguoEra.BEFORE_ROC, "valueOf BEFORE_ROC");
        check(MinguoEra.values().length == 2, "two values");
        check(MinguoEra.of(1) == MinguoEra.ROC, "of(1) ROC");
        check(MinguoEra.of(0) == MinguoEra.BEFORE_ROC, "of(0) BEFORE_ROC");

        expectThrows(IllegalArgumentException.class, () -> MinguoEra.valueOf("NOPE"), "valueOf invalid");
        expectThrows(java.time.DateTimeException.class, () -> MinguoEra.of(2), "of(2) invalid");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
