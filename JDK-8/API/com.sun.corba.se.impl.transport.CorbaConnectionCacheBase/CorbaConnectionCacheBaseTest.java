/*
 * @test
 * @summary com.sun.corba.se.impl.transport.CorbaConnectionCacheBase conformance: type shape via reflection (headless-safe).
 * @run main CorbaConnectionCacheBaseTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CorbaConnectionCacheBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.transport.CorbaConnectionCacheBase", false, CorbaConnectionCacheBaseTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.pept.transport.ConnectionCache"), "interface com.sun.corba.se.pept.transport.ConnectionCache");
        check(in.contains("com.sun.corba.se.spi.transport.CorbaConnectionCache"), "interface com.sun.corba.se.spi.transport.CorbaConnectionCache");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 9, "public declared method count");
        check(mn.contains("close"), "method close");
        check(mn.contains("getCacheType"), "method getCacheType");
        check(mn.contains("getMonitoringName"), "method getMonitoringName");
        check(mn.contains("numberOfBusyConnections"), "method numberOfBusyConnections");
        check(mn.contains("numberOfConnections"), "method numberOfConnections");
        check(mn.contains("numberOfIdleConnections"), "method numberOfIdleConnections");
        check(mn.contains("reclaim"), "method reclaim");
        check(mn.contains("stampTime"), "method stampTime");
        check(mn.contains("values"), "method values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
