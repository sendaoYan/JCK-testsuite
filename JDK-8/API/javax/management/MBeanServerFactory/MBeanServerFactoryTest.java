/*
 * @test
 * @summary javax.management.MBeanServerFactory conformance: create/find/release.
 * @run main MBeanServerFactoryTest
 */

import javax.management.*;
public class MBeanServerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        MBeanServer s = MBeanServerFactory.createMBeanServer();
        check(s != null, "createMBeanServer");
        check(MBeanServerFactory.findMBeanServer(null).contains(s), "findMBeanServer");
        MBeanServerFactory.releaseMBeanServer(s);
        MBeanServer n = MBeanServerFactory.newMBeanServer();
        check(n != null, "newMBeanServer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
