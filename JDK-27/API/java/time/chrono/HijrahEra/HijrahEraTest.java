/*
 * @test
 * @summary java.time.chrono.HijrahEra conformance: white-box structure + black-box behavior.
 * @run main HijrahEraTest
 */

import java.lang.reflect.Modifier;
import java.time.chrono.Era;
import java.time.chrono.HijrahEra;

public class HijrahEraTest {
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
        Class<?> c = Class.forName("java.time.chrono.HijrahEra");
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
        check(HijrahEra.AH.getValue() == 1, "AH value");
        check(HijrahEra.valueOf("AH") == HijrahEra.AH, "valueOf AH");
        check(HijrahEra.values().length == 1, "one value");
        check(HijrahEra.of(1) == HijrahEra.AH, "of(1) AH");

        expectThrows(IllegalArgumentException.class, () -> HijrahEra.valueOf("NOPE"), "valueOf invalid");
        expectThrows(java.time.DateTimeException.class, () -> HijrahEra.of(2), "of(2) invalid");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
