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
        // ---- deepened (batch C, jdk8u372 probed): exact operations surface ----
        java.util.Set<String> dpOwn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.DynamicAny.DynArrayOperations.class.getDeclaredMethods()) dpOwn.add(m.getName());
        check(dpOwn.size() == 4, "exactly 4 own operations: " + dpOwn);
        for (String expect : new String[]{"get_elements", "set_elements", "get_elements_as_dyn_any", "set_elements_as_dyn_any"})
            check(dpOwn.contains(expect), "own method " + expect);
        check(org.omg.DynamicAny.DynAnyOperations.class.isAssignableFrom(org.omg.DynamicAny.DynArrayOperations.class),
              "extends DynAnyOperations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
