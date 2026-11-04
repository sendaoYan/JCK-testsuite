/*
 * @test
 * @summary com.sun.corba.se.impl.transport.SocketOrChannelContactInfoImpl conformance: type shape via reflection (headless-safe).
 * @run main SocketOrChannelContactInfoImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SocketOrChannelContactInfoImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.transport.SocketOrChannelContactInfoImpl", false, SocketOrChannelContactInfoImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.transport.CorbaContactInfoBase"), "superclass com.sun.corba.se.impl.transport.CorbaContactInfoBase");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.transport.SocketInfo"), "interface com.sun.corba.se.spi.transport.SocketInfo");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 11, "public declared method count");
        check(mn.contains("createConnection"), "method createConnection");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getConnectionCacheType"), "method getConnectionCacheType");
        check(mn.contains("getHost"), "method getHost");
        check(mn.contains("getMonitoringName"), "method getMonitoringName");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("isConnectionBased"), "method isConnectionBased");
        check(mn.contains("shouldCacheConnection"), "method shouldCacheConnection");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
