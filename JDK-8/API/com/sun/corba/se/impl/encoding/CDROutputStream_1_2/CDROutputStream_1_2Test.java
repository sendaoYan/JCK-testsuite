/*
 * @test
 * @summary com.sun.corba.se.impl.encoding.CDROutputStream_1_2 conformance: type shape via reflection (headless-safe).
 * @run main CDROutputStream_1_2Test
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CDROutputStream_1_2Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.encoding.CDROutputStream_1_2", false, CDROutputStream_1_2Test.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.encoding.CDROutputStream_1_1"), "superclass com.sun.corba.se.impl.encoding.CDROutputStream_1_1");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("getGIOPVersion"), "method getGIOPVersion");
        check(mn.contains("write_long"), "method write_long");
        check(mn.contains("write_longlong"), "method write_longlong");
        check(mn.contains("write_octet"), "method write_octet");
        check(mn.contains("write_short"), "method write_short");
        check(mn.contains("write_wchar"), "method write_wchar");
        check(mn.contains("write_wchar_array"), "method write_wchar_array");
        check(mn.contains("write_wstring"), "method write_wstring");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
