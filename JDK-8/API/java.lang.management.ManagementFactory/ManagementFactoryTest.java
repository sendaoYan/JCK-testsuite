/*
 * @test
 * @summary java.lang.management.ManagementFactory conformance: MXBean accessors.
 * @run main ManagementFactoryTest
 */

import java.lang.management.ManagementFactory;

public class ManagementFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testManagementFactory() {
        check(ManagementFactory.getMemoryMXBean() != null, "getMemoryMXBean");
        check(ManagementFactory.getThreadMXBean() != null, "getThreadMXBean");
        check(ManagementFactory.getRuntimeMXBean() != null, "getRuntimeMXBean");
        check(ManagementFactory.getClassLoadingMXBean() != null, "getClassLoadingMXBean");
        check(ManagementFactory.getOperatingSystemMXBean() != null, "getOperatingSystemMXBean");
        check(ManagementFactory.getMemoryPoolMXBeans() != null, "getMemoryPoolMXBeans");
        check(ManagementFactory.getMemoryManagerMXBeans() != null, "getMemoryManagerMXBeans");
        check(ManagementFactory.getGarbageCollectorMXBeans() != null, "getGarbageCollectorMXBeans");
        check(ManagementFactory.getMemoryPoolMXBeans().size() > 0, "has memory pools");
        check(ManagementFactory.getGarbageCollectorMXBeans().size() > 0, "has GC beans");
        check(ManagementFactory.getRuntimeMXBean().getName() != null, "runtime name");
        check(ManagementFactory.getPlatformMBeanServer() != null, "getPlatformMBeanServer");
        check(ManagementFactory.getPlatformManagementInterfaces() != null,
              "getPlatformManagementInterfaces");
    }

    public static void main(String[] args) {
        testManagementFactory();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
