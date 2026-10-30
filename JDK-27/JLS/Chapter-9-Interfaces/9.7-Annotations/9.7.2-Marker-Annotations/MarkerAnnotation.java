/*
 * @test
 * @summary JLS 9.7.2: a marker annotation has no elements and may be written
 *          without a parenthesized element-value list.
 * @run main MarkerAnnotation
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MarkerAnnotation {
    @Retention(RetentionPolicy.RUNTIME)
    @interface Marker {}

    @Marker
    static class C {}

    public static void main(String[] args) {
        if (C.class.getAnnotation(Marker.class) == null)
            throw new AssertionError("marker annotation not present");
        System.out.println("PASS");
    }
}
