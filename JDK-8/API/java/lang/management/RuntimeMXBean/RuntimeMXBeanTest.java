/*
 * @test
 * @summary java.lang.management.RuntimeMXBean conformance: JVM/name/start/uptime.
 * @run main RuntimeMXBeanTest
 */

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class RuntimeMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRuntimeMXBean() {
        RuntimeMXBean rmx = ManagementFactory.getRuntimeMXBean();
        check(rmx.getName() != null, "getName");
        check(rmx.getVmName() != null, "getVmName");
        check(rmx.getVmVersion() != null, "getVmVersion");
        check(rmx.getVmVendor() != null, "getVmVendor");
        check(rmx.getSpecName() != null, "getSpecName");
        check(rmx.getSpecVersion() != null, "getSpecVersion");
        check(rmx.getStartTime() > 0, "getStartTime");
        check(rmx.getUptime() >= 0, "getUptime");
        check(rmx.getInputArguments() != null, "getInputArguments");
        check(rmx.getClassPath() != null, "getClassPath");
        check(rmx.getSystemProperties() != null, "getSystemProperties");
        check(rmx.getLibraryPath() != null, "getLibraryPath");
        check(rmx.getObjectName() != null, "getObjectName");
        check(rmx.getManagementSpecVersion() != null, "getManagementSpecVersion");
    }

    public static void main(String[] args) {
        testRuntimeMXBean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
