/*
 * @test
 * @summary org.omg.CORBA.DataInputStream conformance: interface shape.
 * @run main DataInputStreamTest
 */

public class DataInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.DataInputStream");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.portable.ValueBase.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ValueBase");

        // ---- deepened: exact method surface (javap-verified on jdk8u372) ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        String[] must = {"read_any", "read_boolean", "read_boolean_array", "read_char", "read_char_array",
                         "read_double", "read_double_array", "read_float", "read_float_array", "read_long",
                         "read_long_array", "read_longlong", "read_longlong_array", "read_octet",
                         "read_octet_array", "read_short", "read_short_array", "read_string", "read_ulong",
                         "read_ulong_array", "read_ulonglong", "read_ulonglong_array", "read_ushort",
                         "read_ushort_array", "read_wchar", "read_wchar_array", "read_wstring",
                         "read_any_array", "read_TypeCode", "read_Object", "read_Abstract", "read_Value"};
        for (String mname : must) check(names.contains(mname), "DataInputStream declares " + mname);
        check(names.size() == 32, "DataInputStream declares exactly 32 ops, got " + names.size());
        check(c.getMethod("read_any_array", org.omg.CORBA.AnySeqHolder.class, int.class, int.class) != null,
              "read_any_array takes (AnySeqHolder, offset, length) (jdk8u372)");
        check(c.getMethod("read_Value").getReturnType() == java.io.Serializable.class,
              "read_Value returns Serializable");
        check(c.getMethod("read_Object").getReturnType() == org.omg.CORBA.Object.class,
              "read_Object returns org.omg.CORBA.Object");
        check(c.getMethod("read_TypeCode").getReturnType() == org.omg.CORBA.TypeCode.class,
              "read_TypeCode returns TypeCode");
        check(c.getMethod("read_Abstract").getReturnType() == java.lang.Object.class,
              "read_Abstract returns Object");
        check(c.getMethod("read_string").getReturnType() == String.class, "read_string returns String");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
