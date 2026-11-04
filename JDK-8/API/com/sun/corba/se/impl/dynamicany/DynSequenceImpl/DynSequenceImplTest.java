/*
 * @test
 * @summary com.sun.corba.se.impl.dynamicany.DynSequenceImpl conformance: type shape via reflection (headless-safe).
 * @run main DynSequenceImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DynSequenceImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.dynamicany.DynSequenceImpl", false, DynSequenceImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.dynamicany.DynAnyCollectionImpl"), "superclass com.sun.corba.se.impl.dynamicany.DynAnyCollectionImpl");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.DynamicAny.DynSequence"), "interface org.omg.DynamicAny.DynSequence");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 57, "public declared method count");
        check(mn.contains("_ids"), "method _ids");
        check(mn.contains("assign"), "method assign");
        check(mn.contains("component_count"), "method component_count");
        check(mn.contains("copy"), "method copy");
        check(mn.contains("current_component"), "method current_component");
        check(mn.contains("destroy"), "method destroy");
        check(mn.contains("equal"), "method equal");
        check(mn.contains("from_any"), "method from_any");
        check(mn.contains("get_any"), "method get_any");
        check(mn.contains("get_boolean"), "method get_boolean");
        check(mn.contains("get_char"), "method get_char");
        check(mn.contains("get_double"), "method get_double");
        check(mn.contains("get_dyn_any"), "method get_dyn_any");
        check(mn.contains("get_elements"), "method get_elements");
        check(mn.contains("get_elements_as_dyn_any"), "method get_elements_as_dyn_any");
        check(mn.contains("get_float"), "method get_float");
        check(mn.contains("get_length"), "method get_length");
        check(mn.contains("get_long"), "method get_long");
        check(mn.contains("get_longlong"), "method get_longlong");
        check(mn.contains("get_octet"), "method get_octet");
        check(mn.contains("get_reference"), "method get_reference");
        check(mn.contains("get_short"), "method get_short");
        check(mn.contains("get_string"), "method get_string");
        check(mn.contains("get_typecode"), "method get_typecode");
        check(mn.contains("get_ulong"), "method get_ulong");
        check(mn.contains("get_ulonglong"), "method get_ulonglong");
        check(mn.contains("get_ushort"), "method get_ushort");
        check(mn.contains("get_val"), "method get_val");
        check(mn.contains("get_wchar"), "method get_wchar");
        check(mn.contains("get_wstring"), "method get_wstring");
        check(mn.contains("insert_any"), "method insert_any");
        check(mn.contains("insert_boolean"), "method insert_boolean");
        check(mn.contains("insert_char"), "method insert_char");
        check(mn.contains("insert_double"), "method insert_double");
        check(mn.contains("insert_dyn_any"), "method insert_dyn_any");
        check(mn.contains("insert_float"), "method insert_float");
        check(mn.contains("insert_long"), "method insert_long");
        check(mn.contains("insert_longlong"), "method insert_longlong");
        check(mn.contains("insert_octet"), "method insert_octet");
        check(mn.contains("insert_reference"), "method insert_reference");
        check(mn.contains("insert_short"), "method insert_short");
        check(mn.contains("insert_string"), "method insert_string");
        check(mn.contains("insert_typecode"), "method insert_typecode");
        check(mn.contains("insert_ulong"), "method insert_ulong");
        check(mn.contains("insert_ulonglong"), "method insert_ulonglong");
        check(mn.contains("insert_ushort"), "method insert_ushort");
        check(mn.contains("insert_val"), "method insert_val");
        check(mn.contains("insert_wchar"), "method insert_wchar");
        check(mn.contains("insert_wstring"), "method insert_wstring");
        check(mn.contains("next"), "method next");
        check(mn.contains("rewind"), "method rewind");
        check(mn.contains("seek"), "method seek");
        check(mn.contains("set_elements"), "method set_elements");
        check(mn.contains("set_elements_as_dyn_any"), "method set_elements_as_dyn_any");
        check(mn.contains("set_length"), "method set_length");
        check(mn.contains("to_any"), "method to_any");
        check(mn.contains("type"), "method type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
