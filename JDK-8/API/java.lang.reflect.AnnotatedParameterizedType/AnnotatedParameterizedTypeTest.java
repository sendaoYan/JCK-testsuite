/*
 * @test
 * @summary java.lang.reflect.AnnotatedParameterizedType conformance: actual type arguments/owner.
 * @run main AnnotatedParameterizedTypeTest
 */

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.List;

public class AnnotatedParameterizedTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static List<String> returnsList() {
        return null;
    }

    static void testAnnotatedParameterizedType() throws Exception {
        AnnotatedType at = AnnotatedParameterizedTypeTest.class.getMethod("returnsList")
                .getAnnotatedReturnType();
        check(at instanceof AnnotatedParameterizedType, "is AnnotatedParameterizedType");
        AnnotatedParameterizedType apt = (AnnotatedParameterizedType) at;
        check(apt.getAnnotatedActualTypeArguments().length == 1, "one actual type argument");
        check(apt.getAnnotatedActualTypeArguments()[0].getType() == String.class,
              "type argument String");
        check(apt.getType() instanceof java.lang.reflect.ParameterizedType,
              "getType is ParameterizedType");
    }

    public static void main(String[] args) throws Exception {
        testAnnotatedParameterizedType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
