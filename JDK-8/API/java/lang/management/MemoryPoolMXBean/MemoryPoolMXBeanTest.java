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


    static void expectIAE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check(true, what + " throws IllegalArgumentException");
            for (String f : fragments) {
                check(e.getMessage() != null && e.getMessage().contains(f),
                      what + " message contains '" + f + "' (got: " + e.getMessage() + ")");
            }
        }
    }

    static void expectNPE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw NullPointerException");
        } catch (NullPointerException e) {
            check(true, what + " throws NullPointerException");
            for (String f : fragments) {
                check(e.getMessage() == null || e.getMessage().contains(f),
                      what + " message contains '" + f + "' or is null (got: " + e.getMessage() + ")");
            }
        }
    }

    static void expectUOE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            check(true, what + " throws UnsupportedOperationException");
            for (String f : fragments) {
                check(e.getMessage() != null && e.getMessage().contains(f),
                      what + " message contains '" + f + "' (got: " + e.getMessage() + ")");
            }
        }
    }

    static void testDeepPerPoolContract() {
        java.util.List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        java.util.Set<String> managerUniverse = new java.util.HashSet<String>();
        for (java.lang.management.MemoryManagerMXBean m : ManagementFactory.getMemoryManagerMXBeans()) {
            managerUniverse.add(m.getName());
        }
        boolean sawHeap = false;
        boolean sawNonHeap = false;
        for (MemoryPoolMXBean p : pools) {
            check(p.getName() != null && !p.getName().isEmpty(), "pool name non-empty");
            check(p.isValid(), "pool valid");
            java.lang.management.MemoryType t = p.getType();
            check(t == java.lang.management.MemoryType.HEAP || t == java.lang.management.MemoryType.NON_HEAP, "type in {HEAP, NON_HEAP}");
            if (t == java.lang.management.MemoryType.HEAP) {
                sawHeap = true;
            } else {
                sawNonHeap = true;
            }
            java.lang.management.MemoryUsage u = p.getUsage();
            check(u != null, "usage non-null");
            check(u.getUsed() <= u.getCommitted(), "used <= committed for " + p.getName());
            check(u.getMax() >= -1, "max >= -1 (or the -1 sentinel)");
            check(p.getPeakUsage() != null, "peak usage non-null");
            check(("java.lang:name=" + p.getName() + ",type=MemoryPool")
                    .equals(p.getObjectName().getCanonicalName()),
                  "pool ObjectName pattern exact for " + p.getName() + ": " + p.getObjectName());
            for (String mn : p.getMemoryManagerNames()) {
                check(managerUniverse.contains(mn),
                      "manager " + mn + " of pool " + p.getName() + " exists in the manager list");
            }
            java.lang.management.MemoryUsage cu = p.getCollectionUsage();
            check(cu == null || cu.getUsed() <= cu.getCommitted(),
                  "collection usage null or consistent for " + p.getName());
        }
        check(sawHeap && sawNonHeap, "both heap and non-heap pools exist");
    }

    static void testDeepThresholdLifecycle() {
        MemoryPoolMXBean target = null;
        for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) {
            if (p.isUsageThresholdSupported()) {
                target = p;
                break;
            }
        }
        check(target != null, "at least one pool supports usage thresholds");
        long used = target.getUsage().getUsed();
        target.setUsageThreshold(used + 1);
        check(target.getUsageThreshold() == used + 1, "usage threshold round-trips the set value");
        check(target.getUsageThresholdCount() >= 0, "threshold count >= 0");
        target.setUsageThreshold(0); // 0 disables the threshold
        check(target.getUsageThreshold() == 0, "usage threshold 0 disables");
        final java.lang.management.MemoryPoolMXBean thr = target;
        expectIAE(new Runnable() { public void run() { thr.setUsageThreshold(-1); } },
                  "setUsageThreshold(-1)");
        if (target.isCollectionUsageThresholdSupported()) {
            long cu = target.getUsage().getUsed();
            target.setCollectionUsageThreshold(cu + 1);
            check(target.getCollectionUsageThreshold() == cu + 1,
                  "collection threshold round-trips the set value");
            target.setCollectionUsageThreshold(0);
            check(target.getCollectionUsageThreshold() == 0, "collection threshold 0 disables");
        }
    }

    static void testDeepResetPeakUsage() {
        MemoryPoolMXBean p = ManagementFactory.getMemoryPoolMXBeans().get(0);
        p.resetPeakUsage();
        java.lang.management.MemoryUsage peak = p.getPeakUsage();
        check(peak != null, "peak usage non-null after reset");
        check(peak.getUsed() >= 0, "peak used >= 0 after reset");
        check(p.isValid(), "pool still valid after reset");
    }

    public static void main(String[] args) throws Exception {
        testMemoryPoolMXBean();
        testDeepPerPoolContract();
        testDeepThresholdLifecycle();
        testDeepResetPeakUsage();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
