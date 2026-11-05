/*
 * @test
 * @summary java.lang.management.ClassLoadingMXBean conformance: white-box structure + black-box behavior.
 * @run main ClassLoadingMXBeanTest
 */
import java.lang.management.*;

public class ClassLoadingMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.ClassLoadingMXBean");
        check(c.isInterface(), "interface");
        check(PlatformManagedObject.class.isAssignableFrom(c), "extends PlatformManagedObject");
        check(c.getMethod("getTotalLoadedClassCount").getReturnType() == long.class, "getTotalLoadedClassCount returns long");
        check(c.getMethod("getLoadedClassCount").getReturnType() == int.class, "getLoadedClassCount returns int");
        check(c.getMethod("getUnloadedClassCount").getReturnType() == long.class, "getUnloadedClassCount returns long");
        check(c.getMethod("isVerbose").getReturnType() == boolean.class, "isVerbose returns boolean");
        check(c.getMethod("setVerbose", boolean.class).getReturnType() == void.class, "setVerbose returns void");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ClassLoadingMXBean b = ManagementFactory.getClassLoadingMXBean();
        check(b != null, "non-null");
        long total = b.getTotalLoadedClassCount();
        int loaded = b.getLoadedClassCount();
        check(total >= 1, "total loaded classes >= 1");
        check(loaded >= 1, "loaded classes >= 1");
        check(b.getUnloadedClassCount() >= 0, "unloaded >= 0");
        check(b.getObjectName() != null, "objectName non-null");

        boolean original = b.isVerbose();
        b.setVerbose(false);
        check(!b.isVerbose(), "setVerbose(false) applied");
        b.setVerbose(original);
        check(b.isVerbose() == original, "verbose restored");
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
        testWhiteBox();
        testBlackBox();
        testDeepVerboseRoundtrip();
        testDeepCounterGrowthAndOrdering();
        testDeepObjectNameExact();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
