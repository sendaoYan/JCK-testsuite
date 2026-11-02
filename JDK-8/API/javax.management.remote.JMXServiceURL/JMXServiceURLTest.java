/*
 * @test
 * @summary javax.management.remote.JMXServiceURL conformance: protocol/host/port.
 * @run main JMXServiceURLTest
 */

import javax.management.remote.*;
public class JMXServiceURLTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        JMXServiceURL u = new JMXServiceURL("rmi", "localhost", 9999);
        check("rmi".equals(u.getProtocol()), "protocol");
        check("localhost".equals(u.getHost()), "host");
        check(u.getPort() == 9999, "port");
        check(u.toString() != null, "toString");
        check(u.equals(new JMXServiceURL("rmi", "localhost", 9999)), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
