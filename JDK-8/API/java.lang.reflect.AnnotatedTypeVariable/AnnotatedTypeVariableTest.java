/*
 * @test
 * @summary java.lang.reflect.AnnotatedTypeVariable conformance: annotated bounds on a type
 *          variable parameter.
 * @run main AnnotatedTypeVariableTest
 */

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.Method;

public class AnnotatedTypeVariableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static <T extends Number> void typeVar(T t) {
    }

    static void testAnnotatedTypeVariable() throws Exception {
        Method m = AnnotatedTypeVariableTest.class.getMethod("typeVar", Number.class);
        AnnotatedType at = m.getParameters()[0].getAnnotatedType();
        check(at instanceof AnnotatedTypeVariable, "is AnnotatedTypeVariable");
        AnnotatedTypeVariable atv = (AnnotatedTypeVariable) at;
        check(atv.getAnnotatedBounds().length == 1, "one annotated bound");
        check(atv.getAnnotatedBounds()[0].getType() == Number.class, "bound is Number");
        check(atv.getType() instanceof java.lang.reflect.TypeVariable, "getType is TypeVariable");
    }

    public static void main(String[] args) throws Exception {
        testAnnotatedTypeVariable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
