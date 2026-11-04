/*
 * @test
 * @summary JLS 9.7.1 (Java SE 8): a normal annotation lists element-value pairs for
 *          each element of the annotation interface.
 * @run main NormalAnnotation
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class NormalAnnotation {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface A { int x(); String y(); }

    @A(x = 1, y = "a")
    static class C {}

    public static void main(String[] args) throws Exception {
        A a = C.class.getAnnotation(A.class);
        check(a.x() == 1 && a.y().equals("a"), "normal annotation elements");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
