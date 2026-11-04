/*
 * @test
 * @summary com.sun.xml.internal.ws.db.DatabindingProviderImpl.JaxwsWsdlGen conformance: reflection (kind, superclass, interfaces, methods).
 * @run main JaxwsWsdlGenTest
 */

import java.lang.reflect.Modifier;


public class JaxwsWsdlGenTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.db.DatabindingProviderImpl$JaxwsWsdlGen", false, JaxwsWsdlGenTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.db.DatabindingProviderImpl$JaxwsWsdlGen"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.oracle.webservices.internal.api.databinding.WSDLGenerator"), "implements com.oracle.webservices.internal.api.databinding.WSDLGenerator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("generate"), "method generate");
        check(mn.contains("inlineSchema"), "method inlineSchema");
        check(mn.contains("property"), "method property");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
