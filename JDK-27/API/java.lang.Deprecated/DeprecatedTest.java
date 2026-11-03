/*
 * @test
 * @summary java.lang.Deprecated conformance: white-box structure + black-box behavior
 *          (RUNTIME retention, METHOD target, runtime presence, Java 9+ since()/forRemoval()).
 * @run main DeprecatedTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class DeprecatedTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @Deprecated
    static void old() {}

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        Class<?> c = Deprecated.class;
        check(c.isAnnotation(), "Deprecated is an annotation type");
        check(c.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "RUNTIME retention");
        boolean hasMethod = false, hasType = false;
        for (ElementType e : c.getAnnotation(Target.class).value()) {
            if (e == ElementType.METHOD) hasMethod = true;
            if (e == ElementType.TYPE) hasType = true;
        }
        check(hasMethod && hasType, "@Target includes METHOD and TYPE");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testPresent() throws Exception {
        check(DeprecatedTest.class.getDeclaredMethod("old").isAnnotationPresent(Deprecated.class),
              "@Deprecated retained at runtime on a method");
    }

    static void testSinceForRemoval() {
        try {
            java.lang.reflect.Method since = Deprecated.class.getMethod("since");
            java.lang.reflect.Method fr = Deprecated.class.getMethod("forRemoval");
            check(since.getReturnType() == String.class, "since() returns String [Java 9+]");
            check(fr.getReturnType() == boolean.class, "forRemoval() returns boolean [Java 9+]");
        } catch (NoSuchMethodException e) {
            // JDK 8: since()/forRemoval() are absent.
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testPresent();
        testSinceForRemoval();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
