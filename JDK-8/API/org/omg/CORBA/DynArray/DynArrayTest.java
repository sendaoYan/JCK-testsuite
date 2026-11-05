/*
 * @test
 * @summary org.omg.CORBA.DynArray conformance: interface shape.
 * @run main DynArrayTest
 */

public class DynArrayTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.DynArray");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.DynAny.class.isAssignableFrom(c), "extends org.omg.CORBA.DynAny");

        // ---- deepened: exact own method surface (javap-verified on jdk8u372, deprecated org.omg.CORBA.DynAny family) ----
        java.util.Set<String> own = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CORBA.DynArray.class.getDeclaredMethods()) own.add(m.getName());
        java.util.Set<String> expected = new java.util.HashSet<String>(java.util.Arrays.asList("get_elements", "set_elements"));
        check(own.equals(expected), "DynArray declares exactly 2 own ops: " + own);
        check(org.omg.CORBA.DynAny.class.isAssignableFrom(org.omg.CORBA.DynArray.class), "DynArray extends org.omg.CORBA.DynAny (old family)");
        check(org.omg.CORBA.Object.class.isAssignableFrom(org.omg.CORBA.DynArray.class), "DynArray extends org.omg.CORBA.Object");
        check(org.omg.CORBA.DynArray.class.isAnnotationPresent(Deprecated.class), "DynArray is @Deprecated (superseded by org.omg.DynamicAny)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
