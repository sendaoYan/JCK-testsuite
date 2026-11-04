/*
 * @test
 * @summary java.lang.reflect.AnnotatedType conformance: getType/getAnnotations on a return type.
 * @run main AnnotatedTypeTest
 */

import java.lang.reflect.AnnotatedType;

public class AnnotatedTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static String returnsString() {
        return null;
    }

    static void testAnnotatedType() throws Exception {
        AnnotatedType at = AnnotatedTypeTest.class.getMethod("returnsString")
                .getAnnotatedReturnType();
        check(at != null, "getAnnotatedReturnType non-null");
        check(at.getType() == String.class, "getType == String");
        check(at.getAnnotations().length == 0, "getAnnotations");
        check(at.getDeclaredAnnotations().length == 0, "getDeclaredAnnotations");
    }

    public static void main(String[] args) throws Exception {
        testAnnotatedType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
