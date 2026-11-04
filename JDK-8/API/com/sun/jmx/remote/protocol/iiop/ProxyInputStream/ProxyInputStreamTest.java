/*
 * @test
 * @summary com.sun.jmx.remote.protocol.iiop.ProxyInputStream conformance: hierarchy and declared members via reflection.
 * @run main ProxyInputStreamTest
 */

public class ProxyInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.protocol.iiop.ProxyInputStream", false, null);
        check(c.getName().equals("com.sun.jmx.remote.protocol.iiop.ProxyInputStream"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA_2_3.portable.InputStream"), "superclass org.omg.CORBA_2_3.portable.InputStream");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getProxiedInputStream"), "declares getProxiedInputStream");
        check(ms.contains("narrow"), "declares narrow");
        check(ms.contains("orb"), "declares orb");
        check(ms.contains("read"), "declares read");
        check(ms.contains("read_Context"), "declares read_Context");
        check(ms.contains("read_Object"), "declares read_Object");
        check(ms.contains("read_Principal"), "declares read_Principal");
        check(ms.contains("read_TypeCode"), "declares read_TypeCode");
        check(ms.contains("read_abstract_interface"), "declares read_abstract_interface");
        check(ms.contains("read_any"), "declares read_any");
        check(ms.contains("read_boolean"), "declares read_boolean");
        check(ms.contains("read_boolean_array"), "declares read_boolean_array");
        check(ms.contains("read_char"), "declares read_char");
        check(ms.contains("read_char_array"), "declares read_char_array");
        check(ms.contains("read_double"), "declares read_double");
        check(ms.contains("read_double_array"), "declares read_double_array");
        check(ms.contains("read_fixed"), "declares read_fixed");
        check(ms.contains("read_float"), "declares read_float");
        check(ms.contains("read_float_array"), "declares read_float_array");
        check(ms.contains("read_long"), "declares read_long");
        check(ms.contains("read_long_array"), "declares read_long_array");
        check(ms.contains("read_longlong"), "declares read_longlong");
        check(ms.contains("read_longlong_array"), "declares read_longlong_array");
        check(ms.contains("read_octet"), "declares read_octet");
        check(ms.contains("read_octet_array"), "declares read_octet_array");
        check(ms.contains("read_short"), "declares read_short");
        check(ms.contains("read_short_array"), "declares read_short_array");
        check(ms.contains("read_string"), "declares read_string");
        check(ms.contains("read_ulong"), "declares read_ulong");
        check(ms.contains("read_ulong_array"), "declares read_ulong_array");
        check(ms.contains("read_ulonglong"), "declares read_ulonglong");
        check(ms.contains("read_ulonglong_array"), "declares read_ulonglong_array");
        check(ms.contains("read_ushort"), "declares read_ushort");
        check(ms.contains("read_ushort_array"), "declares read_ushort_array");
        check(ms.contains("read_value"), "declares read_value");
        check(ms.contains("read_wchar"), "declares read_wchar");
        check(ms.contains("read_wchar_array"), "declares read_wchar_array");
        check(ms.contains("read_wstring"), "declares read_wstring");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
