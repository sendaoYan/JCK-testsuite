/*
 * @test
 * @summary java.lang.Deprecated API conformance: RUNTIME retention, METHOD target, runtime
 *          presence on a deprecated method, and the Java 9+ since()/forRemoval() members
 *          (via reflection).
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

    static void testRetentionAndTarget() {
        check(Deprecated.class.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "RUNTIME retention");
        boolean hasMethod = false;
        for (ElementType e : Deprecated.class.getAnnotation(Target.class).value()) {
            if (e == ElementType.METHOD) hasMethod = true;
        }
        check(hasMethod, "@Target includes METHOD");
    }

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
        testRetentionAndTarget();
        testPresent();
        testSinceForRemoval();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
