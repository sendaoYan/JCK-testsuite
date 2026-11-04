/*
 * @test
 * @summary java.text.Annotation conformance: value/equals/hashCode/toString.
 * @run main AnnotationTest
 */

import java.text.Annotation;

public class AnnotationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAnnotation() {
        Annotation a = new Annotation("value");
        check("value".equals(a.getValue()), "getValue");
        check(a.toString() != null && a.toString().length() > 0, "toString");
        Annotation withInt = new Annotation(42);
        check(Integer.valueOf(42).equals(withInt.getValue()), "getValue Integer");
        check(withInt.toString() != null, "toString Integer");
    }

    public static void main(String[] args) {
        testAnnotation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
