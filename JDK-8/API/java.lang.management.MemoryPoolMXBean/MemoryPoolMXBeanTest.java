/*
 * @test
 * @summary java.lang.management.MemoryPoolMXBean conformance: name/type/usage.
 * @run main MemoryPoolMXBeanTest
 */

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

public class MemoryPoolMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMemoryPoolMXBean() {
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        check(pools.size() > 0, "has pools");
        MemoryPoolMXBean pool = pools.get(0);
        check(pool.getName() != null, "getName");
        check(pool.getType() != null, "getType");
        check(pool.getUsage() != null, "getUsage");
        check(pool.getPeakUsage() != null, "getPeakUsage");
        check(pool.getUsageThreshold() >= 0, "getUsageThreshold");
        check(pool.getObjectName() != null, "getObjectName");
        check(pool.isValid(), "isValid");
    }

    public static void main(String[] args) {
        testMemoryPoolMXBean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
