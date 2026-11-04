/*
 * @test
 * @summary java.rmi.server.RMISocketFactory conformance: white-box structure + black-box behavior.
 * @run main RMISocketFactoryTest
 */

import java.lang.reflect.Modifier;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIFailureHandler;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;

public class RMISocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = RMISocketFactory.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()), "public abstract");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "abstract class");
        check(c.getSuperclass() == Object.class, "extends Object");
        java.util.Set<String> ifaces = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains(RMIClientSocketFactory.class.getName()), "implements RMIClientSocketFactory");
        check(ifaces.contains(RMIServerSocketFactory.class.getName()), "implements RMIServerSocketFactory");
    }

    static void testBlackBox() throws Exception {
        RMISocketFactory def = RMISocketFactory.getDefaultSocketFactory();
        check(def != null, "default factory non-null");
        check(RMISocketFactory.getSocketFactory() == null, "no custom socket factory set");
        check(RMISocketFactory.getFailureHandler() == null, "failure handler default null");

        // minimal subclass: no real sockets, sentinel returns
        RMISocketFactory f = new RMISocketFactory() {
            public java.net.Socket createSocket(String host, int port) { return null; }
            public java.net.ServerSocket createServerSocket(int port) { return null; }
        };
        check(f instanceof RMIClientSocketFactory && f instanceof RMIServerSocketFactory, "subclass implements both");
        check(f.createSocket("h", 1) == null, "createSocket returns sentinel");
        check(f.createServerSocket(0) == null, "createServerSocket returns sentinel");

        // failure handler set/get round trip (restored afterwards)
        RMIFailureHandler h = new RMIFailureHandler() {
            public boolean failure(Exception ex) { return ex != null; }
        };
        RMISocketFactory.setFailureHandler(h);
        check(RMISocketFactory.getFailureHandler() == h, "set/get failure handler");
        check(h.failure(new Exception("boom")), "failure handler lambda behavior");
        RMISocketFactory.setFailureHandler(null);
        check(RMISocketFactory.getFailureHandler() == null, "failure handler restored null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
