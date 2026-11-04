/*
 * @test
 * @summary com.sun.xml.internal.ws.wsdl.writer.document.soap.Header conformance: reflection (kind, superclass, interfaces, methods).
 * @run main HeaderTest
 */

import java.lang.reflect.Modifier;


public class HeaderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.wsdl.writer.document.soap.Header", false, HeaderTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.wsdl.writer.document.soap.Header"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.txw2.TypedXmlWriter"), "implements com.sun.xml.internal.txw2.TypedXmlWriter");
        check(has(ifs, "com.sun.xml.internal.ws.wsdl.writer.document.soap.BodyType"), "implements com.sun.xml.internal.ws.wsdl.writer.document.soap.BodyType");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("headerFault"), "method headerFault");
        check(mn.contains("message"), "method message");
        check(mn.contains("part"), "method part");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
