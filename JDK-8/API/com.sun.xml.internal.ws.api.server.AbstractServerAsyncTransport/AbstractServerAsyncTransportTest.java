/*
 * @test
 * @summary com.sun.xml.internal.ws.api.server.AbstractServerAsyncTransport conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AbstractServerAsyncTransportTest
 */

import java.lang.reflect.Modifier;


public class AbstractServerAsyncTransportTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.server.AbstractServerAsyncTransport", false, AbstractServerAsyncTransportTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.server.AbstractServerAsyncTransport"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("decodePacket"), "method decodePacket");
        check(mn.contains("encodePacket"), "method encodePacket");
        check(mn.contains("getAcceptableMimeTypes"), "method getAcceptableMimeTypes");
        check(mn.contains("getPropertySet"), "method getPropertySet");
        check(mn.contains("getTransportBackChannel"), "method getTransportBackChannel");
        check(mn.contains("getWebServiceContextDelegate"), "method getWebServiceContextDelegate");
        check(mn.contains("handle"), "method handle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
