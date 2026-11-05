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
        // ---- deepened (batch C, jdk8u372 probed): exact operations surface ----
        java.util.Set<String> dpOwn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.DynamicAny.DynValueCommonOperations.class.getDeclaredMethods()) dpOwn.add(m.getName());
        check(dpOwn.size() == 3, "exactly 3 own operations: " + dpOwn);
        for (String expect : new String[]{"is_null", "set_to_null", "set_to_value"})
            check(dpOwn.contains(expect), "own method " + expect);
        check(org.omg.DynamicAny.DynAnyOperations.class.isAssignableFrom(org.omg.DynamicAny.DynValueCommonOperations.class),
              "extends DynAnyOperations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
