/*
 * @test
 * @summary org.omg.CORBA.DynEnum conformance: interface shape.
 * @run main DynEnumTest
 */

public class DynEnumTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.DynEnum");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.DynAny.class.isAssignableFrom(c), "extends org.omg.CORBA.DynAny");

        // ---- deepened: exact own method surface (javap-verified on jdk8u372, deprecated org.omg.CORBA.DynAny family) ----
        java.util.Set<String> own = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CORBA.DynEnum.class.getDeclaredMethods()) own.add(m.getName());
        java.util.Set<String> expected = new java.util.HashSet<String>(java.util.Arrays.asList("value_as_string", "value_as_ulong"));
        check(own.equals(expected), "DynEnum declares exactly 2 own ops: " + own);
        check(c.getMethod("value_as_string").getReturnType() == String.class, "value_as_string() returns String");
        check(c.getMethod("value_as_string", String.class) != null, "value_as_string(String) setter exists");
        check(c.getMethod("value_as_ulong").getReturnType() == int.class, "value_as_ulong() returns int");
        check(c.getMethod("value_as_ulong", int.class) != null, "value_as_ulong(int) setter exists");
        check(org.omg.CORBA.DynAny.class.isAssignableFrom(org.omg.CORBA.DynEnum.class), "DynEnum extends org.omg.CORBA.DynAny (old family)");
        check(org.omg.CORBA.Object.class.isAssignableFrom(org.omg.CORBA.DynEnum.class), "DynEnum extends org.omg.CORBA.Object");
        check(org.omg.CORBA.DynEnum.class.isAnnotationPresent(Deprecated.class), "DynEnum is @Deprecated (superseded by org.omg.DynamicAny)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
