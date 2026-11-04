/*
 * @test
 * @summary java.lang.management.OperatingSystemMXBean conformance: name/arch/processors.
 * @run main OperatingSystemMXBeanTest
 */

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class OperatingSystemMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOperatingSystemMXBean() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        check(os.getName() != null, "getName");
        check(os.getArch() != null, "getArch");
        check(os.getVersion() != null, "getVersion");
        check(os.getAvailableProcessors() > 0, "getAvailableProcessors");
        check(os.getObjectName() != null, "getObjectName");
        double load = os.getSystemLoadAverage();
        check(load >= -1.0, "getSystemLoadAverage >= -1");
    }

    public static void main(String[] args) {
        testOperatingSystemMXBean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
