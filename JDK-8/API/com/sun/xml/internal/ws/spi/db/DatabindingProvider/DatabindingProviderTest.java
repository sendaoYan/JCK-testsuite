/*
 * @test
 * @summary com.sun.xml.internal.ws.spi.db.DatabindingProvider conformance: reflection (kind, superclass, interfaces, methods).
 * @run main DatabindingProviderTest
 */

import java.lang.reflect.Modifier;


public class DatabindingProviderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.spi.db.DatabindingProvider", false, DatabindingProviderTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.spi.db.DatabindingProvider"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("create"), "method create");
        check(mn.contains("init"), "method init");
        check(mn.contains("isFor"), "method isFor");
        check(mn.contains("wsdlGen"), "method wsdlGen");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
