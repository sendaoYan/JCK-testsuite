/*
 * @test
 * @summary com.sun.corba.se.impl.encoding.IDLJavaSerializationInputStream conformance: type shape via reflection (headless-safe).
 * @run main IDLJavaSerializationInputStreamTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IDLJavaSerializationInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.encoding.IDLJavaSerializationInputStream", false, IDLJavaSerializationInputStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.encoding.CDRInputStreamBase"), "superclass com.sun.corba.se.impl.encoding.CDRInputStreamBase");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 61, "public declared method count");
        check(mn.contains("_truncatable_ids"), "method _truncatable_ids");
        check(mn.contains("consumeEndian"), "method consumeEndian");
        check(mn.contains("dup"), "method dup");
        check(mn.contains("end_value"), "method end_value");
        check(mn.contains("getBufferLength"), "method getBufferLength");
        check(mn.contains("getBufferManager"), "method getBufferManager");
        check(mn.contains("getByteBuffer"), "method getByteBuffer");
        check(mn.contains("getGIOPVersion"), "method getGIOPVersion");
        check(mn.contains("getIndex"), "method getIndex");
        check(mn.contains("getPosition"), "method getPosition");
        check(mn.contains("init"), "method init");
        check(mn.contains("isLittleEndian"), "method isLittleEndian");
        check(mn.contains("mark"), "method mark");
        check(mn.contains("markSupported"), "method markSupported");
        check(mn.contains("orb"), "method orb");
        check(mn.contains("read"), "method read");
        check(mn.contains("read_Abstract"), "method read_Abstract");
        check(mn.contains("read_Context"), "method read_Context");
        check(mn.contains("read_Object"), "method read_Object");
        check(mn.contains("read_Principal"), "method read_Principal");
        check(mn.contains("read_TypeCode"), "method read_TypeCode");
        check(mn.contains("read_Value"), "method read_Value");
        check(mn.contains("read_abstract_interface"), "method read_abstract_interface");
        check(mn.contains("read_any"), "method read_any");
        check(mn.contains("read_any_array"), "method read_any_array");
        check(mn.contains("read_boolean"), "method read_boolean");
        check(mn.contains("read_boolean_array"), "method read_boolean_array");
        check(mn.contains("read_char"), "method read_char");
        check(mn.contains("read_char_array"), "method read_char_array");
        check(mn.contains("read_double"), "method read_double");
        check(mn.contains("read_double_array"), "method read_double_array");
        check(mn.contains("read_fixed"), "method read_fixed");
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
        check(mn.contains("setBufferLength"), "method setBufferLength");
        check(mn.contains("setByteBuffer"), "method setByteBuffer");
        check(mn.contains("setByteBufferWithInfo"), "method setByteBufferWithInfo");
        check(mn.contains("setIndex"), "method setIndex");
        check(mn.contains("setParent"), "method setParent");
        check(mn.contains("start_value"), "method start_value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
