/*
 * @test
 * @summary java.lang.FunctionalInterface conformance: white-box structure + black-box behavior
 *          (RUNTIME retention, TYPE target, usable as lambda target, reflected presence).
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        Class<?> c = FunctionalInterface.class;
        check(c.isAnnotation(), "FunctionalInterface is an annotation type");
        check(c.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "RUNTIME retention");
        boolean hasType = false;
        for (ElementType e : c.getAnnotation(Target.class).value()) {
            if (e == ElementType.TYPE) hasType = true;
        }
        check(hasType, "@Target includes TYPE");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testUsableAsLambda() {
        FI f = x -> x * 2;
        check(f.apply(3) == 6, "@FunctionalInterface type used as a lambda target");
    }

    static void testPresent() {
        check(FI.class.isAnnotationPresent(FunctionalInterface.class),
              "annotated interface carries @FunctionalInterface");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testUsableAsLambda();
        testPresent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
