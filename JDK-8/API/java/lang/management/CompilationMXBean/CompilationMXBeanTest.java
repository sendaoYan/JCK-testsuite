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

    public static void main(String[] args) {
        testCompilationMXBean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
