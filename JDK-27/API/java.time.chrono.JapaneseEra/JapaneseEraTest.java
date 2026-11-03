/*
 * @test
 * @summary java.time.chrono.JapaneseEra conformance: white-box structure + black-box behavior.
 * @run main JapaneseEraTest
 */

import java.lang.reflect.Modifier;
import java.time.chrono.Era;
import java.time.chrono.JapaneseEra;

public class JapaneseEraTest {
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
        Class<?> c = Class.forName("java.time.chrono.JapaneseEra");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isEnum(), "not an enum (final class)");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Era.class.isAssignableFrom(c), "implements Era");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        checkMethod(c, "of", int.class);
        checkMethod(c, "getValue");
        checkMethod(c, "values");
        checkMethod(c, "valueOf", String.class);
    }

    static void testBlackBox() {
        check(JapaneseEra.MEIJI.getValue() == -1, "MEIJI value");
        check(JapaneseEra.TAISHO.getValue() == 0, "TAISHO value");
        check(JapaneseEra.SHOWA.getValue() == 1, "SHOWA value");
        check(JapaneseEra.HEISEI.getValue() == 2, "HEISEI value");
        check(JapaneseEra.REIWA.getValue() == 3, "REIWA value");

        check(JapaneseEra.of(-1) == JapaneseEra.MEIJI, "of(-1) MEIJI");
        check(JapaneseEra.of(0) == JapaneseEra.TAISHO, "of(0) TAISHO");
        check(JapaneseEra.of(1) == JapaneseEra.SHOWA, "of(1) SHOWA");
        check(JapaneseEra.of(2) == JapaneseEra.HEISEI, "of(2) HEISEI");
        check(JapaneseEra.of(3) == JapaneseEra.REIWA, "of(3) REIWA");

        JapaneseEra[] values = JapaneseEra.values();
        check(values.length >= 4, "values() at least 4 eras");
        boolean hasHeisei = false;
        for (JapaneseEra e : values) if (e == JapaneseEra.HEISEI) hasHeisei = true;
        check(hasHeisei, "values() contains HEISEI");

        // of() rejects out-of-range values
        expectThrows(java.time.DateTimeException.class, () -> JapaneseEra.of(99), "of(99) invalid");
        expectThrows(java.time.DateTimeException.class, () -> JapaneseEra.of(-99), "of(-99) invalid");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
