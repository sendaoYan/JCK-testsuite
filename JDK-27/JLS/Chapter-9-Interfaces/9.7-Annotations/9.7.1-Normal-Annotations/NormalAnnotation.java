/*
 * @test
 * @summary JLS 9.7.1: a normal annotation supplies an element-value pair for each
 *          element of the annotation interface.
 * @run main NormalAnnotation
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class NormalAnnotation {
    @Retention(RetentionPolicy.RUNTIME)
    @interface Rfe {
        int id();
        String synopsis();
    }

    @Rfe(id = 1, synopsis = "improve type inference")
    static class C {}

    public static void main(String[] args) {
        Rfe r = C.class.getAnnotation(Rfe.class);
        if (r.id() != 1 || !"improve type inference".equals(r.synopsis()))
            throw new AssertionError("normal annotation values");
        System.out.println("PASS");
    }
}
