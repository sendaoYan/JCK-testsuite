/*
 * @test
 * @summary JLS 9.6.2: an annotation interface element may have a default value,
 *          which is applied when the element is omitted from an annotation.
 * @run main AnnotationElementDefault
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnnotationElementDefault {
    @Retention(RetentionPolicy.RUNTIME)
    @interface Rfe {
        int id();
        String synopsis();
        String engineer() default "[unassigned]";
        String date() default "[unimplemented]";
    }

    @Rfe(id = 1, synopsis = "balance the budget")
    static class C {}

    public static void main(String[] args) {
        Rfe r = C.class.getAnnotation(Rfe.class);
        if (r.id() != 1 || !"balance the budget".equals(r.synopsis()))
            throw new AssertionError("explicit values");
        if (!"[unassigned]".equals(r.engineer())) throw new AssertionError("default engineer");
        if (!"[unimplemented]".equals(r.date())) throw new AssertionError("default date");
        System.out.println("PASS");
    }
}
