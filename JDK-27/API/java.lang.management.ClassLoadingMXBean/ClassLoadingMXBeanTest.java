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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
