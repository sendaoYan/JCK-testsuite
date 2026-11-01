/*
 * @test
 * @summary java.lang.reflect.AnnotatedWildcardType conformance: upper/lower annotated bounds.
 * @run main AnnotatedWildcardTypeTest
 */

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Method;
import java.util.List;

public class AnnotatedWildcardTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void wildcard(List<? extends Number> l) {
    }

    static void testAnnotatedWildcardType() throws Exception {
        Method m = AnnotatedWildcardTypeTest.class.getMethod("wildcard", List.class);
        AnnotatedType at = m.getParameters()[0].getAnnotatedType();
        check(at instanceof AnnotatedParameterizedType, "parameter is parameterized");
        AnnotatedType arg =
                ((AnnotatedParameterizedType) at).getAnnotatedActualTypeArguments()[0];
        check(arg instanceof AnnotatedWildcardType, "argument is AnnotatedWildcardType");
        AnnotatedWildcardType awt = (AnnotatedWildcardType) arg;
        check(awt.getAnnotatedUpperBounds().length == 1, "one annotated upper bound");
        check(awt.getAnnotatedUpperBounds()[0].getType() == Number.class, "upper bound Number");
        check(awt.getAnnotatedLowerBounds().length == 0, "no lower bounds");
    }

    public static void main(String[] args) throws Exception {
        testAnnotatedWildcardType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
