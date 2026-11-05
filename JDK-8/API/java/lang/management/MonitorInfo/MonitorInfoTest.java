/*
 * @test
 * @summary java.lang.management.MonitorInfo conformance: locked stack depth/frame.
 * @run main MonitorInfoTest
 */

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;

public class MonitorInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMonitorInfo() {
        MonitorInfo mi = new MonitorInfo("java.lang.Object", 123, 5,
                new StackTraceElement("C", "m", "F", 1));
        check("java.lang.Object".equals(mi.getClassName()), "getClassName");
        check(mi.getIdentityHashCode() == 123, "getIdentityHashCode");
        check(mi.getLockedStackDepth() == 5, "getLockedStackDepth");
        check("C".equals(mi.getLockedStackFrame().getClassName()), "getLockedStackFrame");
        check(mi instanceof LockInfo, "extends LockInfo");
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

    static final Object CHAIN_LOCK = new Object();
    static volatile Thread chainHolder;
    static volatile Thread chainBlocked;
    static java.util.concurrent.CountDownLatch chainAcquired;

    static void startLockChain() throws Exception {
        chainAcquired = new java.util.concurrent.CountDownLatch(1);
        chainHolder = new Thread(new Runnable() {
            public void run() {
                synchronized (CHAIN_LOCK) {
                    chainAcquired.countDown();
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        // release and exit
                    }
                }
            }
        }, "ChainHolder");
        chainHolder.start();
        check(chainAcquired.await(10, java.util.concurrent.TimeUnit.SECONDS),
              "holder acquired the monitor (latch)");
        chainBlocked = new Thread(new Runnable() {
            public void run() {
                synchronized (CHAIN_LOCK) {
                }
            }
        }, "ChainBlocked");
        chainBlocked.start();
        long deadline = System.nanoTime() + 10000000000L;
        while (chainBlocked.getState() != Thread.State.BLOCKED
                && System.nanoTime() < deadline) {
            Thread.yield();
        }
    }

    static void stopLockChain() throws Exception {
        chainHolder.interrupt();
        chainHolder.join(10000);
        chainBlocked.join(10000);
    }

    static void testDeepConstructorBoundaries() {
        // documented pair rule: depth 0 (outside any frame) requires a null frame;
        // negative depth is the "unknown" sentinel and also pairs with null
        expectIAE(new Runnable() { public void run() { new MonitorInfo("java.lang.Object", 5, 0, null); } },
                  "depth 0 with null frame", "stackDepth");
        MonitorInfo zeroed = new MonitorInfo("java.lang.Object", 5, 0,
                new StackTraceElement("C", "m", "C.java", 1));
        check(zeroed.getLockedStackDepth() == 0 && zeroed.getLockedStackFrame() != null,
              "depth 0 accepted when a frame is supplied");
        MonitorInfo negative = new MonitorInfo("java.lang.Object", 5, -1, null);
        check(negative.getLockedStackDepth() == -1, "negative depth stored as given");
        check(negative.getLockedStackFrame() == null, "negative depth pairs with null frame");
        MonitorInfo withFrame = new MonitorInfo("C", 9, 3,
                new StackTraceElement("Owner", "m", "Owner.java", 17));
        check(withFrame.getLockedStackFrame() != null
                && "Owner".equals(withFrame.getLockedStackFrame().getClassName())
                && "m".equals(withFrame.getLockedStackFrame().getMethodName())
                && withFrame.getLockedStackFrame().getLineNumber() == 17,
              "getLockedStackFrame round-trips depth/frame");
        check(("C@" + Integer.toHexString(9)).equals(withFrame.toString()),
              "MonitorInfo inherits LockInfo toString format");
    }

    static void testDeepNullClassName() {
        expectNPE(new Runnable() { public void run() { new MonitorInfo(null, 1, -1, null); } },
                  "className=null", "className");
    }

    static void testDeepFromNull() {
        check(MonitorInfo.from(null) == null, "from(null) returns null (no NPE)");
    }

    static void testDeepRealLockedMonitors() throws Exception {
        java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
        check(tm.isObjectMonitorUsageSupported(), "object monitor usage supported (HotSpot)");
        startLockChain();
        try {
            // shallow snapshot: locked monitors are NOT captured -> empty array
            java.lang.management.ThreadInfo shallow = tm.getThreadInfo(chainHolder.getId());
            check(shallow != null && shallow.getLockedMonitors().length == 0,
                  "plain getThreadInfo captures no locked monitors");
            // deep snapshot (lockedMonitors=true): the held monitor is reported with its frame
            java.lang.management.ThreadInfo[] deep =
                    tm.getThreadInfo(new long[]{chainHolder.getId()}, true, true);
            java.lang.management.MonitorInfo[] locked = deep[0].getLockedMonitors();
            check(locked.length >= 1, "deep snapshot reports the held monitor");
            boolean found = false;
            for (java.lang.management.MonitorInfo m : locked) {
                check(m instanceof java.lang.management.LockInfo, "MonitorInfo is a LockInfo");
                check(m.getLockedStackDepth() >= 1, "locked depth >= 1: " + m.getLockedStackDepth());
                check(m.getLockedStackFrame() != null, "locked frame non-null");
                check(m.getLockedStackFrame().getClassName().contains("MonitorInfoTest"),
                      "locked frame is inside this test class, got " + m.getLockedStackFrame());
                if (m.getIdentityHashCode() == System.identityHashCode(CHAIN_LOCK)) {
                    found = true;
                }
            }
            check(found, "locked monitor identity matches System.identityHashCode of the lock object");
            check(deep[0].getLockedSynchronizers().length == 0,
                  "no java.util.concurrent locks held -> empty synchronizer array");
        } finally {
            stopLockChain();
        }
    }

    public static void main(String[] args) throws Exception {
        testMonitorInfo();
        testDeepConstructorBoundaries();
        testDeepNullClassName();
        testDeepFromNull();
        testDeepRealLockedMonitors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
