/*
 * @test
 * @summary java.time.chrono.ThaiBuddhistEra conformance: white-box structure + black-box behavior.
 * @run main ThaiBuddhistEraTest
 */

import java.lang.reflect.Modifier;
import java.time.chrono.Era;
import java.time.chrono.ThaiBuddhistEra;

public class ThaiBuddhistEraTest {
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
        Class<?> c = Class.forName("java.time.chrono.ThaiBuddhistEra");
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
        check(ThaiBuddhistEra.BEFORE_BE.getValue() == 0, "BEFORE_BE value");
        check(ThaiBuddhistEra.BE.getValue() == 1, "BE value");
        check(ThaiBuddhistEra.valueOf("BE") == ThaiBuddhistEra.BE, "valueOf BE");
        check(ThaiBuddhistEra.valueOf("BEFORE_BE") == ThaiBuddhistEra.BEFORE_BE, "valueOf BEFORE_BE");
        check(ThaiBuddhistEra.values().length == 2, "two values");
        check(ThaiBuddhistEra.of(1) == ThaiBuddhistEra.BE, "of(1) BE");
        check(ThaiBuddhistEra.of(0) == ThaiBuddhistEra.BEFORE_BE, "of(0) BEFORE_BE");

        expectThrows(IllegalArgumentException.class, () -> ThaiBuddhistEra.valueOf("NOPE"), "valueOf invalid");
        expectThrows(java.time.DateTimeException.class, () -> ThaiBuddhistEra.of(2), "of(2) invalid");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
