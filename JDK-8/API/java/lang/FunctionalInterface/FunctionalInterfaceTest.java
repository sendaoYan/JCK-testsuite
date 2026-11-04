/*
 * @test
 * @summary java.lang.FunctionalInterface API conformance: RUNTIME retention, TYPE target,
 *          usable as a lambda target, and reflected presence on an annotated interface.
 * @run main FunctionalInterfaceTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class FunctionalInterfaceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @FunctionalInterface
    interface FI { int apply(int x); }

    static void testRetentionAndTarget() {
        check(FunctionalInterface.class.getAnnotation(Retention.class).value()
              == RetentionPolicy.RUNTIME, "RUNTIME retention");
        boolean hasType = false;
        for (ElementType e : FunctionalInterface.class.getAnnotation(Target.class).value()) {
            if (e == ElementType.TYPE) hasType = true;
        }
        check(hasType, "@Target includes TYPE");
    }

    static void testUsableAsLambda() {
        FI f = x -> x * 2;
        check(f.apply(3) == 6, "@FunctionalInterface type used as a lambda target");
    }

    static void testPresent() {
        check(FI.class.isAnnotationPresent(FunctionalInterface.class),
              "annotated interface carries @FunctionalInterface");
    }

    public static void main(String[] args) {
        testRetentionAndTarget();
        testUsableAsLambda();
        testPresent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
