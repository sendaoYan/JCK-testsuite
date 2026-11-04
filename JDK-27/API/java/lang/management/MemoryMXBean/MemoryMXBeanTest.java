/*
 * @test
 * @summary java.lang.management.MemoryMXBean conformance: white-box structure + black-box behavior.
 * @run main MemoryMXBeanTest
 */
import java.lang.management.*;

public class MemoryMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.MemoryMXBean");
        check(c.isInterface(), "interface");
        check(PlatformManagedObject.class.isAssignableFrom(c), "extends PlatformManagedObject");
        check(c.getMethod("getObjectPendingFinalizationCount").getReturnType() == int.class,
              "getObjectPendingFinalizationCount returns int");
        check(c.getMethod("getHeapMemoryUsage").getReturnType() == MemoryUsage.class,
              "getHeapMemoryUsage returns MemoryUsage");
        check(c.getMethod("getNonHeapMemoryUsage").getReturnType() == MemoryUsage.class,
              "getNonHeapMemoryUsage returns MemoryUsage");
        check(c.getMethod("isVerbose").getReturnType() == boolean.class, "isVerbose returns boolean");
        check(c.getMethod("setVerbose", boolean.class).getReturnType() == void.class, "setVerbose returns void");
        check(c.getMethod("gc").getReturnType() == void.class, "gc returns void");
        check(c.getMethod("getTotalGcCpuTime").getReturnType() == long.class, "getTotalGcCpuTime returns long");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        MemoryMXBean b = ManagementFactory.getMemoryMXBean();
        check(b != null, "non-null");

        MemoryUsage heap = b.getHeapMemoryUsage();
        check(heap != null, "heap usage non-null");
        check(heap.getUsed() >= 0, "heap used non-negative");
        check(heap.getCommitted() >= heap.getUsed(), "heap committed >= used");
        check(heap.getMax() >= -1, "heap max >= -1");
        if (heap.getMax() > 0) check(heap.getMax() >= heap.getUsed(), "heap max >= used when defined");
        check(heap.getInit() >= -1, "heap init >= -1");

        MemoryUsage nonHeap = b.getNonHeapMemoryUsage();
        check(nonHeap != null, "non-heap usage non-null");
        check(nonHeap.getUsed() >= 0, "non-heap used non-negative");
        check(nonHeap.getCommitted() >= nonHeap.getUsed(), "non-heap committed >= used");

        check(b.getObjectPendingFinalizationCount() >= 0, "pending finalization >= 0");
        check(b.getObjectName() != null, "objectName non-null");
        check(b.getTotalGcCpuTime() >= -1, "total GC cpu time >= -1");

        boolean original = b.isVerbose();
        b.setVerbose(false);
        check(!b.isVerbose(), "setVerbose(false) applied");
        b.setVerbose(original);
        check(b.isVerbose() == original, "verbose restored");

        b.gc(); // callable; no effect asserted (non-deterministic)
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
