/*
 * @test
 * @summary com.sun.xml.internal.ws.developer.WSBindingProvider conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSBindingProviderTest
 */

import java.lang.reflect.Modifier;


public class WSBindingProviderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.developer.WSBindingProvider", false, WSBindingProviderTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.developer.WSBindingProvider"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.ComponentRegistry"), "implements com.sun.xml.internal.ws.api.ComponentRegistry");
        check(has(ifs, "java.io.Closeable"), "implements java.io.Closeable");
        check(has(ifs, "javax.xml.ws.BindingProvider"), "implements javax.xml.ws.BindingProvider");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getInboundHeaders"), "method getInboundHeaders");
        check(mn.contains("getManagedObjectManager"), "method getManagedObjectManager");
        check(mn.contains("getPortInfo"), "method getPortInfo");
        check(mn.contains("getWSEndpointReference"), "method getWSEndpointReference");
        check(mn.contains("setAddress"), "method setAddress");
        check(mn.contains("setOutboundHeaders"), "method setOutboundHeaders");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
