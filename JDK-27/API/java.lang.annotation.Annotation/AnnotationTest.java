/*
 * @test
 * @summary java.lang.annotation.Annotation conformance: annotationType/equals/hashCode/toString.
 * @run main AnnotationTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnnotationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Marker {
    }

    @Marker
    static class Annotated {
    }

    static void testAnnotation() {
        Annotation a = Annotated.class.getAnnotation(Marker.class);
        check(a != null, "annotation present");
        check(a.annotationType() == Marker.class, "annotationType");
        check(a.equals(Annotated.class.getAnnotation(Marker.class)), "equals");
        check(a.hashCode() == Annotated.class.getAnnotation(Marker.class).hashCode(), "hashCode");
        check(a.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testAnnotation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
