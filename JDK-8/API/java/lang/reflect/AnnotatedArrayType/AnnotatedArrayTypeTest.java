/*
 * @test
 * @summary java.lang.reflect.AnnotatedArrayType conformance: component/owner on an array return
 *          type.
 * @run main AnnotatedArrayTypeTest
 */

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;

public class AnnotatedArrayTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static int[] returnsArray() {
        return null;
    }

    static void testAnnotatedArrayType() throws Exception {
        AnnotatedType at = AnnotatedArrayTypeTest.class.getMethod("returnsArray")
                .getAnnotatedReturnType();
        check(at instanceof AnnotatedArrayType, "is AnnotatedArrayType");
        AnnotatedArrayType aat = (AnnotatedArrayType) at;
        check(aat.getType() instanceof Class, "getType is Class");
        check(((Class) aat.getType()).getComponentType() == int.class, "component int");
        check(aat.getAnnotatedGenericComponentType() != null, "getAnnotatedGenericComponentType");
        check(aat.getAnnotations().length == 0, "getAnnotations");
    }

    public static void main(String[] args) throws Exception {
        testAnnotatedArrayType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
