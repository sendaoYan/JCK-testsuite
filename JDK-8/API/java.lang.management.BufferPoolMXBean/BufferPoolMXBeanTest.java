/*
 * @test
 * @summary java.lang.management.BufferPoolMXBean conformance: name/count/capacity/used.
 * @run main BufferPoolMXBeanTest
 */

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

public class BufferPoolMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBufferPoolMXBean() {
        List<BufferPoolMXBean> pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        check(pools.size() > 0, "has buffer pools");
        BufferPoolMXBean bp = pools.get(0);
        check(bp.getName() != null, "getName");
        check(bp.getCount() >= 0, "getCount");
        check(bp.getTotalCapacity() >= 0, "getTotalCapacity");
        check(bp.getMemoryUsed() >= 0, "getMemoryUsed");
        check(bp.getObjectName() != null, "getObjectName");
    }

    public static void main(String[] args) {
        testBufferPoolMXBean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
