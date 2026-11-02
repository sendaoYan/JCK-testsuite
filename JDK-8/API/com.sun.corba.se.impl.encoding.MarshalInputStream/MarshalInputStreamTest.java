/*
 * @test
 * @summary com.sun.corba.se.impl.encoding.MarshalInputStream conformance: type shape via reflection (headless-safe).
 * @run main MarshalInputStreamTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MarshalInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.encoding.MarshalInputStream", false, MarshalInputStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 37, "public declared method count");
        check(mn.contains("consumeEndian"), "method consumeEndian");
        check(mn.contains("getPosition"), "method getPosition");
        check(mn.contains("mark"), "method mark");
        check(mn.contains("performORBVersionSpecificInit"), "method performORBVersionSpecificInit");
        check(mn.contains("read_Object"), "method read_Object");
        check(mn.contains("read_Principal"), "method read_Principal");
        check(mn.contains("read_TypeCode"), "method read_TypeCode");
        check(mn.contains("read_any"), "method read_any");
        check(mn.contains("read_boolean"), "method read_boolean");
        check(mn.contains("read_boolean_array"), "method read_boolean_array");
        check(mn.contains("read_char"), "method read_char");
        check(mn.contains("read_char_array"), "method read_char_array");
        check(mn.contains("read_double"), "method read_double");
        check(mn.contains("read_double_array"), "method read_double_array");
        check(mn.contains("read_float"), "method read_float");
        check(mn.contains("read_float_array"), "method read_float_array");
        check(mn.contains("read_long"), "method read_long");
        check(mn.contains("read_long_array"), "method read_long_array");
        check(mn.contains("read_longlong"), "method read_longlong");
        check(mn.contains("read_longlong_array"), "method read_longlong_array");
        check(mn.contains("read_octet"), "method read_octet");
        check(mn.contains("read_octet_array"), "method read_octet_array");
        check(mn.contains("read_short"), "method read_short");
        check(mn.contains("read_short_array"), "method read_short_array");
        check(mn.contains("read_string"), "method read_string");
        check(mn.contains("read_ulong"), "method read_ulong");
        check(mn.contains("read_ulong_array"), "method read_ulong_array");
        check(mn.contains("read_ulonglong"), "method read_ulonglong");
        check(mn.contains("read_ulonglong_array"), "method read_ulonglong_array");
        check(mn.contains("read_ushort"), "method read_ushort");
        check(mn.contains("read_ushort_array"), "method read_ushort_array");
        check(mn.contains("read_value"), "method read_value");
        check(mn.contains("read_wchar"), "method read_wchar");
        check(mn.contains("read_wchar_array"), "method read_wchar_array");
        check(mn.contains("read_wstring"), "method read_wstring");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("resetCodeSetConverters"), "method resetCodeSetConverters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
