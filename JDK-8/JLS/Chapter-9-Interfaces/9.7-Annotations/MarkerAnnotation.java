/*
 * @test
 * @summary JLS 9.7.2 (Java SE 8): a marker annotation is a shorthand for an annotation
 *          whose elements all have defaults.
 * @run main MarkerAnnotation
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MarkerAnnotation {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Marker {}

    @Marker
    static class C {}

    public static void main(String[] args) {
        check(C.class.isAnnotationPresent(Marker.class), "marker annotation present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
