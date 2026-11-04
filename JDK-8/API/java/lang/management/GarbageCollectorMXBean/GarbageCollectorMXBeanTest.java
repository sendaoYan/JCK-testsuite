/*
 * @test
 * @summary java.lang.management.GarbageCollectorMXBean conformance: count/time/name.
 * @run main GarbageCollectorMXBeanTest
 */

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

public class GarbageCollectorMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testGarbageCollectorMXBean() {
        List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
        check(gcs.size() > 0, "has GC beans");
        GarbageCollectorMXBean gc = gcs.get(0);
        check(gc.getName() != null, "getName");
        check(gc.getCollectionCount() >= 0, "getCollectionCount");
        check(gc.getCollectionTime() >= 0, "getCollectionTime");
        check(gc.getMemoryPoolNames() != null, "getMemoryPoolNames");
        check(gc.getObjectName() != null, "getObjectName");
        check(gc.isValid(), "isValid");
    }

    public static void main(String[] args) {
        testGarbageCollectorMXBean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
