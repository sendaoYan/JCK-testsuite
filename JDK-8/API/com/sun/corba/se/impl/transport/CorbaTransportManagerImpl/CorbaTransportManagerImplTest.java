/*
 * @test
 * @summary com.sun.corba.se.impl.transport.CorbaTransportManagerImpl conformance: type shape via reflection (headless-safe).
 * @run main CorbaTransportManagerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CorbaTransportManagerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.transport.CorbaTransportManagerImpl", false, CorbaTransportManagerImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.transport.CorbaTransportManager"), "interface com.sun.corba.se.spi.transport.CorbaTransportManager");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 11, "public declared method count");
        check(mn.contains("addToIORTemplate"), "method addToIORTemplate");
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
