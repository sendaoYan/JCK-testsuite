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


    static void testDeepVerboseRoundtrip() {
        ClassLoadingMXBean b = ManagementFactory.getClassLoadingMXBean();
        boolean original = b.isVerbose();
        b.setVerbose(true);
        check(b.isVerbose(), "setVerbose(true) applied");
        b.setVerbose(false);
        check(!b.isVerbose(), "setVerbose(false) applied");
        b.setVerbose(original);
        check(b.isVerbose() == original, "verbose restored");
    }

    static void testDeepCounterGrowthAndOrdering() {
        ClassLoadingMXBean b = ManagementFactory.getClassLoadingMXBean();
        int loaded = b.getLoadedClassCount();
        long total = b.getTotalLoadedClassCount();
        check(loaded >= 1, "loaded >= 1");
        check(total >= loaded, "total loaded >= currently loaded");
        check(b.getUnloadedClassCount() >= 0, "unloaded >= 0");
        // defining a brand new class must advance the live loaded-class counter
        int before = b.getLoadedClassCount();
        Object fresh1 = new Object() { };
        Object fresh2 = new Object() { };
        int after = b.getLoadedClassCount();
        check(after >= before + 2, "two new anonymous classes bump getLoadedClassCount by >= 2");
        check(b.getTotalLoadedClassCount() >= total + 2, "total also advances");
    }

    static void testDeepObjectNameExact() {
        ClassLoadingMXBean b = ManagementFactory.getClassLoadingMXBean();
        check("java.lang:type=ClassLoading".equals(b.getObjectName().getCanonicalName()),
              "ClassLoadingMXBean ObjectName exact, got " + b.getObjectName());
    }

    public static void main(String[] args) throws Exception {
        testClassLoadingMXBean();
        testDeepVerboseRoundtrip();
        testDeepCounterGrowthAndOrdering();
        testDeepObjectNameExact();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
