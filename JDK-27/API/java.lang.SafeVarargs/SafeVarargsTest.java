/*
 * @test
 * @summary java.lang.SafeVarargs API conformance: RUNTIME retention, CONSTRUCTOR/METHOD
 *          targets, and usable on a static method to suppress a varargs warning.
 * @run main SafeVarargsTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class SafeVarargsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @SafeVarargs
    static final <T> java.util.List<T> asList(T... a) { return java.util.Arrays.asList(a); }

    static void testRetentionAndTarget() {
        check(SafeVarargs.class.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "RUNTIME retention");
        boolean hasMethod = false, hasCtor = false;
        for (ElementType e : SafeVarargs.class.getAnnotation(Target.class).value()) {
            if (e == ElementType.METHOD) hasMethod = true;
            if (e == ElementType.CONSTRUCTOR) hasCtor = true;
        }
        check(hasMethod && hasCtor, "@Target includes METHOD and CONSTRUCTOR");
    }

    static void testUsable() {
        java.util.List<Integer> xs = asList(1, 2, 3);
        check(xs.size() == 3, "@SafeVarargs method works normally");
    }

    public static void main(String[] args) {
        testRetentionAndTarget();
        testUsable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
