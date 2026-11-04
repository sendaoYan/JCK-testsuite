/*
 * @test
 * @summary java.lang.annotation.AnnotationTypeMismatchException conformance: element/foundType.
 * @run main AnnotationTypeMismatchExceptionTest
 */

import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.reflect.Method;

public class AnnotationTypeMismatchExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAnnotationTypeMismatchException() throws Exception {
        Method m = Object.class.getMethod("toString");
        AnnotationTypeMismatchException e = new AnnotationTypeMismatchException(m, "int");
        check(e.element() == m, "element");
        check("int".equals(e.foundType()), "foundType");
    }

    public static void main(String[] args) throws Exception {
        testAnnotationTypeMismatchException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
