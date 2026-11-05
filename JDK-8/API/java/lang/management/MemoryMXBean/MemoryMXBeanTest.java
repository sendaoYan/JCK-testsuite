/*
 * @test
 * @summary java.lang.management.MemoryMXBean conformance: heap/non-heap usage and gc.
 * @run main MemoryMXBeanTest
 */

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class MemoryMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMemoryMXBean() {
        MemoryMXBean mmx = ManagementFactory.getMemoryMXBean();
        check(mmx.getHeapMemoryUsage() != null, "getHeapMemoryUsage");
        check(mmx.getNonHeapMemoryUsage() != null, "getNonHeapMemoryUsage");
        check(mmx.getObjectPendingFinalizationCount() >= 0, "getObjectPendingFinalizationCount");
        check(mmx.getObjectName() != null, "getObjectName");
        boolean vb = mmx.isVerbose();
        mmx.setVerbose(vb);
        mmx.gc();
        check(true, "gc/setVerbose callable");
    }


    static void testDeepUsageInvariants() {
        MemoryMXBean b = ManagementFactory.getMemoryMXBean();
        java.lang.management.MemoryUsage heap = b.getHeapMemoryUsage();
        check(heap.getInit() > 0, "heap init > 0 (initial heap capacity configured)");
        check(heap.getUsed() <= heap.getCommitted(), "heap used <= committed");
        if (heap.getMax() != -1) {
            check(heap.getCommitted() <= heap.getMax(), "heap committed <= max when max defined");
        }
        java.lang.management.MemoryUsage nonHeap = b.getNonHeapMemoryUsage();
        check(nonHeap.getInit() > 0, "non-heap init > 0");
        check(nonHeap.getUsed() <= nonHeap.getCommitted(), "non-heap used <= committed");
        check(b.getObjectPendingFinalizationCount() >= 0, "pending finalization >= 0");
    }

    static void testDeepVerboseRoundtrip() {
        MemoryMXBean b = ManagementFactory.getMemoryMXBean();
        boolean original = b.isVerbose();
        b.setVerbose(true);
        check(b.isVerbose(), "setVerbose(true) applied");
        b.setVerbose(false);
        check(!b.isVerbose(), "setVerbose(false) applied");
        b.setVerbose(original);
        check(b.isVerbose() == original, "verbose restored");
    }

    static void testDeepObjectNameExact() throws Exception {
        MemoryMXBean b = ManagementFactory.getMemoryMXBean();
        check("java.lang:type=Memory".equals(b.getObjectName().getCanonicalName()),
              "MemoryMXBean ObjectName is java.lang:type=Memory, got " + b.getObjectName());
    }

    static void testDeepNotificationListenerWeak() throws Exception {
        // Weak, flake-free contract: registration/removal must work; if a notification
        // happens to arrive its type must be one of the two documented memory types.
        // Thresholds are disabled by default, so normally none arrive.
        MemoryMXBean b = ManagementFactory.getMemoryMXBean();
        check(b instanceof javax.management.NotificationEmitter,
              "MemoryMXBean is a NotificationEmitter");
        javax.management.NotificationEmitter emitter = (javax.management.NotificationEmitter) b;
        final java.util.List<javax.management.Notification> received =
                java.util.Collections.synchronizedList(new java.util.ArrayList<javax.management.Notification>());
        javax.management.NotificationListener listener = new javax.management.NotificationListener() {
            public void handleNotification(javax.management.Notification n, Object handback) {
                received.add(n);
            }
        };
        emitter.addNotificationListener(listener, null, null);
        try {
            byte[] churn = new byte[32 * 1024 * 1024]; // exercised, then discarded
            check(churn.length == 32 * 1024 * 1024, "allocation observed");
            b.gc();
            check(b.getObjectPendingFinalizationCount() >= 0, "still healthy after gc()");
            for (javax.management.Notification n : received) {
                check(java.lang.management.MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED.equals(n.getType())
                        || java.lang.management.MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED.equals(n.getType()),
                      "any delivered notification has a documented type, got " + n.getType());
                check(n.getUserData() != null, "memory notification carries CompositeData userData");
                if (n.getUserData() instanceof javax.management.openmbean.CompositeData) {
                    java.lang.management.MemoryNotificationInfo info =
                            java.lang.management.MemoryNotificationInfo.from((javax.management.openmbean.CompositeData) n.getUserData());
                    check(info != null && info.getPoolName() != null,
                          "userData converts back via MemoryNotificationInfo.from");
                }
            }
        } finally {
            emitter.removeNotificationListener(listener, null, null);
        }
    }

    public static void main(String[] args) throws Exception {
        testMemoryMXBean();
        testDeepUsageInvariants();
        testDeepVerboseRoundtrip();
        testDeepObjectNameExact();
        testDeepNotificationListenerWeak();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
