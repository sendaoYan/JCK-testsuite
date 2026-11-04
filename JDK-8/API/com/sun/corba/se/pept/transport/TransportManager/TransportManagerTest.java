/*
 * @test
 * @summary com.sun.corba.se.pept.transport.TransportManager conformance: type shape via reflection (headless-safe).
 * @run main TransportManagerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TransportManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.pept.transport.TransportManager", false, TransportManagerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 10, "public declared method count");
        check(mn.contains("close"), "method close");
        check(mn.contains("getAcceptors"), "method getAcceptors");
        check(mn.contains("getByteBufferPool"), "method getByteBufferPool");
        check(mn.contains("getInboundConnectionCache"), "method getInboundConnectionCache");
        check(mn.contains("getInboundConnectionCaches"), "method getInboundConnectionCaches");
        check(mn.contains("getOutboundConnectionCache"), "method getOutboundConnectionCache");
        check(mn.contains("getOutboundConnectionCaches"), "method getOutboundConnectionCaches");
        check(mn.contains("getSelector"), "method getSelector");
        check(mn.contains("registerAcceptor"), "method registerAcceptor");
        check(mn.contains("unregisterAcceptor"), "method unregisterAcceptor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
