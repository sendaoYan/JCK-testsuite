/*
 * @test
 * @summary java.lang.management.MemoryManagerMXBean conformance: name/pools/validity.
 * @run main MemoryManagerMXBeanTest
 */

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryManagerMXBean;
import java.util.List;

public class MemoryManagerMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMemoryManagerMXBean() {
        List<MemoryManagerMXBean> mgrs = ManagementFactory.getMemoryManagerMXBeans();
        check(mgrs.size() > 0, "has managers");
        MemoryManagerMXBean mgr = mgrs.get(0);
        check(mgr.getName() != null, "getName");
        check(mgr.getMemoryPoolNames() != null, "getMemoryPoolNames");
        check(mgr.getObjectName() != null, "getObjectName");
        check(mgr.isValid(), "isValid");
    }

    public static void main(String[] args) {
        testMemoryManagerMXBean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
