/*
 * @test
 * @summary com.sun.xml.internal.ws.api.server.SDDocument.WSDL conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLTest
 */

import java.lang.reflect.Modifier;


public class WSDLTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.server.SDDocument$WSDL", false, WSDLTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.server.SDDocument$WSDL"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.server.SDDocument"), "implements com.sun.xml.internal.ws.api.server.SDDocument");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAllServices"), "method getAllServices");
        check(mn.contains("getTargetNamespace"), "method getTargetNamespace");
        check(mn.contains("hasPortType"), "method hasPortType");
        check(mn.contains("hasService"), "method hasService");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
