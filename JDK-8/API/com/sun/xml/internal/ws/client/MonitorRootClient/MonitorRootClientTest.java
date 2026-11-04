/*
 * @test
 * @summary com.sun.xml.internal.ws.client.MonitorRootClient conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MonitorRootClientTest
 */

import java.lang.reflect.Modifier;


public class MonitorRootClientTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.client.MonitorRootClient", false, MonitorRootClientTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.client.MonitorRootClient"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.server.MonitorBase"), "superclass com.sun.xml.internal.ws.server.MonitorBase");
        check(c.getInterfaces().length == 0, "no interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
