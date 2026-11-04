/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyFactoryOperations conformance: interface shape.
 * @run main DynAnyFactoryOperationsTest
 */

public class DynAnyFactoryOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynAnyFactoryOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
