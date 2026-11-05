/*
 * @test
 * @summary org.omg.CORBA.DynValue conformance: interface shape.
 * @run main DynValueTest
 */

public class DynValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.DynValue");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.DynAny.class.isAssignableFrom(c), "extends org.omg.CORBA.DynAny");

        // ---- deepened: exact own method surface (javap-verified on jdk8u372, deprecated org.omg.CORBA.DynAny family) ----
        java.util.Set<String> own = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CORBA.DynValue.class.getDeclaredMethods()) own.add(m.getName());
        java.util.Set<String> expected = new java.util.HashSet<String>(java.util.Arrays.asList("current_member_kind", "current_member_name", "get_members", "set_members"));
        check(own.equals(expected), "DynValue declares exactly 4 own ops: " + own);
        check(org.omg.CORBA.DynAny.class.isAssignableFrom(org.omg.CORBA.DynValue.class), "DynValue extends org.omg.CORBA.DynAny (old family)");
        check(org.omg.CORBA.Object.class.isAssignableFrom(org.omg.CORBA.DynValue.class), "DynValue extends org.omg.CORBA.Object");
        check(org.omg.CORBA.DynValue.class.isAnnotationPresent(Deprecated.class), "DynValue is @Deprecated (superseded by org.omg.DynamicAny)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
