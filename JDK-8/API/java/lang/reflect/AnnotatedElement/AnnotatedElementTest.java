/*
 * @test
 * @summary java.lang.reflect.AnnotatedElement conformance: annotation accessors.
 * @run main AnnotatedElementTest
 */

import java.lang.reflect.AnnotatedElement;

public class AnnotatedElementTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @Deprecated
    static class Annotated {
    }

    static class Plain {
    }

    static void testAnnotatedElement() {
        AnnotatedElement e = Annotated.class;
        check(e.isAnnotationPresent(Deprecated.class), "isAnnotationPresent");
        check(e.getAnnotation(Deprecated.class) != null, "getAnnotation");
        check(e.getDeclaredAnnotation(Deprecated.class) != null, "getDeclaredAnnotation");
        check(e.getAnnotations().length == 1, "getAnnotations length");
        check(e.getDeclaredAnnotations().length == 1, "getDeclaredAnnotations length");
        check(e.getAnnotationsByType(Deprecated.class).length == 1, "getAnnotationsByType");
        check(e.getDeclaredAnnotationsByType(Deprecated.class).length == 1,
              "getDeclaredAnnotationsByType");

        AnnotatedElement p = Plain.class;
        check(!p.isAnnotationPresent(Deprecated.class), "plain not annotated");
        check(p.getAnnotation(Deprecated.class) == null, "plain getAnnotation null");
        check(p.getAnnotations().length == 0, "plain getAnnotations");
    }

    public static void main(String[] args) {
        testAnnotatedElement();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
