/*
 * @test
 * @summary org.omg.DynamicAny.DynUnionOperations conformance: interface shape.
 * @run main DynUnionOperationsTest
 */

public class DynUnionOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynUnionOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynAnyOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAnyOperations");
        // ---- deepened (batch C, jdk8u372 probed): exact operations surface ----
        java.util.Set<String> dpOwn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.DynamicAny.DynUnionOperations.class.getDeclaredMethods()) dpOwn.add(m.getName());
        check(dpOwn.size() == 9, "exactly 9 own operations: " + dpOwn);
        for (String expect : new String[]{"get_discriminator", "set_discriminator", "set_to_default_member",
                "set_to_no_active_member", "has_no_active_member", "discriminator_kind", "member_kind", "member", "member_name"})
            check(dpOwn.contains(expect), "own method " + expect);

        System.out.println("PASS (" + passed + " assertions)");
    }
}
