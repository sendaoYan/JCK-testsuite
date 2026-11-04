/*
 * @test
 * @summary org.omg.DynamicAny.DynValueCommonOperations conformance: interface shape.
 * @run main DynValueCommonOperationsTest
 */

public class DynValueCommonOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynValueCommonOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynAnyOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAnyOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
