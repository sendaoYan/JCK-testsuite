/*
 * @test
 * @summary java.lang.Override API conformance: SOURCE retention, METHOD target, and
 *          source-retained absence of the annotation at runtime.
 * @run main OverrideTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class OverrideTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Base { void m() {} }
    static class Sub extends Base { @Override void m() {} }

    static void testRetentionAndTarget() {
        check(Override.class.getAnnotation(Retention.class).value() == RetentionPolicy.SOURCE,
              "SOURCE retention");
        boolean hasMethod = false;
        for (ElementType e : Override.class.getAnnotation(Target.class).value()) {
            if (e == ElementType.METHOD) hasMethod = true;
        }
        check(hasMethod, "@Target includes METHOD");
    }

    static void testSourceRetained() throws Exception {
        check(!Sub.class.getDeclaredMethod("m").isAnnotationPresent(Override.class),
              "@Override is source-retained (absent at runtime)");
    }

    public static void main(String[] args) throws Exception {
        testRetentionAndTarget();
        testSourceRetained();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
