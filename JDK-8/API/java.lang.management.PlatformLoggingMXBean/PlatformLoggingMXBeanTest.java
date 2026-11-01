/*
 * @test
 * @summary java.lang.management.PlatformLoggingMXBean conformance: logger names.
 * @run main PlatformLoggingMXBeanTest
 */

import java.lang.management.ManagementFactory;
import java.lang.management.PlatformLoggingMXBean;
import java.util.List;

public class PlatformLoggingMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPlatformLoggingMXBean() {
        List<PlatformLoggingMXBean> beans = ManagementFactory.getPlatformMXBeans(
                PlatformLoggingMXBean.class);
        check(beans.size() > 0, "has logging MXBean");
        PlatformLoggingMXBean p = beans.get(0);
        check(p.getLoggerNames() != null, "getLoggerNames");
        check(p.getObjectName() != null, "getObjectName");
    }

    public static void main(String[] args) {
        testPlatformLoggingMXBean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
