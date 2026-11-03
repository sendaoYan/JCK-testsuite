/*
 * @test
 * @summary javax.management.remote.JMXConnectorFactory conformance: white-box structure + black-box behavior.
 * @run main JMXConnectorFactoryTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnector;

public class JMXConnectorFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(Method[] mm, String n) {
        for (Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXConnectorFactory");
        check(!c.isInterface(), "concrete class");
        // The constructor is private: no public constructors.
        check(c.getConstructors().length == 0, "no public ctor");
        Method[] mm = c.getMethods();
        check(has(mm, "connect"), "connect");
        check(has(mm, "newJMXConnector"), "newJMXConnector");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        check(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES.equals("jmx.remote.protocol.provider.pkgs"), "PROTOCOL_PROVIDER_PACKAGES");
        check(JMXConnectorFactory.DEFAULT_CLASS_LOADER.equals("jmx.remote.default.class.loader"), "DEFAULT_CLASS_LOADER");
        check(JMXConnectorFactory.PROTOCOL_PROVIDER_CLASS_LOADER.equals("jmx.remote.protocol.provider.class.loader"), "PROTOCOL_PROVIDER_CLASS_LOADER");

        // newJMXConnector just instantiates a connector; it must not open a connection.
        JMXServiceURL rmiUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
        JMXConnector c = JMXConnectorFactory.newJMXConnector(rmiUrl, null);
        check(c != null, "newJMXConnector non-null");
        check(c instanceof RMIConnector, "rmi protocol yields RMIConnector");
        // The connector is not connected: getConnectionId throws IOException("Not connected").
        try {
            c.getConnectionId();
            check(false, "expected IOException for unconnected getConnectionId");
        } catch (java.io.IOException e) {
            check(e.getMessage().equals("Not connected"), "unconnected getConnectionId message: " + e.getMessage());
        }
        c.close();

        // An unknown protocol produces a stable MalformedURLException.
        try {
            JMXServiceURL bad = new JMXServiceURL("service:jmx:noSuchProto://localhost:9999/x");
            JMXConnectorFactory.newJMXConnector(bad, null);
            check(false, "expected MalformedURLException for unknown protocol");
        } catch (java.net.MalformedURLException e) {
            check(e.getMessage().equals("Unsupported protocol: nosuchproto"), "unsupported protocol message: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
