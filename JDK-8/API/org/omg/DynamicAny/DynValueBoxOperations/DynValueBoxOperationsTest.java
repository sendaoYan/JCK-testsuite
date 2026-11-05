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
        // ---- deepened (batch C, jdk8u372 probed): exact operations surface ----
        java.util.Set<String> dpOwn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.DynamicAny.DynValueBoxOperations.class.getDeclaredMethods()) dpOwn.add(m.getName());
        check(dpOwn.size() == 4, "exactly 4 own operations: " + dpOwn);
        for (String expect : new String[]{"get_boxed_value", "set_boxed_value", "get_boxed_value_as_dyn_any", "set_boxed_value_as_dyn_any"})
            check(dpOwn.contains(expect), "own method " + expect);
        check(org.omg.DynamicAny.DynAnyOperations.class.isAssignableFrom(org.omg.DynamicAny.DynValueBoxOperations.class),
              "extends DynAnyOperations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
