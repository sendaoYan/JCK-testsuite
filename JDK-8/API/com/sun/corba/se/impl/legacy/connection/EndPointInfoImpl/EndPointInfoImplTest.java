/*
 * @test
 * @summary com.sun.corba.se.impl.legacy.connection.EndPointInfoImpl conformance: type shape via reflection (headless-safe).
 * @run main EndPointInfoImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class EndPointInfoImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.legacy.connection.EndPointInfoImpl", false, EndPointInfoImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo"), "interface com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo");
        check(in.contains("com.sun.corba.se.spi.transport.SocketInfo"), "interface com.sun.corba.se.spi.transport.SocketInfo");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 10, "public declared method count");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getHost"), "method getHost");
        check(mn.contains("getHostName"), "method getHostName");
        check(mn.contains("getLocatorPort"), "method getLocatorPort");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("setLocatorPort"), "method setLocatorPort");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
