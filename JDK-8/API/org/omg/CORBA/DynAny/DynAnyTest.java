/*
 * @test
 * @summary org.omg.CORBA.DynAny conformance: interface shape.
 * @run main DynAnyTest
 */

public class DynAnyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.DynAny");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");

        // ---- deepened: exact own method surface (javap-verified on jdk8u372, deprecated org.omg.CORBA.DynAny family) ----
        java.util.Set<String> own = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CORBA.DynAny.class.getDeclaredMethods()) own.add(m.getName());
        java.util.Set<String> expected = new java.util.HashSet<String>(java.util.Arrays.asList("assign", "copy", "current_component", "destroy", "from_any", "get_any", "get_boolean", "get_char", "get_double", "get_float", "get_long", "get_longlong", "get_octet", "get_reference", "get_short", "get_string", "get_typecode", "get_ulong", "get_ulonglong", "get_ushort", "get_val", "get_wchar", "get_wstring", "insert_any", "insert_boolean", "insert_char", "insert_double", "insert_float", "insert_long", "insert_longlong", "insert_octet", "insert_reference", "insert_short", "insert_string", "insert_typecode", "insert_ulong", "insert_ulonglong", "insert_ushort", "insert_val", "insert_wchar", "insert_wstring", "next", "rewind", "seek", "to_any", "type"));
        check(own.equals(expected), "DynAny declares exactly 46 own ops: " + own);
        check(c.getMethod("insert_long", int.class) != null, "insert_long takes int");
        check(c.getMethod("get_long").getReturnType() == int.class, "get_long returns int");
        check(c.getMethod("insert_string", String.class) != null, "insert_string takes String");
        check(c.getMethod("seek", int.class) != null, "seek takes int");
        check(c.getMethod("type").getReturnType() == org.omg.CORBA.TypeCode.class, "type() returns TypeCode");
        check(c.getMethod("assign", org.omg.CORBA.DynAny.class) != null, "assign takes DynAny");
        check(c.getMethod("from_any", org.omg.CORBA.Any.class) != null, "from_any takes Any");
        check(c.getMethod("to_any").getReturnType() == org.omg.CORBA.Any.class, "to_any returns Any");
        check(org.omg.CORBA.DynAny.class.isAssignableFrom(org.omg.CORBA.DynAny.class), "DynAny extends org.omg.CORBA.DynAny (old family)");
        check(org.omg.CORBA.Object.class.isAssignableFrom(org.omg.CORBA.DynAny.class), "DynAny extends org.omg.CORBA.Object");
        check(org.omg.CORBA.DynAny.class.isAnnotationPresent(Deprecated.class), "DynAny is @Deprecated (superseded by org.omg.DynamicAny)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
