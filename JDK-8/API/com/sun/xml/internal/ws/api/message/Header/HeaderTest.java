/*
 * @test
 * @summary com.sun.xml.internal.ws.api.message.Header conformance: reflection (kind, superclass, interfaces, methods).
 * @run main HeaderTest
 */

import java.lang.reflect.Modifier;


public class HeaderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.message.Header", false, HeaderTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.message.Header"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAttribute"), "method getAttribute");
        check(mn.contains("getLocalPart"), "method getLocalPart");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getRole"), "method getRole");
        check(mn.contains("getStringContent"), "method getStringContent");
        check(mn.contains("isIgnorable"), "method isIgnorable");
        check(mn.contains("isRelay"), "method isRelay");
        check(mn.contains("readAsEPR"), "method readAsEPR");
        check(mn.contains("readAsJAXB"), "method readAsJAXB");
        check(mn.contains("readHeader"), "method readHeader");
        check(mn.contains("writeTo"), "method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
