/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.xml.XMLMessage conformance: reflection (kind, superclass, interfaces, methods).
 * @run main XMLMessageTest
 */

import java.lang.reflect.Modifier;


public class XMLMessageTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.xml.XMLMessage", false, XMLMessageTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.xml.XMLMessage"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("create"), "method create");
        check(mn.contains("createDataSource"), "method createDataSource");
        check(mn.contains("getDataSource"), "method getDataSource");
        check(mn.contains("identifyContentType"), "method identifyContentType");
        check(mn.contains("isFastInfoset"), "method isFastInfoset");
        check(mn.contains("isFastInfosetType"), "method isFastInfosetType");
        check(mn.contains("isXMLType"), "method isXMLType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
