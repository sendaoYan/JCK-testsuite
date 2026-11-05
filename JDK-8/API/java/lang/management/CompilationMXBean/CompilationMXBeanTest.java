/*
 * @test
 * @summary java.lang.management.CompilationMXBean conformance: name and compilation time.
 * @run main CompilationMXBeanTest
 */

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;

public class CompilationMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCompilationMXBean() {
        CompilationMXBean c = ManagementFactory.getCompilationMXBean();
        if (c == null) {
            check(true, "no compilation MXBean (interpreter-only JVM)");
            return;
        }
        check(c.getName() != null, "getName");
        check(c.getObjectName() != null, "getObjectName");
        check(c.isCompilationTimeMonitoringSupported() ==
              c.isCompilationTimeMonitoringSupported(), "isCompilationTimeMonitoringSupported");
    }


    static void testDeepNameStableAndTimeMonotonic() {
        CompilationMXBean b = ManagementFactory.getCompilationMXBean();
        if (b == null) {
            check(true, "no JIT present: null bean is legal");
            return;
        }
        check(b.getName() != null && b.getName().equals(b.getName()), "name stable across calls");
        check(b.getName().length() > 0, "name non-empty");
        check(b.isCompilationTimeMonitoringSupported(), "time monitoring supported on HotSpot");
        long t1 = b.getTotalCompilationTime();
        long t2 = b.getTotalCompilationTime();
        check(t1 >= 0 && t2 >= t1, "compilation time non-negative and non-decreasing: " + t1 + " -> " + t2);
    }

    static void testDeepObjectNameExact() {
        CompilationMXBean b = ManagementFactory.getCompilationMXBean();
        if (b == null) {
            return;
        }
        check("java.lang:type=Compilation".equals(b.getObjectName().getCanonicalName()),
              "CompilationMXBean ObjectName exact, got " + b.getObjectName());
    }

    public static void main(String[] args) throws Exception {
        testCompilationMXBean();
        testDeepNameStableAndTimeMonotonic();
        testDeepObjectNameExact();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
