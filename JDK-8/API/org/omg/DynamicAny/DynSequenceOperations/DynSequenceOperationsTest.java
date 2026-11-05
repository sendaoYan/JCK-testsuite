/*
 * @test
 * @summary org.omg.DynamicAny.DynSequenceOperations conformance: interface shape.
 * @run main DynSequenceOperationsTest
 */

public class DynSequenceOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynSequenceOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynAnyOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAnyOperations");
        // ---- deepened (batch C, jdk8u372 probed): exact operations surface ----
        java.util.Set<String> dpOwn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.DynamicAny.DynSequenceOperations.class.getDeclaredMethods()) dpOwn.add(m.getName());
        check(dpOwn.size() == 6, "exactly 6 own operations: " + dpOwn);
        for (String expect : new String[]{"get_length", "set_length", "get_elements", "set_elements",
                "get_elements_as_dyn_any", "set_elements_as_dyn_any"})
            check(dpOwn.contains(expect), "own method " + expect);

        System.out.println("PASS (" + passed + " assertions)");
    }
}
