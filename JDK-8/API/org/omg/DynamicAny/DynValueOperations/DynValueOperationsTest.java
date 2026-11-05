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
        // ---- deepened (batch C, jdk8u372 probed): exact operations surface ----
        java.util.Set<String> dpOwn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.DynamicAny.DynValueOperations.class.getDeclaredMethods()) dpOwn.add(m.getName());
        check(dpOwn.size() == 6, "exactly 6 own operations: " + dpOwn);
        for (String expect : new String[]{"current_member_name", "current_member_kind",
                "get_members", "set_members", "get_members_as_dyn_any", "set_members_as_dyn_any"})
            check(dpOwn.contains(expect), "own method " + expect);
        check(org.omg.DynamicAny.DynAnyOperations.class.isAssignableFrom(org.omg.DynamicAny.DynValueOperations.class),
              "extends DynAnyOperations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
