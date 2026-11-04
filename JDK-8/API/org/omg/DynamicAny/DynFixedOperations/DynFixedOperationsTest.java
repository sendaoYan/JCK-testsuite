/*
 * @test
 * @summary org.omg.DynamicAny.DynFixedOperations conformance: interface shape.
 * @run main DynFixedOperationsTest
 */

public class DynFixedOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynFixedOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynAnyOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAnyOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
