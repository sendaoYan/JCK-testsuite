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


    static void testDeepMatchesSystemProperties() {
        OperatingSystemMXBean b = ManagementFactory.getOperatingSystemMXBean();
        check(b.getName().equals(System.getProperty("os.name")), "getName == os.name");
        check(b.getArch().equals(System.getProperty("os.arch")), "getArch == os.arch");
        check(b.getVersion().equals(System.getProperty("os.version")), "getVersion == os.version");
    }

    static void testDeepProcessorsAndLoad() {
        OperatingSystemMXBean b = ManagementFactory.getOperatingSystemMXBean();
        check(b.getAvailableProcessors() >= 1, "available processors >= 1");
        check(b.getAvailableProcessors() == Runtime.getRuntime().availableProcessors(),
              "processors equal Runtime.availableProcessors()");
        double l1 = b.getSystemLoadAverage();
        double l2 = b.getSystemLoadAverage();
        check((l1 >= 0.0 || l1 == -1.0) && !Double.isNaN(l1), "load average >= 0 or -1, never NaN");
        check((l2 >= 0.0 || l2 == -1.0) && !Double.isNaN(l2), "load average domain stable across calls");
    }

    static void testDeepObjectNameExact() {
        OperatingSystemMXBean b = ManagementFactory.getOperatingSystemMXBean();
        check("java.lang:type=OperatingSystem".equals(b.getObjectName().getCanonicalName()),
              "OperatingSystemMXBean ObjectName exact, got " + b.getObjectName());
    }

    public static void main(String[] args) throws Exception {
        testOperatingSystemMXBean();
        testDeepMatchesSystemProperties();
        testDeepProcessorsAndLoad();
        testDeepObjectNameExact();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
