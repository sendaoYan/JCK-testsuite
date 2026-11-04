/*
 * @test
 * @summary JLS 9.7.3 (Java SE 8): a single-element annotation is a shorthand for a
 *          normal annotation with a single element named value.
 * @run main SingleElementAnnotation
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SingleElementAnnotation {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface A { int value(); }

    @A(5)
    static class C {}

    public static void main(String[] args) throws Exception {
        A a = C.class.getAnnotation(A.class);
        check(a.value() == 5, "single element annotation value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
