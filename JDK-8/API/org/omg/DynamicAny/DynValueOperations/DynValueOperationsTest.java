/*
 * @test
 * @summary org.omg.DynamicAny.DynValueOperations conformance: interface shape.
 * @run main DynValueOperationsTest
 */

public class DynValueOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynValueOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynValueCommonOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynValueCommonOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
