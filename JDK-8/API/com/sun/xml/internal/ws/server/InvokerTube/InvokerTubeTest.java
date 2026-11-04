/*
 * @test
 * @summary com.sun.xml.internal.ws.server.InvokerTube conformance: reflection (kind, superclass, interfaces, methods).
 * @run main InvokerTubeTest
 */

import java.lang.reflect.Modifier;


public class InvokerTubeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.server.InvokerTube", false, InvokerTubeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.server.InvokerTube"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.server.sei.InvokerTube"), "superclass com.sun.xml.internal.ws.server.sei.InvokerTube");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.server.EndpointAwareTube"), "implements com.sun.xml.internal.ws.server.EndpointAwareTube");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("copy"), "method copy");
        check(mn.contains("getCurrentPacket"), "method getCurrentPacket");
        check(mn.contains("getEndpoint"), "method getEndpoint");
        check(mn.contains("getInvoker"), "method getInvoker");
        check(mn.contains("preDestroy"), "method preDestroy");
        check(mn.contains("setEndpoint"), "method setEndpoint");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
