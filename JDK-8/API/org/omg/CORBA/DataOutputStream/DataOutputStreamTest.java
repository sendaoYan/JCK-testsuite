/*
 * @test
 * @summary org.omg.CORBA.DataOutputStream conformance: interface shape.
 * @run main DataOutputStreamTest
 */

public class DataOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.DataOutputStream");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.portable.ValueBase.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ValueBase");

        // ---- deepened: exact method surface (javap-verified on jdk8u372) ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        String[] must = {"write_any", "write_boolean", "write_boolean_array", "write_char", "write_char_array",
                         "write_double", "write_double_array", "write_float", "write_float_array", "write_long",
                         "write_long_array", "write_longlong", "write_longlong_array", "write_octet",
                         "write_octet_array", "write_short", "write_short_array", "write_string", "write_ulong",
                         "write_ulong_array", "write_ulonglong", "write_ulonglong_array", "write_ushort",
                         "write_ushort_array", "write_wchar", "write_wchar_array", "write_wstring",
                         "write_any_array", "write_TypeCode", "write_Object", "write_Abstract", "write_Value"};
        for (String mname : must) check(names.contains(mname), "DataOutputStream declares " + mname);
        check(names.size() == 32, "DataOutputStream declares exactly 32 ops, got " + names.size());
        check(c.getMethod("write_any_array", org.omg.CORBA.Any[].class, int.class, int.class) != null,
              "write_any_array takes (Any[], offset, length)");
        check(c.getMethod("write_Value", java.io.Serializable.class) != null, "write_Value takes Serializable");
        check(c.getMethod("write_Abstract", java.lang.Object.class) != null,
              "write_Abstract takes Object (jdk8u372, unlike write_Value's Serializable)");
        check(c.getMethod("write_TypeCode", org.omg.CORBA.TypeCode.class) != null, "write_TypeCode takes TypeCode");
        check(c.getMethod("write_Object", org.omg.CORBA.Object.class) != null, "write_Object takes Object");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
