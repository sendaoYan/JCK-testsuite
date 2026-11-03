/*
 * @test
 * @summary java.lang.management.CompilationMXBean conformance: white-box structure + black-box behavior.
 * @run main CompilationMXBeanTest
 */
import java.lang.management.*;

public class CompilationMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.CompilationMXBean");
        check(c.isInterface(), "interface");
        check(PlatformManagedObject.class.isAssignableFrom(c), "extends PlatformManagedObject");
        check(c.getMethod("getName").getReturnType() == String.class, "getName returns String");
        check(c.getMethod("isCompilationTimeMonitoringSupported").getReturnType() == boolean.class,
              "isCompilationTimeMonitoringSupported returns boolean");
        check(c.getMethod("getTotalCompilationTime").getReturnType() == long.class, "getTotalCompilationTime returns long");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CompilationMXBean b = ManagementFactory.getCompilationMXBean();
        check(b != null, "non-null");
        check(b.getName() != null && !b.getName().isEmpty(), "compiler name non-empty");
        check(b.getObjectName() != null, "objectName non-null");
        boolean supported = b.isCompilationTimeMonitoringSupported();
        if (supported) {
            check(b.getTotalCompilationTime() >= 0, "total compilation time >= 0 when supported");
        } else {
            check(b.getTotalCompilationTime() == 0, "total compilation time 0 when unsupported");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
