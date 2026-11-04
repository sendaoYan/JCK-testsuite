/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnectorServer conformance: white-box structure + black-box behavior.
 * @run main RMIConnectorServerTest
 */

import java.lang.reflect.Method;
import java.lang.management.ManagementFactory;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

public class RMIConnectorServerTest {
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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnectorServer");
        check(JMXConnectorServer.class.isAssignableFrom(c), "extends JMXConnectorServer");
        Method[] mm = c.getMethods();
        check(has(mm, "start"), "start");
        check(has(mm, "stop"), "stop");
        check(has(mm, "isActive"), "isActive");
        check(has(mm, "getAddress"), "getAddress");
        check(has(mm, "getAttributes"), "getAttributes");
        check(has(mm, "toJMXConnector"), "toJMXConnector");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        check(RMIConnectorServer.JNDI_REBIND_ATTRIBUTE.equals("jmx.remote.jndi.rebind"), "JNDI_REBIND_ATTRIBUTE");
        check(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE.equals("jmx.remote.rmi.client.socket.factory"), "RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE");
        check(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE.equals("jmx.remote.rmi.server.socket.factory"), "RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE");
        check(RMIConnectorServer.CREDENTIALS_FILTER_PATTERN.equals("jmx.remote.rmi.server.credentials.filter.pattern"), "CREDENTIALS_FILTER_PATTERN");
        check(RMIConnectorServer.SERIAL_FILTER_PATTERN.equals("jmx.remote.rmi.server.serial.filter.pattern"), "SERIAL_FILTER_PATTERN");

        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        RMIConnectorServer server = new RMIConnectorServer(url, null, mbs);

        check(server.getMBeanServer() == mbs, "mbean server preserved");
        check(!server.isActive(), "not active");
        check(server.getAddress() == null, "inactive server address is null");
        check(server.getConnectionIds().length == 0, "no connections");
        Map<String, ?> attrs = server.getAttributes();
        check(attrs != null, "attributes non-null");
        check(attrs.isEmpty(), "empty attributes with null env");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
