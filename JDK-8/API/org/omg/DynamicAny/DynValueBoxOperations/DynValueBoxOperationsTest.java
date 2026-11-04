/*
 * @test
 * @summary org.omg.DynamicAny.DynValueBoxOperations conformance: interface shape.
 * @run main DynValueBoxOperationsTest
 */

public class DynValueBoxOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynValueBoxOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynValueCommonOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynValueCommonOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
