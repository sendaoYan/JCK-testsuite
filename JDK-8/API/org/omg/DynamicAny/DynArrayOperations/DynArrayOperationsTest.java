/*
 * @test
 * @summary org.omg.DynamicAny.DynArrayOperations conformance: interface shape.
 * @run main DynArrayOperationsTest
 */

public class DynArrayOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynArrayOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynAnyOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAnyOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
