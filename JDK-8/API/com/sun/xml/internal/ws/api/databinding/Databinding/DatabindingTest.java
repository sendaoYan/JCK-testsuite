/*
 * @test
 * @summary com.sun.xml.internal.ws.api.databinding.Databinding conformance: reflection (kind, superclass, interfaces, methods).
 * @run main DatabindingTest
 */

import java.lang.reflect.Modifier;


public class DatabindingTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.databinding.Databinding", false, DatabindingTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.databinding.Databinding"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.oracle.webservices.internal.api.databinding.Databinding"), "implements com.oracle.webservices.internal.api.databinding.Databinding");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("decode"), "method decode");
        check(mn.contains("encode"), "method encode");
        check(mn.contains("generateWSDL"), "method generateWSDL");
        check(mn.contains("getClientBridge"), "method getClientBridge");
        check(mn.contains("getEndpointBridge"), "method getEndpointBridge");
        check(mn.contains("getMessageContextFactory"), "method getMessageContextFactory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
