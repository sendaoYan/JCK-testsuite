/*
 * @test
 * @summary JLS 9.6.2 (Java SE 8): an annotation interface element may have a default
 *          value, which is used when the element is omitted from an annotation.
 * @run main AnnotationElementDefault
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnnotationElementDefault {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface A {
        int value() default 42;
        String name() default "dflt";
    }

    @A static class WithDefault {}
    @A(value = 7, name = "x") static class WithExplicit {}

    public static void main(String[] args) throws Exception {
        A d = WithDefault.class.getAnnotation(A.class);
        check(d.value() == 42 && d.name().equals("dflt"), "defaults applied");
        A e = WithExplicit.class.getAnnotation(A.class);
        check(e.value() == 7 && e.name().equals("x"), "explicit values override defaults");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
