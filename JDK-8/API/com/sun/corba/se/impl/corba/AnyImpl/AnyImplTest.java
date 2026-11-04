/*
 * @test
 * @summary com.sun.corba.se.impl.corba.AnyImpl conformance: type shape via reflection (headless-safe).
 * @run main AnyImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AnyImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.corba.AnyImpl", false, AnyImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.Any"), "superclass org.omg.CORBA.Any");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 52, "public declared method count");
        check(mn.contains("createTypeCodeForClass"), "method createTypeCodeForClass");
        check(mn.contains("create_input_stream"), "method create_input_stream");
        check(mn.contains("create_output_stream"), "method create_output_stream");
        check(mn.contains("equal"), "method equal");
        check(mn.contains("extractAny"), "method extractAny");
        check(mn.contains("extractAnyFromStream"), "method extractAnyFromStream");
        check(mn.contains("extract_Object"), "method extract_Object");
        check(mn.contains("extract_Principal"), "method extract_Principal");
        check(mn.contains("extract_Streamable"), "method extract_Streamable");
        check(mn.contains("extract_TypeCode"), "method extract_TypeCode");
        check(mn.contains("extract_Value"), "method extract_Value");
        check(mn.contains("extract_any"), "method extract_any");
        check(mn.contains("extract_boolean"), "method extract_boolean");
        check(mn.contains("extract_char"), "method extract_char");
        check(mn.contains("extract_double"), "method extract_double");
        check(mn.contains("extract_fixed"), "method extract_fixed");
        check(mn.contains("extract_float"), "method extract_float");
        check(mn.contains("extract_long"), "method extract_long");
        check(mn.contains("extract_longlong"), "method extract_longlong");
        check(mn.contains("extract_octet"), "method extract_octet");
        check(mn.contains("extract_short"), "method extract_short");
        check(mn.contains("extract_string"), "method extract_string");
        check(mn.contains("extract_ulong"), "method extract_ulong");
        check(mn.contains("extract_ulonglong"), "method extract_ulonglong");
        check(mn.contains("extract_ushort"), "method extract_ushort");
        check(mn.contains("extract_wchar"), "method extract_wchar");
        check(mn.contains("extract_wstring"), "method extract_wstring");
        check(mn.contains("insert_Object"), "method insert_Object");
        check(mn.contains("insert_Principal"), "method insert_Principal");
        check(mn.contains("insert_Streamable"), "method insert_Streamable");
        check(mn.contains("insert_TypeCode"), "method insert_TypeCode");
        check(mn.contains("insert_Value"), "method insert_Value");
        check(mn.contains("insert_any"), "method insert_any");
        check(mn.contains("insert_boolean"), "method insert_boolean");
        check(mn.contains("insert_char"), "method insert_char");
        check(mn.contains("insert_double"), "method insert_double");
        check(mn.contains("insert_fixed"), "method insert_fixed");
        check(mn.contains("insert_float"), "method insert_float");
        check(mn.contains("insert_long"), "method insert_long");
        check(mn.contains("insert_longlong"), "method insert_longlong");
        check(mn.contains("insert_octet"), "method insert_octet");
        check(mn.contains("insert_short"), "method insert_short");
        check(mn.contains("insert_string"), "method insert_string");
        check(mn.contains("insert_ulong"), "method insert_ulong");
        check(mn.contains("insert_ulonglong"), "method insert_ulonglong");
        check(mn.contains("insert_ushort"), "method insert_ushort");
        check(mn.contains("insert_wchar"), "method insert_wchar");
        check(mn.contains("insert_wstring"), "method insert_wstring");
        check(mn.contains("isInitialized"), "method isInitialized");
        check(mn.contains("read_value"), "method read_value");
        check(mn.contains("type"), "method type");
        check(mn.contains("write_value"), "method write_value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
