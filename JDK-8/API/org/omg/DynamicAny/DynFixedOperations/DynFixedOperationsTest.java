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
        // ---- deepened (batch C, jdk8u372 probed): exact operations surface ----
        java.util.Set<String> dpOwn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.DynamicAny.DynFixedOperations.class.getDeclaredMethods()) dpOwn.add(m.getName());
        check(dpOwn.size() == 2, "exactly 2 own operations: " + dpOwn);
        for (String expect : new String[]{"get_value", "set_value"}) check(dpOwn.contains(expect), "own method " + expect);
        try {
            java.lang.reflect.Method sv = org.omg.DynamicAny.DynFixedOperations.class.getMethod("set_value", String.class);
            check(sv.getReturnType() == boolean.class, "set_value returns boolean (digits/scale adjusted)");
        } catch (NoSuchMethodException impossible) {
            throw new AssertionError("set_value missing");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
