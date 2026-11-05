/*
 * @test
 * @summary java.lang.management.ThreadInfo conformance: via ThreadMXBean.
 * @run main ThreadInfoTest
 */

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class ThreadInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThreadInfo() {
        ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
        long[] ids = tmx.getAllThreadIds();
        check(ids.length > 0, "has thread ids");
        ThreadInfo ti = tmx.getThreadInfo(ids[0]);
        check(ti != null, "getThreadInfo");
        check(ti.getThreadId() == ids[0], "getThreadId");
        check(ti.getThreadName() != null, "getThreadName");
        check(ti.getThreadState() != null, "getThreadState");
        check(ti.getBlockedCount() >= 0, "getBlockedCount");
        check(ti.getWaitedCount() >= 0, "getWaitedCount");
        check(ti.isInNative() == ti.isInNative(), "isInNative callable");
        check(ti.toString() != null, "toString");
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

    static void testDeepCurrentThreadSnapshot() {
        java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
        long id = Thread.currentThread().getId();
        java.lang.management.ThreadInfo ti = tm.getThreadInfo(id);
        check(ti != null, "current thread info non-null");
        check(ti.getThreadId() == id, "getThreadId round-trip");
        check(Thread.currentThread().getName().equals(ti.getThreadName()), "getThreadName round-trip");
        check(ti.getThreadState() != null, "getThreadState non-null");
        check(ti.getLockName() == null, "getLockName null when not waiting for a monitor");
        check(ti.getLockOwnerId() == -1, "getLockOwnerId -1 when no owner");
        check(ti.getLockOwnerName() == null, "getLockOwnerName null when no owner");
        check(ti.getLockInfo() == null, "getLockInfo null when not blocked");
        check(!ti.isSuspended(), "current thread not suspended");
        check(ti.getBlockedCount() >= 0, "getBlockedCount >= 0");
        check(ti.getWaitedCount() >= 0, "getWaitedCount >= 0");
        check(ti.getBlockedTime() >= -1, "getBlockedTime >= -1");
        check(ti.getWaitedTime() >= -1, "getWaitedTime >= -1");
        String s = ti.toString();
        check(s.contains(Thread.currentThread().getName()), "toString contains thread name");
        check(s.contains(ti.getThreadState().name()), "toString contains state name");
    }

    static void testDeepStackDepthContract() {
        java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
        long id = Thread.currentThread().getId();
        check(tm.getThreadInfo(id).getStackTrace().length == 0,
              "plain getThreadInfo(id) captures a zero-depth stack (no frames)");
        check(tm.getThreadInfo(id, 0).getStackTrace().length == 0, "maxDepth 0 -> 0 frames");
        java.lang.management.ThreadInfo two = tm.getThreadInfo(id, 2);
        check(two.getStackTrace().length == 2, "maxDepth 2 -> exactly 2 frames");
        java.lang.management.ThreadInfo full = tm.getThreadInfo(id, Integer.MAX_VALUE);
        java.lang.StackTraceElement[] st = full.getStackTrace();
        check(st.length >= 1, "full-depth stack non-empty");
        boolean sawThisClass = false;
        for (java.lang.StackTraceElement e : st) {
            if (e.getClassName().contains("ThreadInfoTest")) {
                sawThisClass = true;
                break;
            }
        }
        check(sawThisClass, "full-depth stack contains this test class frame");
    }

    static void testDeepFromNullAndDeadThread() throws Exception {
        check(java.lang.management.ThreadInfo.from(null) == null,
              "ThreadInfo.from(null) returns null (no NPE)");
        Thread shortLived = new Thread(new Runnable() { public void run() { } }, "DyingThread");
        shortLived.start();
        shortLived.join(10000);
        check(shortLived.getState() == Thread.State.TERMINATED, "helper thread terminated");
        java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
        check(tm.getThreadInfo(shortLived.getId()) == null,
              "getThreadInfo(dead id) returns null, no exception");
        java.lang.management.ThreadInfo[] arr = tm.getThreadInfo(new long[]{shortLived.getId()});
        check(arr.length == 1 && arr[0] == null, "array form yields null element for dead id");
    }

    static void testDeepInvalidIdMatrix() {
        final java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
        expectIAE(new Runnable() { public void run() { tm.getThreadInfo(-1L); } },
                  "getThreadInfo(-1)", "Invalid thread ID", "-1");
        expectIAE(new Runnable() { public void run() { tm.getThreadInfo(0L); } },
                  "getThreadInfo(0)", "Invalid thread ID", "0");
        expectIAE(new Runnable() { public void run() { tm.getThreadInfo(-1L, 5); } },
                  "getThreadInfo(-1,5)", "Invalid thread ID");
        expectIAE(new Runnable() { public void run() { tm.getThreadInfo(new long[]{-1L}); } },
                  "getThreadInfo({-1})", "Invalid thread ID");
        expectIAE(new Runnable() { public void run() { tm.getThreadInfo(new long[]{-1L}, 5); } },
                  "getThreadInfo({-1},5)", "Invalid thread ID");
        expectIAE(new Runnable() { public void run() { tm.getThreadCpuTime(-1L); } },
                  "getThreadCpuTime(-1)", "Invalid thread ID");
        expectIAE(new Runnable() { public void run() { tm.getThreadUserTime(-1L); } },
                  "getThreadUserTime(-1)", "Invalid thread ID");
        // empty id array: legal, returns a length-0 array; unknown-but-valid id returns null
        check(tm.getThreadInfo(new long[0]).length == 0, "getThreadInfo(long[0]) -> length 0");
        check(tm.getThreadInfo(99999999L) == null, "getThreadInfo(never-used id) returns null");
    }

    static void testDeepBlockedLockChain() throws Exception {
        startLockChain();
        try {
            java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
            java.lang.management.ThreadInfo bi = tm.getThreadInfo(chainBlocked.getId(), Integer.MAX_VALUE);
            check(bi.getThreadState() == Thread.State.BLOCKED, "blocked waiter is BLOCKED");
            String expected = "java.lang.Object@" + Integer.toHexString(System.identityHashCode(CHAIN_LOCK));
            check(expected.equals(bi.getLockName()), "getLockName is className@identityHex");
            check(bi.getLockInfo() != null, "getLockInfo non-null while BLOCKED");
            check(expected.equals(bi.getLockInfo().toString()), "getLockInfo identity matches");
            check(bi.getLockOwnerId() == chainHolder.getId(), "owner id is the holder");
            check("ChainHolder".equals(bi.getLockOwnerName()), "owner name resolved");
            check(bi.getStackTrace().length >= 1, "blocked thread full-depth stack non-empty");
        } finally {
            stopLockChain();
        }
    }

    static void testDeepDumpAllThreads() {
        java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
        java.lang.management.ThreadInfo[] all = tm.dumpAllThreads(false, false);
        check(all.length >= 1, "dumpAllThreads non-empty");
        boolean sawCurrent = false;
        for (java.lang.management.ThreadInfo ti : all) {
            check(ti != null && ti.getThreadId() > 0, "dumped info has positive id");
            if (ti.getThreadId() == Thread.currentThread().getId()) {
                sawCurrent = true;
            }
        }
        check(sawCurrent, "dumpAllThreads includes the current thread");
    }

    static void testDeepVersionMethodSurface() throws Exception {
        // JDK 8: no getThreadInfo(long[],boolean,boolean,int) overload, no priority/daemon
        for (java.lang.reflect.Method m : java.lang.management.ThreadMXBean.class.getMethods()) {
            check(!(m.getName().equals("getThreadInfo") && m.getParameterTypes().length == 4),
                  "JDK 8 has no 4-arg getThreadInfo");
        }
        for (java.lang.reflect.Method m : java.lang.management.ThreadInfo.class.getMethods()) {
            check(!m.getName().equals("getPriority") && !m.getName().equals("isDaemon"),
                  "JDK 8 ThreadInfo has no getPriority/isDaemon (added in JDK 14)");
        }
    }

    public static void main(String[] args) throws Exception {
        testThreadInfo();
        testDeepCurrentThreadSnapshot();
        testDeepStackDepthContract();
        testDeepFromNullAndDeadThread();
        testDeepInvalidIdMatrix();
        testDeepBlockedLockChain();
        testDeepDumpAllThreads();
        testDeepVersionMethodSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
