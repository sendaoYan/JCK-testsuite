/*
 * @test
 * @summary java.lang.management.LockInfo conformance: class name and identity hash.
 * @run main LockInfoTest
 */

import java.lang.management.LockInfo;

public class LockInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLockInfo() {
        LockInfo li = new LockInfo("java.util.concurrent.locks.ReentrantLock", 12345);
        check("java.util.concurrent.locks.ReentrantLock".equals(li.getClassName()), "getClassName");
        check(li.getIdentityHashCode() == 12345, "getIdentityHashCode");
        check(li.toString() != null, "toString");
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

    static void testDeepToStringFormat() {
        LockInfo li = new LockInfo("java.lang.Object", 1234567);
        check(("java.lang.Object@" + Integer.toHexString(1234567)).equals(li.toString()),
              "toString is className@identityHashCodeHex, got " + li.toString());
        LockInfo zero = new LockInfo("java.lang.Object", 0);
        check("java.lang.Object@0".equals(zero.toString()), "identity hash 0 renders as 0");
        LockInfo neg = new LockInfo("java.lang.Object", -1);
        check(("java.lang.Object@" + Integer.toHexString(-1)).equals(neg.toString()),
              "negative identity hash renders unsigned hex");
        LockInfo plain = new LockInfo("X", 7);
        check("X@7".equals(plain.toString()), "short class name unqualified, no padding");
    }

    static void testDeepNullClassName() {
        expectNPE(new Runnable() { public void run() { new LockInfo(null, 1); } },
                  "className=null", "className");
        // identity hash 0 and negative are accepted; only the name is validated
        check(new LockInfo("C", 0).getIdentityHashCode() == 0, "identity hash 0 legal");
    }

    static void testDeepFromNull() {
        check(LockInfo.from(null) == null, "from(null) returns null (no NPE)");
    }

    static void testDeepIdentityEquals() {
        // LockInfo does not override equals/hashCode: identity semantics
        LockInfo a = new LockInfo("C", 1);
        LockInfo b = new LockInfo("C", 1);
        check(a.equals(a), "equals self");
        check(!a.equals(b), "no value equals: identity semantics");
        check(!a.equals(null), "equals(null) false");
    }

    static void testDeepBlockedWaiterLockIdentity() throws Exception {
        startLockChain();
        try {
            java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
            java.lang.management.ThreadInfo bi = tm.getThreadInfo(chainBlocked.getId(), Integer.MAX_VALUE);
            check(bi != null, "blocked thread info non-null");
            check(bi.getThreadState() == Thread.State.BLOCKED, "waiter state BLOCKED, got " + bi.getThreadState());
            LockInfo info = bi.getLockInfo();
            check(info != null, "getLockInfo non-null while BLOCKED");
            String expected = "java.lang.Object@" + Integer.toHexString(System.identityHashCode(CHAIN_LOCK));
            check(expected.equals(info.toString()),
                  "getLockInfo identity matches the contended monitor: " + info + " vs " + expected);
            check(expected.equals(bi.getLockName()),
                  "getLockName is the fully qualified monitor identity");
            check(bi.getLockOwnerId() == chainHolder.getId(), "lock owner is the holder thread");
            check("ChainHolder".equals(bi.getLockOwnerName()), "lock owner name resolved");
        } finally {
            stopLockChain();
        }
    }

    public static void main(String[] args) throws Exception {
        testLockInfo();
        testDeepToStringFormat();
        testDeepNullClassName();
        testDeepFromNull();
        testDeepIdentityEquals();
        testDeepBlockedWaiterLockIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
