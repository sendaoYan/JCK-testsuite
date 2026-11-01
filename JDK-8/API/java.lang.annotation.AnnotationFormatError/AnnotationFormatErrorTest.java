/*
 * @test
 * @summary java.lang.annotation.AnnotationFormatError conformance: constructors/hierarchy.
 * @run main AnnotationFormatErrorTest
 */

import java.lang.annotation.AnnotationFormatError;

public class AnnotationFormatErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAnnotationFormatError() {
        AnnotationFormatError e = new AnnotationFormatError("msg");
        check(e instanceof Error, "is Error");
        check("msg".equals(e.getMessage()), "message");
        AnnotationFormatError e2 = new AnnotationFormatError("msg", new RuntimeException("c"));
        check("msg".equals(e2.getMessage()), "message with cause");
        check(e2.getCause() instanceof RuntimeException, "cause");
    }

    public static void main(String[] args) {
        testAnnotationFormatError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
