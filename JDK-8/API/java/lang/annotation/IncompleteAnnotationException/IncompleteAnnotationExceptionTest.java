/*
 * @test
 * @summary java.lang.annotation.IncompleteAnnotationException conformance: annotationType/
 *          elementName.
 * @run main IncompleteAnnotationExceptionTest
 */

import java.lang.annotation.IncompleteAnnotationException;
import java.lang.annotation.Retention;

public class IncompleteAnnotationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIncompleteAnnotationException() {
        IncompleteAnnotationException e = new IncompleteAnnotationException(Retention.class, "value");
        check(e.annotationType() == Retention.class, "annotationType");
        check("value".equals(e.elementName()), "elementName");
    }

    public static void main(String[] args) {
        testIncompleteAnnotationException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
