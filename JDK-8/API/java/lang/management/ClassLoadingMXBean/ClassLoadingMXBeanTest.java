/*
 * @test
 * @summary java.lang.management.ClassLoadingMXBean conformance: loaded class counts.
 * @run main ClassLoadingMXBeanTest
 */

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

public class ClassLoadingMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClassLoadingMXBean() {
        ClassLoadingMXBean cmx = ManagementFactory.getClassLoadingMXBean();
        check(cmx.getLoadedClassCount() > 0, "getLoadedClassCount");
        check(cmx.getTotalLoadedClassCount() > 0, "getTotalLoadedClassCount");
        check(cmx.getUnloadedClassCount() >= 0, "getUnloadedClassCount");
        check(cmx.getObjectName() != null, "getObjectName");
        check(cmx.isVerbose() == cmx.isVerbose(), "isVerbose callable");
    }

    public static void main(String[] args) {
        testClassLoadingMXBean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
