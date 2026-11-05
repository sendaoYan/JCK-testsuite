/*
 * @test
 * @summary java.lang.ThreadGroup conformance: white-box structure + black-box behavior.
 * @run main ThreadGroupTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ThreadGroupTest {
    static int passed;

    // Records the uncaught exception delivered to the group.
    static class RecordingGroup extends ThreadGroup {
        volatile Thread caughtThread;
        volatile Throwable caught;
        RecordingGroup(String name) { super(name); }
        @Override public void uncaughtException(Thread t, Throwable e) { caughtThread = t; caught = e; }
    }

    // ===================== 契约深化（2026-11）=====================

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void expectITSE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalThreadStateException");
        } catch (IllegalThreadStateException expected) {
            passed++;
        }
    }

    // Bounded (<= 10s) poll until the group reports the expected active count.
    static int awaitActiveCount(ThreadGroup g, int expected) {
        long deadline = System.nanoTime() + 10_000_000_000L;
        int c = g.activeCount();
        while (System.nanoTime() < deadline && (c = g.activeCount()) != expected) {
            Thread.yield();
        }
        return c;
    }

    static void testNestedHierarchy() {
        ThreadGroup a = new ThreadGroup("lvl-a");
        ThreadGroup b = new ThreadGroup(a, "lvl-b");
        ThreadGroup c = new ThreadGroup(b, "lvl-c");
        check("lvl-b".equals(b.getName()), "child name");
        check("lvl-c".equals(c.getName()), "grandchild name");
        check(c.getParent() == b, "grandchild parent is b");
        check(b.getParent() == a, "child parent is a");
        check(a.getParent() == Thread.currentThread().getThreadGroup(),
              "ThreadGroup(String) parent is the current thread's group");
        check(a.parentOf(a), "parentOf(self) true");
        check(a.parentOf(b) && a.parentOf(c), "parentOf(child/grandchild) true");
        check(b.parentOf(c) && !b.parentOf(a), "parentOf direction correct");
        check(!c.parentOf(a) && !c.parentOf(b), "leaf is parentOf nothing above");
        ThreadGroup unrelated = new ThreadGroup("unrelated");
        check(!a.parentOf(unrelated) && !unrelated.parentOf(a), "unrelated groups parentOf false");
        // Daemon flag is inherited by subgroups (probed on both JDKs).
        ThreadGroup dparent = new ThreadGroup("dm-parent");
        dparent.setDaemon(true);
        ThreadGroup dchild = new ThreadGroup(dparent, "dm-child");
        check(dchild.isDaemon(), "subgroup inherits daemon flag of parent");
        dparent.setDaemon(false);
        check(dchild.isDaemon(), "already-created subgroup keeps its daemon flag");
        // Null name is stored verbatim on JDK-27 too (no NPE for the name).
        check(new ThreadGroup((String) null).getName() == null, "ThreadGroup((String)null) stores null name");
        expectNPE(() -> new ThreadGroup((ThreadGroup) null, "x"), "ThreadGroup(null parent,name) NPE");
        check(new ThreadGroup(a, (String) null).getName() == null,
              "ThreadGroup(parent,(String)null) stores null name");
    }

    static void testMaxPriorityContract() {
        ThreadGroup g = new ThreadGroup("mp");
        check(g.getMaxPriority() == Thread.MAX_PRIORITY, "new group max is 10");
        g.setMaxPriority(7);
        check(g.getMaxPriority() == 7, "setMaxPriority(7)");
        g.setMaxPriority(10);
        check(g.getMaxPriority() == 10, "raising back to 10 allowed");
        g.setMaxPriority(3);
        check(g.getMaxPriority() == 3, "setMaxPriority(3)");
        g.setMaxPriority(10);
        check(g.getMaxPriority() == 10, "top-level group can be raised back (parent max is 10)");
        g.setMaxPriority(Thread.MIN_PRIORITY);
        check(g.getMaxPriority() == Thread.MIN_PRIORITY, "setMaxPriority(MIN)");
        // Out-of-range values are ignored entirely (locked by probe).
        ThreadGroup h = new ThreadGroup("mp-oob");
        h.setMaxPriority(0);
        check(h.getMaxPriority() == Thread.MAX_PRIORITY, "setMaxPriority(0) ignored");
        h.setMaxPriority(-1);
        check(h.getMaxPriority() == Thread.MAX_PRIORITY, "setMaxPriority(-1) ignored");
        h.setMaxPriority(11);
        check(h.getMaxPriority() == Thread.MAX_PRIORITY, "setMaxPriority(11) ignored at top level");

        // Clamp to the parent's max.
        ThreadGroup par = new ThreadGroup("mp-par");
        par.setMaxPriority(9);
        ThreadGroup ch = new ThreadGroup(par, "mp-ch");
        check(ch.getMaxPriority() == 9, "child inherits lowered parent max");
        ch.setMaxPriority(10);
        check(ch.getMaxPriority() == 9, "child setMax(10) clamped to parent 9");
        ch.setMaxPriority(3);
        ch.setMaxPriority(10);
        check(ch.getMaxPriority() == 9, "raise after lower still clamped to parent");
        ch.setMaxPriority(11);
        check(ch.getMaxPriority() == 9, "out-of-range setMax(11) ignored on child");
        ch.setMaxPriority(0);
        check(ch.getMaxPriority() == 9, "out-of-range setMax(0) ignored on child");

        // Changing the parent's max propagates to existing children.
        ThreadGroup p2 = new ThreadGroup("mp-p2");
        ThreadGroup c2 = new ThreadGroup(p2, "mp-c2");
        check(c2.getMaxPriority() == Thread.MAX_PRIORITY, "child starts at 10");
        p2.setMaxPriority(5);
        check(c2.getMaxPriority() == 5, "parent lower propagates to existing child");
        p2.setMaxPriority(8);
        check(c2.getMaxPriority() == 8, "parent raise propagates to existing child (clamped to new max)");
        // Grandchildren propagate too.
        ThreadGroup p3 = new ThreadGroup("mp-p3");
        ThreadGroup c3 = new ThreadGroup(p3, "mp-c3");
        ThreadGroup g3 = new ThreadGroup(c3, "mp-g3");
        p3.setMaxPriority(4);
        check(g3.getMaxPriority() == 4, "propagation reaches grandchildren");
    }

    static void testGroupMaxVsThreadPriority() throws Exception {
        ThreadGroup g = new ThreadGroup("mp-thread");
        g.setMaxPriority(4);
        // Thread priority is clamped at creation and by setPriority.
        Thread t = new Thread(g, () -> {});
        check(t.getPriority() == 4, "new thread priority clamped to group max");
        t.setPriority(9);
        check(t.getPriority() == 4, "setPriority clamped to group max");
        t.setPriority(2);
        check(t.getPriority() == 2, "priority below group max allowed");
        // Lowering the group max later does not touch existing threads.
        final CountDownLatch release = new CountDownLatch(1);
        Thread w = new Thread(g, () -> {
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        w.setDaemon(true);
        w.setPriority(4);
        w.start();
        try {
            g.setMaxPriority(2);
            check(w.getPriority() == 4, "existing thread priority unaffected by group max lower");
        } finally {
            release.countDown();
            w.join(10_000);
        }
        check(!w.isAlive(), "worker terminated");
    }

    static void testDaemonRoundtrip() {
        ThreadGroup g = new ThreadGroup("dm-rt");
        check(!g.isDaemon(), "new group non-daemon");
        g.setDaemon(true);
        check(g.isDaemon(), "setDaemon(true)");
        g.setDaemon(false);
        check(!g.isDaemon(), "setDaemon(false) roundtrip");
        g.setDaemon(true);
        check(g.isDaemon(), "setDaemon(true) again");
    }

    static void testActiveCountsPrecise() throws Exception {
        ThreadGroup g = new ThreadGroup("ac-precise");
        check(g.activeCount() == 0, "fresh group activeCount 0");
        check(g.activeGroupCount() == 0, "fresh group activeGroupCount 0");
        final CountDownLatch entered1 = new CountDownLatch(1);
        final CountDownLatch entered2 = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread w1 = new Thread(g, () -> {
            entered1.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "ac-w1");
        Thread w2 = new Thread(g, () -> {
            entered2.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "ac-w2");
        w1.setDaemon(true);
        w2.setDaemon(true);
        w1.start();
        check(entered1.await(10, TimeUnit.SECONDS), "w1 started");
        check(awaitActiveCount(g, 1) == 1, "activeCount 1 with one live thread");
        w2.start();
        check(entered2.await(10, TimeUnit.SECONDS), "w2 started");
        check(awaitActiveCount(g, 2) == 2, "activeCount 2 with two live threads");
        ThreadGroup sub = new ThreadGroup(g, "ac-sub");
        check(g.activeGroupCount() == 1, "activeGroupCount 1 after adding subgroup");
        ThreadGroup sub2 = new ThreadGroup(g, "ac-sub2");
        check(g.activeGroupCount() == 2, "activeGroupCount 2 after second subgroup");
        ThreadGroup[] subs = new ThreadGroup[3];
        int nsg = g.enumerate(subs, false);
        check(nsg == 2, "enumerate(ThreadGroup[]) returns both subgroups");
        boolean sawSub1 = false;
        boolean sawSub2 = false;
        for (int i = 0; i < nsg; i++) {
            if (subs[i] == sub) {
                sawSub1 = true;
            } else if (subs[i] == sub2) {
                sawSub2 = true;
            }
        }
        check(sawSub1 && sawSub2, "subgroup enumerate contains both by identity");
        check(subs[2] == null, "subgroup slots beyond n untouched");
        release.countDown();
        w1.join(10_000);
        w2.join(10_000);
        check(awaitActiveCount(g, 0) == 0, "activeCount back to 0 after both join");
    }

    static void testActiveCountRecursive() throws Exception {
        ThreadGroup outer = new ThreadGroup("acr-outer");
        ThreadGroup inner = new ThreadGroup(outer, "acr-inner");
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread w = new Thread(inner, () -> {
            entered.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "acr-worker");
        w.setDaemon(true);
        check(outer.activeCount() == 0, "outer count 0 before start");
        w.start();
        try {
            check(entered.await(10, TimeUnit.SECONDS), "worker started");
            check(awaitActiveCount(outer, 1) == 1, "outer activeCount includes subgroup threads");
            check(awaitActiveCount(inner, 1) == 1, "inner activeCount includes own threads");
        } finally {
            release.countDown();
            w.join(10_000);
        }
        check(awaitActiveCount(outer, 0) == 0, "outer count back to 0");
        check(inner.activeCount() == 0, "inner count back to 0");
        // Thread constructor accepts a null Runnable inside a group.
        check(new Thread(inner, (Runnable) null).getThreadGroup() == inner,
              "Thread(group,null) group membership");
    }

    static void testEnumeratePrecise() throws Exception {
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        ThreadGroup outer = new ThreadGroup("en-outer");
        ThreadGroup inner = new ThreadGroup(outer, "en-inner");
        Thread direct = new Thread(outer, () -> {
            entered.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "en-direct");
        Thread nested = new Thread(inner, () -> {
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "en-nested");
        direct.setDaemon(true);
        nested.setDaemon(true);
        direct.start();
        nested.start();
        try {
            check(entered.await(10, TimeUnit.SECONDS), "direct worker started");
            check(awaitActiveCount(outer, 2) == 2, "both workers alive in hierarchy");
            // Non-recursive: only the group's own threads.
            Thread[] arr = new Thread[4];
            int n = outer.enumerate(arr, false);
            check(n == 1, "enumerate(false) lists only own threads");
            check(arr[0] == direct, "enumerate(false) contains the direct worker");
            check(arr[1] == null && arr[2] == null && arr[3] == null, "slots beyond n untouched");
            // Recursive (one-arg and two-arg true): includes subgroup threads.
            java.util.Arrays.fill(arr, null);
            int nrec = outer.enumerate(arr);
            check(nrec == 2, "enumerate(Thread[]) recurses into subgroups");
            boolean sawDirect = false;
            boolean sawNested = false;
            for (int i = 0; i < nrec; i++) {
                if (arr[i] == direct) {
                    sawDirect = true;
                } else if (arr[i] == nested) {
                    sawNested = true;
                }
            }
            check(sawDirect && sawNested, "recursive enumerate contains both workers");
            check(outer.enumerate(new Thread[0], false) == 0, "zero-length array returns 0");
            // The inner group's own enumerate sees only its own thread.
            Thread[] innerArr = new Thread[2];
            int ni = inner.enumerate(innerArr, false);
            check(ni == 1 && innerArr[0] == nested, "inner enumerate(false) returns its own worker");
            // Exactly-sized array returns exactly the live count.
            int one = outer.enumerate(new Thread[1], false);
            check(one == 1, "exactly-sized array returns the count");
        } finally {
            release.countDown();
            direct.join(10_000);
            nested.join(10_000);
        }
        check(awaitActiveCount(outer, 0) == 0, "no active threads after join");
        check(outer.enumerate(new Thread[2], true) == 0, "enumerate empty after workers exit");
        // Group enumeration.
        ThreadGroup[] groups = new ThreadGroup[2];
        int ng = outer.enumerate(groups, false);
        check(ng == 1 && groups[0] == inner, "enumerate(ThreadGroup[],false) lists the subgroup");
        int ngRec = outer.enumerate(groups);
        check(ngRec == 1, "one-arg enumerate(ThreadGroup[]) same here (single subgroup)");
        // Null arrays -> NPE.
        expectNPE(() -> outer.enumerate((Thread[]) null), "enumerate((Thread[])null)");
        expectNPE(() -> outer.enumerate((ThreadGroup[]) null), "enumerate((ThreadGroup[])null)");
    }

    static void testToStringExact() {
        ThreadGroup g = new ThreadGroup("fmt-exact");
        check("java.lang.ThreadGroup[name=fmt-exact,maxpri=10]".equals(g.toString()),
              "toString exact format");
        g.setMaxPriority(7);
        check("java.lang.ThreadGroup[name=fmt-exact,maxpri=7]".equals(g.toString()),
              "toString reflects maxpri");
        g.setMaxPriority(2);
        check("java.lang.ThreadGroup[name=fmt-exact,maxpri=2]".equals(g.toString()),
              "toString tracks setMaxPriority");
        g.setMaxPriority(Thread.MIN_PRIORITY);
        check("java.lang.ThreadGroup[name=fmt-exact,maxpri=1]".equals(g.toString()),
              "toString clamps at MIN priority");
    }

    static void testEqualsHash() {
        ThreadGroup a = new ThreadGroup("eq-a");
        ThreadGroup b = new ThreadGroup("eq-a");
        check(a.equals(a), "identity equals");
        check(!a.equals(b), "distinct groups not equal despite same name");
        check(a.hashCode() == a.hashCode(), "hashCode stable");
        check(b.hashCode() == b.hashCode(), "hashCode stable (second group)");
    }

    static void testUncaughtRoutingDeep() throws Exception {
        // Real dispatch: a thread dying in a RecordingGroup delivers (t, e).
        RecordingGroup leaf = new RecordingGroup("uc-leaf");
        final RuntimeException boom = new RuntimeException("uc-boom");
        Thread t = new Thread(leaf, () -> {
            throw boom;
        }, "uc-worker");
        t.setDaemon(true);
        t.start();
        long deadline = System.nanoTime() + 10_000_000_000L;
        while (leaf.caught == null && System.nanoTime() < deadline) {
            Thread.yield();
        }
        check(leaf.caught == boom, "group handler throwable identity");
        check(leaf.caughtThread == t, "group handler thread identity");
        t.join(10_000);

        // Per-thread handler takes precedence over the group's handler.
        final boolean[] perThreadWon = {false};
        final CountDownLatch done = new CountDownLatch(1);
        final RuntimeException prec = new RuntimeException("uc-prec");
        Thread t2 = new Thread(leaf, () -> {
            throw prec;
        }, "uc-prec-worker");
        t2.setDaemon(true);
        t2.setUncaughtExceptionHandler((th, ex) -> {
            perThreadWon[0] = (ex == prec);
            done.countDown();
        });
        t2.start();
        check(done.await(10, TimeUnit.SECONDS), "per-thread handler won");
        t2.join(10_000);
        check(perThreadWon[0], "per-thread handler received the same instance");

        // Default handler routing for a plain group (no override anywhere),
        // restored in finally.
        Thread.UncaughtExceptionHandler savedDefault = Thread.getDefaultUncaughtExceptionHandler();
        try {
            final RecordingHandler dh = new RecordingHandler();
            final CountDownLatch d2 = new CountDownLatch(1);
            Thread.setDefaultUncaughtExceptionHandler((th, ex) -> {
                dh.uncaughtException(th, ex);
                d2.countDown();
            });
            final RuntimeException boom2 = new RuntimeException("uc-default");
            // Anchor the worker in the real root group: the default handler is
            // consulted only when the group chain reaches the unmodified root
            // group. A plain subgroup would hit the harness's ThreadGroup
            // override (jtreg MainThreadGroup) before the default handler.
            ThreadGroup root = Thread.currentThread().getThreadGroup();
            while (root.getParent() != null) {
                root = root.getParent();
            }
            Thread t3 = new Thread(root, () -> {
                throw boom2;
            }, "uc-default-worker");
            t3.setDaemon(true);
            t3.start();
            check(d2.await(10, TimeUnit.SECONDS), "default handler reached from plain group");
            check(dh.seenThrowable == boom2, "default handler throwable identity");
            check(dh.seenThread == t3, "default handler thread identity");
            t3.join(10_000);
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(savedDefault);
        }
        check(Thread.getDefaultUncaughtExceptionHandler() == null, "default handler restored");

        // Direct invocation records too.
        RecordingGroup rg = new RecordingGroup("uc-direct");
        RuntimeException direct = new RuntimeException("uc-direct-boom");
        rg.uncaughtException(t, direct);
        check(rg.caught == direct && rg.caughtThread == t, "direct uncaughtException call records");

        // Misc: subgroup toString, nested list(), checkAccess, null-name toString.
        ThreadGroup parent = new ThreadGroup("misc-parent");
        ThreadGroup child = new ThreadGroup(parent, "misc-child");
        check(child.toString().contains("name=misc-child"), "subgroup toString name");
        check(child.toString().endsWith("maxpri=10]"), "subgroup toString maxpri");
        child.list();
        passed++; // list() on a nested group must not throw
        child.checkAccess();
        passed++; // checkAccess without a SecurityManager must not throw
        check("java.lang.ThreadGroup[name=null,maxpri=10]".equals(new ThreadGroup((String) null).toString()),
              "toString of null-named group");
        ThreadGroup leafGroup = new ThreadGroup(child, "misc-leaf");
        ThreadGroup[] oneGroup = new ThreadGroup[1];
        int nLeaf = child.enumerate(oneGroup, false);
        check(nLeaf == 1 && oneGroup[0] == leafGroup, "subgroup enumerates its own subgroup by identity");
        check(leafGroup.enumerate(new ThreadGroup[1], true) == 0, "leaf group has no subgroups");
    }

    static class RecordingHandler implements Thread.UncaughtExceptionHandler {
        volatile Thread seenThread;
        volatile Throwable seenThrowable;
        volatile int calls;
        public void uncaughtException(Thread t, Throwable e) {
            seenThread = t;
            seenThrowable = e;
            calls++;
        }
    }

    static void testInterruptGroupSelective() throws Exception {
        ThreadGroup ga = new ThreadGroup("ig-a");
        ThreadGroup gb = new ThreadGroup("ig-b");
        final CountDownLatch caughtA = new CountDownLatch(1);
        final CountDownLatch releaseB = new CountDownLatch(1);
        final boolean[] bInterrupted = {false};
        Thread wa = new Thread(ga, () -> {
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException e) {
                caughtA.countDown();
            }
        }, "ig-a-worker");
        Thread wb = new Thread(gb, () -> {
            try {
                releaseB.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                bInterrupted[0] = true;
                Thread.currentThread().interrupt();
            }
        }, "ig-b-worker");
        wa.setDaemon(true);
        wb.setDaemon(true);
        wa.start();
        wb.start();
        ga.interrupt();
        check(caughtA.await(10, TimeUnit.SECONDS), "group interrupt hits member thread");
        releaseB.countDown();
        wa.join(10_000);
        wb.join(10_000);
        check(!bInterrupted[0], "group interrupt does not hit other groups' threads");
        check(!wa.isAlive() && !wb.isAlive(), "both workers terminated");
        // Interrupting an empty group never throws.
        new ThreadGroup("ig-empty").interrupt();
        passed++;
    }

    static void testInterruptRecurses() throws Exception {
        ThreadGroup outer = new ThreadGroup("ir-outer");
        ThreadGroup inner = new ThreadGroup(outer, "ir-inner");
        final CountDownLatch caught = new CountDownLatch(1);
        Thread nested = new Thread(inner, () -> {
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException e) {
                caught.countDown();
            }
        }, "ir-worker");
        nested.setDaemon(true);
        nested.start();
        outer.interrupt(); // must recurse into subgroups
        check(caught.await(10, TimeUnit.SECONDS), "group interrupt reaches nested subgroup threads");
        nested.join(10_000);
        check(!nested.isAlive(), "nested worker terminated");
        check(outer.activeCount() == 0, "no active threads remain");
    }

    static void testListCapture() throws Exception {
        ThreadGroup g = new ThreadGroup("list-capture");
        java.io.PrintStream savedOut = System.out;
        java.io.PrintStream savedErr = System.err;
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            System.setOut(new java.io.PrintStream(bos, true, "UTF-8"));
            System.setErr(new java.io.PrintStream(new java.io.ByteArrayOutputStream(), true, "UTF-8"));
            g.list();
            String out = new String(bos.toByteArray(), "UTF-8");
            check(out.contains("list-capture"), "list() prints the group name");
            check(out.contains("java.lang.ThreadGroup["), "list() prints ThreadGroup header");
        } finally {
            System.setOut(savedOut);
            System.setErr(savedErr);
        }
    }

    // JDK-27: destroy()/isDestroyed() are deprecated no-ops.
    static void testDestroyNoOp() throws Exception {
        ThreadGroup empty = new ThreadGroup("dn-empty");
        empty.destroy();
        check(!empty.isDestroyed(), "destroy() is a no-op: not destroyed");
        empty.destroy();
        check(!empty.isDestroyed(), "second destroy() still a no-op (no ITSE)");

        // Threads and subgroups can still join a "destroyed" group.
        Thread t = new Thread(empty, () -> {});
        check(t.getThreadGroup() == empty, "new Thread in destroyed group accepted (JDK-27)");
        ThreadGroup sub = new ThreadGroup(empty, "dn-sub");
        check(sub.getParent() == empty, "new ThreadGroup in destroyed group accepted (JDK-27)");
        // Non-empty group destroy does not throw either.
        final CountDownLatch release = new CountDownLatch(1);
        Thread w = new Thread(empty, () -> {
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        w.setDaemon(true);
        w.start();
        try {
            empty.destroy();
            check(!empty.isDestroyed(), "non-empty destroy() still a no-op (JDK-27)");
            check(awaitActiveCount(empty, 1) == 1, "activeCount still functional after destroy()");
            check(empty.enumerate(new Thread[1], false) == 1, "enumerate still functional after destroy()");
        } finally {
            release.countDown();
            w.join(10_000);
        }
        check(!w.isAlive(), "worker terminated");
    }

    // JDK-27 removed the terminal-deprecation trio plus allowThreadSuspension.
    static void testRemovedMembersAbsent() throws Exception {
        Class<?> c = Class.forName("java.lang.ThreadGroup");
        String[] gone = {"suspend", "resume", "stop", "allowThreadSuspension"};
        for (String name : gone) {
            boolean absent = false;
            try {
                c.getMethod(name);
            } catch (NoSuchMethodException expected) {
                absent = true;
            }
            check(absent, name + "() removed from ThreadGroup (JDK-27)");
        }
    }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ThreadGroup");
        check(!c.isInterface(), "ThreadGroup is a class");
        check(c.getSuperclass() == Object.class, "ThreadGroup extends Object");
        check(Thread.UncaughtExceptionHandler.class.isAssignableFrom(c),
              "ThreadGroup implements Thread.UncaughtExceptionHandler");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCurrentGroup() {
        ThreadGroup g = Thread.currentThread().getThreadGroup();
        check(g != null, "current thread group non-null");
        check(g.getName() != null && !g.getName().isEmpty(), "group name non-empty");
        check(g.getMaxPriority() == Thread.MAX_PRIORITY, "max priority is MAX_PRIORITY");
        check(g.activeCount() >= 1, "activeCount >= 1 (includes current thread)");
    }

    static void testNewGroup() {
        ThreadGroup parent = Thread.currentThread().getThreadGroup();
        ThreadGroup child = new ThreadGroup("custom-group");
        check("custom-group".equals(child.getName()), "new group name");
        check(child.getParent() == parent, "new group parent is current group");
        check(child.getMaxPriority() == Thread.MAX_PRIORITY, "new group inherits max priority");
        child.setMaxPriority(Thread.MAX_PRIORITY - 1);
        check(child.getMaxPriority() == Thread.MAX_PRIORITY - 1, "setMaxPriority");
        check(parent.parentOf(child), "parentOf(child) true");
        check(!child.parentOf(parent), "child.parentOf(parent) false");
    }

    static void testEnumerate() {
        ThreadGroup g = Thread.currentThread().getThreadGroup();
        Thread[] threads = new Thread[g.activeCount() + 1];
        int n = g.enumerate(threads, false);
        check(n >= 1, "enumerate returns >= 1 thread");
    }

    static void testEnumerateStartedThread() throws Exception {
        ThreadGroup group = new ThreadGroup("enum-group");
        final boolean[] ran = {false};
        Thread t = new Thread(group, () -> ran[0] = true, "enum-worker");
        t.setDaemon(true);
        t.start();
        Thread[] threads = new Thread[group.activeCount() + 4];
        int n = group.enumerate(threads, true);
        check(n >= 1, "enumerate includes started thread in group");
        boolean found = false;
        for (int i = 0; i < n; i++) if (threads[i] == t) found = true;
        check(found, "started worker is enumerated");
        t.join();
        check(ran[0], "worker ran");
    }

    static void testDaemonFlag() {
        ThreadGroup g = new ThreadGroup("daemon-group");
        check(!g.isDaemon(), "new group is not daemon");
        g.setDaemon(true);
        check(g.isDaemon(), "setDaemon(true)");
    }

    static void testActiveGroupCountAndList() {
        ThreadGroup parent = Thread.currentThread().getThreadGroup();
        ThreadGroup child = new ThreadGroup(parent, "list-group");
        check(child.getParent() == parent, "two-arg constructor parent");
        check(parent.activeGroupCount() >= 1, "activeGroupCount >= 1");
        child.list(); // must not throw
        passed++;
        check(child.toString() != null, "toString non-null");
    }

    static void testDestroyEmpty() {
        // Since the re-implementation of ThreadGroup (JDK 16+), destroy() is a
        // no-op and isDestroyed() always returns false (both deprecated for removal).
        ThreadGroup g = new ThreadGroup("destroy-me");
        check(!g.isDestroyed(), "isDestroyed always false");
        g.destroy(); // no-op, must not throw
        check(!g.isDestroyed(), "destroy() is a no-op: still not destroyed");
        check(g.getName() != null, "group still usable after destroy()");
    }

    // ---- Part 2b: black-box for checkAccess/interrupt/uncaughtException ----
    static void testBlackBox() throws Exception {
        // checkAccess is a no-op when no SecurityManager is installed.
        new ThreadGroup("bb-tg").checkAccess();
        passed++;

        // interrupt() interrupts every thread in the group.
        ThreadGroup ig = new ThreadGroup("interrupt-group");
        final boolean[] interrupted = {false};
        Thread t = new Thread(ig, () -> {
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException e) {
                interrupted[0] = true;
            }
        });
        t.setDaemon(true);
        t.start();
        for (int i = 0; i < 10_000 && t.getState() != Thread.State.TIMED_WAITING; i++) {
            Thread.yield();
        }
        ig.interrupt();
        t.join();
        check(interrupted[0], "ThreadGroup.interrupt() interrupts member threads");

        // uncaughtException is the JVM hook invoked when a member thread dies
        // with an uncaught exception; a recording subclass observes it.
        RecordingGroup rg = new RecordingGroup("uncaught-group");
        Thread t2 = new Thread(rg, () -> { throw new RuntimeException("boom"); });
        t2.setDaemon(true);
        t2.start();
        t2.join();
        check(rg.caught instanceof RuntimeException, "group.uncaughtException invoked on uncaught exception");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCurrentGroup();
        testNewGroup();
        testEnumerate();
        testEnumerateStartedThread();
        testDaemonFlag();
        testActiveGroupCountAndList();
        testDestroyEmpty();
        testBlackBox();
        // ---- 契约深化（2026-11）----
        testNestedHierarchy();
        testMaxPriorityContract();
        testGroupMaxVsThreadPriority();
        testDaemonRoundtrip();
        testActiveCountsPrecise();
        testActiveCountRecursive();
        testEnumeratePrecise();
        testToStringExact();
        testEqualsHash();
        testUncaughtRoutingDeep();
        testInterruptGroupSelective();
        testInterruptRecurses();
        testListCapture();
        testDestroyNoOp();
        testRemovedMembersAbsent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
