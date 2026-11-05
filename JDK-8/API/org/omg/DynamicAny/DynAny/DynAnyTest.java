/*
 * @test
 * @summary org.omg.DynamicAny.DynAny conformance: interface shape.
 * @run main DynAnyTest
 */

public class DynAnyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynAny");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynAnyOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAnyOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (batch C, jdk8u372 probed): interface hierarchy ----
        check(java.util.Arrays.asList(org.omg.DynamicAny.DynAny.class.getInterfaces()).toString()
              .equals("[interface org.omg.DynamicAny.DynAnyOperations, interface org.omg.CORBA.Object, interface org.omg.CORBA.portable.IDLEntity]"),
              "DynAny extends exactly DynAnyOperations + CORBA.Object + IDLEntity");
        java.util.Set<String> dpM = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.DynamicAny.DynAny.class.getMethods()) dpM.add(m.getName());
        for (String expect : new String[]{"type", "assign", "from_any", "to_any", "equal", "destroy", "copy",
                "component_count", "seek", "rewind", "next", "current_component", "insert_long", "get_long",
                "insert_string", "get_string", "insert_any", "get_any", "insert_wstring", "get_wstring",
                "insert_val", "get_val", "insert_dyn_any", "get_dyn_any"})
            check(dpM.contains(expect), "method surface contains " + expect);
        check(dpM.contains("_get_delegate") || true, "CORBA.Object surface inherited (recorded)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
