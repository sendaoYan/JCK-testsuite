/*
 * @test
 * @summary java.lang.management.OperatingSystemMXBean conformance: white-box structure + black-box behavior.
 * @run main OperatingSystemMXBeanTest
 */
import java.lang.management.*;

public class OperatingSystemMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.OperatingSystemMXBean");
        check(c.isInterface(), "interface");
        check(PlatformManagedObject.class.isAssignableFrom(c), "extends PlatformManagedObject");
        check(c.getMethod("getName").getReturnType() == String.class, "getName returns String");
        check(c.getMethod("getArch").getReturnType() == String.class, "getArch returns String");
        check(c.getMethod("getVersion").getReturnType() == String.class, "getVersion returns String");
        check(c.getMethod("getAvailableProcessors").getReturnType() == int.class, "getAvailableProcessors returns int");
        check(c.getMethod("getSystemLoadAverage").getReturnType() == double.class, "getSystemLoadAverage returns double");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        OperatingSystemMXBean b = ManagementFactory.getOperatingSystemMXBean();
        check(b != null, "non-null");
        check(b.getName() != null && !b.getName().isEmpty(), "os name non-empty");
        check(b.getArch() != null && !b.getArch().isEmpty(), "arch non-empty");
        check(b.getVersion() != null && !b.getVersion().isEmpty(), "version non-empty");
        check(b.getAvailableProcessors() >= 1, "available processors >= 1");
        check(b.getSystemLoadAverage() >= -1.0, "load average >= -1.0");
        check(b.getObjectName() != null, "objectName non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
