/*
 * @test
 * @summary javax.management.remote.JMXConnectorServerFactory conformance: white-box structure + black-box behavior.
 * @run main JMXConnectorServerFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

public class JMXConnectorServerFactoryTest {
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
        Class<?> c = Class.forName("javax.management.remote.JMXConnectorServerFactory");
        check(!c.isInterface(), "concrete class");
        check(c.getConstructors().length == 0, "no public ctor");
        Method[] mm = c.getMethods();
        check(has(mm, "newJMXConnectorServer"), "newJMXConnectorServer");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        check(JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES.equals("jmx.remote.protocol.provider.pkgs"), "PROTOCOL_PROVIDER_PACKAGES");
        check(JMXConnectorServerFactory.DEFAULT_CLASS_LOADER.equals("jmx.remote.default.class.loader"), "DEFAULT_CLASS_LOADER");
        check(JMXConnectorServerFactory.DEFAULT_CLASS_LOADER_NAME.equals("jmx.remote.default.class.loader.name"), "DEFAULT_CLASS_LOADER_NAME");
        check(JMXConnectorServerFactory.PROTOCOL_PROVIDER_CLASS_LOADER.equals("jmx.remote.protocol.provider.class.loader"), "PROTOCOL_PROVIDER_CLASS_LOADER");

        // newJMXConnectorServer creates an unstarted server without exporting RMI.
        JMXServiceURL rmiUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(rmiUrl, null, mbs);
        check(cs != null, "server non-null");
        check(cs instanceof RMIConnectorServer, "rmi protocol yields RMIConnectorServer");
        check(!cs.isActive(), "not active");
        check(cs.getMBeanServer() == mbs, "mbean server preserved");
        check(cs.getAddress() == null, "inactive server has null address");
        check(cs.getConnectionIds().length == 0, "no connections yet");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
