/*
 * @test
 * @summary java.lang.ScopedValue conformance: white-box structure + black-box behavior.
 * @run main ScopedValueTest
 */

import java.lang.ScopedValue;
import java.lang.reflect.Modifier;

public class ScopedValueTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ScopedValue");
        check(Modifier.isFinal(c.getModifiers()), "ScopedValue is final");
        check(!c.isInterface(), "ScopedValue is a class");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testUnbound() {
        ScopedValue<String> sv = ScopedValue.newInstance();
        check(!sv.isBound(), "not bound outside");
        check("d".equals(sv.orElse("d")), "orElse default");
        try {
            sv.get();
            throw new AssertionError("unbound get: expected NoSuchElementException");
        } catch (java.util.NoSuchElementException expected) {
            passed++;
        }
        try {
            sv.orElseThrow(() -> new IllegalStateException("unbound"));
            throw new AssertionError("orElseThrow: expected supplier exception");
        } catch (IllegalStateException expected) {
            passed++;
            check("unbound".equals(expected.getMessage()), "orElseThrow supplier message");
        }
        check(sv.hashCode() == sv.hashCode(), "hashCode stable");
    }

    static void testBound() {
        ScopedValue<String> sv = ScopedValue.newInstance();
        ScopedValue.Carrier car = ScopedValue.where(sv, "hello");
        check("hello".equals(car.get(sv)), "carrier get");
        check("hello".equals(car.call(() -> sv.get())), "carrier call");
        final int[] ran = {0};
        car.run(() -> { ran[0]++; check("hello".equals(sv.get()), "run sees value");
                        check(sv.isBound(), "bound inside run"); });
        check(ran[0] == 1, "run executed");
        ScopedValue.Carrier car2 = car.where(sv, "world");
        check("world".equals(car2.get(sv)), "where overrides");
        check("hello".equals(car.get(sv)), "original carrier unchanged");
        check(!sv.isBound(), "still unbound outside");
    }

    static void testWhereMultiple() {
        ScopedValue<String> a = ScopedValue.newInstance();
        ScopedValue<Integer> b = ScopedValue.newInstance();
        ScopedValue.Carrier car = ScopedValue.where(a, "x").where(b, 3);
        check("x".equals(car.get(a)) && car.get(b) == 3, "chained where");
        car.run(() -> check("x".equals(a.get()) && b.get() == 3, "both values bound"));
    }

    
    static void testBlackBox() throws Exception {
        testUnbound();
        testBound();
        testWhereMultiple();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
