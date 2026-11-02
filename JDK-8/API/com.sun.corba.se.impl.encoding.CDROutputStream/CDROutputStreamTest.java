/*
 * @test
 * @summary com.sun.corba.se.impl.encoding.CDROutputStream conformance: type shape via reflection (headless-safe).
 * @run main CDROutputStreamTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CDROutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.encoding.CDROutputStream", false, CDROutputStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA_2_3.portable.OutputStream"), "superclass org.omg.CORBA_2_3.portable.OutputStream");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.encoding.MarshalOutputStream"), "interface com.sun.corba.se.impl.encoding.MarshalOutputStream");
        check(in.contains("org.omg.CORBA.DataOutputStream"), "interface org.omg.CORBA.DataOutputStream");
        check(in.contains("org.omg.CORBA.portable.ValueOutputStream"), "interface org.omg.CORBA.portable.ValueOutputStream");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 60, "public declared method count");
        check(mn.contains("_truncatable_ids"), "method _truncatable_ids");
        check(mn.contains("alignOnBoundary"), "method alignOnBoundary");
        check(mn.contains("close"), "method close");
        check(mn.contains("create_input_stream"), "method create_input_stream");
        check(mn.contains("end_block"), "method end_block");
        check(mn.contains("end_value"), "method end_value");
        check(mn.contains("flush"), "method flush");
        check(mn.contains("getBufferManager"), "method getBufferManager");
        check(mn.contains("getByteBufferWithInfo"), "method getByteBufferWithInfo");
        check(mn.contains("getGIOPVersion"), "method getGIOPVersion");
        check(mn.contains("getMessageMediator"), "method getMessageMediator");
        check(mn.contains("isLittleEndian"), "method isLittleEndian");
        check(mn.contains("orb"), "method orb");
        check(mn.contains("putEndian"), "method putEndian");
        check(mn.contains("setHeaderPadding"), "method setHeaderPadding");
        check(mn.contains("setMessageMediator"), "method setMessageMediator");
        check(mn.contains("start_block"), "method start_block");
        check(mn.contains("start_value"), "method start_value");
        check(mn.contains("toByteArray"), "method toByteArray");
        check(mn.contains("write"), "method write");
        check(mn.contains("writeIndirection"), "method writeIndirection");
        check(mn.contains("writeOctetSequenceTo"), "method writeOctetSequenceTo");
        check(mn.contains("writeTo"), "method writeTo");
        check(mn.contains("write_Abstract"), "method write_Abstract");
        check(mn.contains("write_Context"), "method write_Context");
        check(mn.contains("write_Object"), "method write_Object");
        check(mn.contains("write_Principal"), "method write_Principal");
        check(mn.contains("write_TypeCode"), "method write_TypeCode");
        check(mn.contains("write_Value"), "method write_Value");
        check(mn.contains("write_abstract_interface"), "method write_abstract_interface");
        check(mn.contains("write_any"), "method write_any");
        check(mn.contains("write_any_array"), "method write_any_array");
        check(mn.contains("write_boolean"), "method write_boolean");
        check(mn.contains("write_boolean_array"), "method write_boolean_array");
        check(mn.contains("write_char"), "method write_char");
        check(mn.contains("write_char_array"), "method write_char_array");
        check(mn.contains("write_double"), "method write_double");
        check(mn.contains("write_double_array"), "method write_double_array");
        check(mn.contains("write_fixed"), "method write_fixed");
        check(mn.contains("write_float"), "method write_float");
        check(mn.contains("write_float_array"), "method write_float_array");
        check(mn.contains("write_long"), "method write_long");
        check(mn.contains("write_long_array"), "method write_long_array");
        check(mn.contains("write_longlong"), "method write_longlong");
        check(mn.contains("write_longlong_array"), "method write_longlong_array");
        check(mn.contains("write_octet"), "method write_octet");
        check(mn.contains("write_octet_array"), "method write_octet_array");
        check(mn.contains("write_short"), "method write_short");
        check(mn.contains("write_short_array"), "method write_short_array");
        check(mn.contains("write_string"), "method write_string");
        check(mn.contains("write_ulong"), "method write_ulong");
        check(mn.contains("write_ulong_array"), "method write_ulong_array");
        check(mn.contains("write_ulonglong"), "method write_ulonglong");
        check(mn.contains("write_ulonglong_array"), "method write_ulonglong_array");
        check(mn.contains("write_ushort"), "method write_ushort");
        check(mn.contains("write_ushort_array"), "method write_ushort_array");
        check(mn.contains("write_value"), "method write_value");
        check(mn.contains("write_wchar"), "method write_wchar");
        check(mn.contains("write_wchar_array"), "method write_wchar_array");
        check(mn.contains("write_wstring"), "method write_wstring");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
