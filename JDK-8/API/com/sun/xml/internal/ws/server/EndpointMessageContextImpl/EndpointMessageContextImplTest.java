/*
 * @test
 * @summary com.sun.xml.internal.ws.server.EndpointMessageContextImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main EndpointMessageContextImplTest
 */

import java.lang.reflect.Modifier;


public class EndpointMessageContextImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.server.EndpointMessageContextImpl", false, EndpointMessageContextImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.server.EndpointMessageContextImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.util.AbstractMap"), "superclass java.util.AbstractMap");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.handler.MessageContext"), "implements javax.xml.ws.handler.MessageContext");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("entrySet"), "method entrySet");
        check(mn.contains("get"), "method get");
        check(mn.contains("getScope"), "method getScope");
        check(mn.contains("put"), "method put");
        check(mn.contains("remove"), "method remove");
        check(mn.contains("setScope"), "method setScope");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
