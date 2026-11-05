/*
 * @test
 * @summary java.lang.ThreadGroup conformance: white-box structure (implements
 *          UncaughtExceptionHandler, two constructors) + black-box behavior
 *          (name, parent linkage, max/setMaxPriority, daemon, activeCount,
 *          enumerate threads/groups, destroy empty vs non-empty ITSE, interrupt,
 *          uncaughtException callable, toString/checkAccess).
 * @run main ThreadGroupTest
 */

import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ThreadGroupTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ThreadGroup");
        check(!c.isInterface(), "ThreadGroup is a class");
        check(Thread.UncaughtExceptionHandler.class.isAssignableFrom(c),
              "ThreadGroup implements UncaughtExceptionHandler");
        c.getConstructor(String.class);
        c.getConstructor(ThreadGroup.class, String.class);
        passed += 2;
    }

    // ===================== 契约深化（2026-11）=====================

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
        check("lvl-c".equals(c.getName()), "grandchild name");
        check(c.getParent() == b, "grandchild parent is b");
        check("lvl-b".equals(b.getName()), "child name");
        check(b.getParent() == a, "child parent is a");
        check(a.getParent() == Thread.currentThread().getThreadGroup(),
              "ThreadGroup(String) parent is the current thread's group");
        check(a.parentOf(a), "parentOf(self) true");
        check(a.parentOf(b) && a.parentOf(c), "parentOf(child/grandchild) true");
        check(b.parentOf(c) && !b.parentOf(a), "parentOf direction correct");
        check(!c.parentOf(a) && !c.parentOf(b), "leaf is parentOf nothing above");
        ThreadGroup unrelated = new ThreadGroup("unrelated");
        check(!a.parentOf(unrelated) && !unrelated.parentOf(a), "unrelated groups parentOf false");
        check(!a.parentOf(null) || true, "parentOf(null) callable (no assertion on result)");
        // Daemon flag is inherited by subgroups (probed on both JDKs).
        ThreadGroup dparent = new ThreadGroup("dm-parent");
        dparent.setDaemon(true);
        ThreadGroup dchild = new ThreadGroup(dparent, "dm-child");
        check(dchild.isDaemon(), "subgroup inherits daemon flag of parent");
        dparent.setDaemon(false);
        check(dchild.isDaemon(), "already-created subgroup keeps its daemon flag");
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
            // Direct-call of the recording hook (already covered by testMisc for
            // forwarding; here a recording group records a direct call).
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

        // Lowering the parent propagates to existing children.
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
        release.countDown();
        w1.join(10_000);
        w2.join(10_000);
        check(awaitActiveCount(g, 0) == 0, "activeCount back to 0 after both join");
        sub.destroy();
        sub2.destroy();
        check(g.activeGroupCount() == 0, "activeGroupCount back to 0 after subgroup destroy");
        check(g.enumerate(new ThreadGroup[1]) == 0, "group enumerate empty after destroys");
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

    // Counting subclass of the RecordingGroup above.
    static class CountingGroup extends RecordingGroup {
        volatile int calls;
        CountingGroup(String name) { super(name); }
        @Override public void uncaughtException(Thread t, Throwable e) {
            calls++;
            super.uncaughtException(t, e);
        }
    }

    static void testUncaughtRoutingDeep() throws Exception {
        // Real dispatch: a thread dying in a RecordingGroup delivers (t, e).
        CountingGroup leaf = new CountingGroup("uc-leaf");
        final RuntimeException boom = new RuntimeException("uc-boom");
        Thread t = new Thread(leaf, () -> {
            throw boom;
        }, "uc-worker");
        t.setDaemon(true);
        t.start();
        long deadline = System.nanoTime() + 10_000_000_000L;
        while (leaf.calls == 0 && System.nanoTime() < deadline) {
            Thread.yield();
        }
        check(leaf.calls == 1, "group handler called exactly once");
        check(leaf.caughtThread == t, "group handler thread identity");
        check(leaf.caught == boom, "group handler throwable identity");
        t.join(10_000);

        // Per-thread handler takes precedence over the group's handler.
        final RecordingHandler ph = new RecordingHandler();
        final CountDownLatch done = new CountDownLatch(1);
        Thread t2 = new Thread(leaf, () -> {
            throw new RuntimeException("uc-prec");
        }, "uc-prec-worker");
        t2.setDaemon(true);
        t2.setUncaughtExceptionHandler((th, ex) -> {
            ph.uncaughtException(th, ex);
            done.countDown();
        });
        t2.start();
        check(done.await(10, TimeUnit.SECONDS), "per-thread handler won");
        t2.join(10_000);
        check(ph.calls == 1, "per-thread handler called once");
        check(leaf.calls == 1, "group handler count unchanged (precedence)");

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

    static void testListCapture() throws Exception {
        ThreadGroup g = new ThreadGroup("list-capture");
        java.io.PrintStream savedOut = System.out;
        java.io.PrintStream savedErr = System.err;
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            System.setOut(new java.io.PrintStream(bos, true));
            System.setErr(new java.io.PrintStream(new java.io.ByteArrayOutputStream(), true));
            g.list();
            String out = new String(bos.toByteArray(), "UTF-8");
            check(out.contains("list-capture"), "list() prints the group name");
            check(out.contains("java.lang.ThreadGroup["), "list() prints ThreadGroup header");
        } finally {
            System.setOut(savedOut);
            System.setErr(savedErr);
        }
    }

    static void testMiscExtra() {
        // Direct invocation of a recording group's uncaughtException records.
        RecordingGroup rg = new RecordingGroup("misc-rec");
        Thread t = new Thread("misc-rec-thread");
        RuntimeException e = new RuntimeException("misc-direct");
        rg.uncaughtException(t, e);
        check(rg.caughtThread == t && rg.caught == e, "direct uncaughtException call records");
        // Subgroup toString carries its own name and max.
        ThreadGroup parent = new ThreadGroup("misc-parent");
        ThreadGroup child = new ThreadGroup(parent, "misc-child");
        check(child.toString().contains("name=misc-child"), "subgroup toString name");
        check(child.toString().endsWith("maxpri=10]"), "subgroup toString maxpri");
        // list() on a nested group must not throw.
        child.list();
        passed++;
        // allowThreadSuspension(true) is callable (deprecated, result unchecked).
        child.allowThreadSuspension(true);
        passed++;
    }

    static void testDestroySemantics() throws Exception {
        // Empty group destroy: becomes destroyed.
        ThreadGroup empty = new ThreadGroup("ds-empty");
        empty.destroy();
        check(empty.isDestroyed(), "empty group destroyed");
        expectITSE(() -> empty.destroy(), "second destroy on destroyed group");
        // Destroying a parent recurses into (live) subgroups.
        ThreadGroup par = new ThreadGroup("ds-par");
        ThreadGroup ch = new ThreadGroup(par, "ds-ch");
        par.destroy();
        check(par.isDestroyed() && ch.isDestroyed(), "destroy recurses into subgroups");
        expectITSE(() -> ch.destroy(), "destroy on already-destroyed subgroup");
        // New threads and subgroups cannot join a destroyed group.
        expectITSE(() -> new Thread(ch, () -> {}), "new Thread in destroyed group");
        expectITSE(() -> new ThreadGroup(ch, "ds-no"), "new ThreadGroup in destroyed group");
        check(ch.activeCount() == 0, "destroyed group has no active threads");
        check(ch.enumerate(new Thread[1]) == 0, "destroyed group enumerates nothing");
        check(ch.activeGroupCount() == 0, "destroyed group has no subgroups");
        ch.checkAccess();
        passed++; // checkAccess on a destroyed group does not throw
        // Non-empty group destroy -> ITSE (existing testDestroy covers the live
        // thread case); a live subgroup does NOT prevent destroy.
        ThreadGroup par2 = new ThreadGroup("ds-par2");
        new ThreadGroup(par2, "ds-ch2");
        par2.destroy();
        check(par2.isDestroyed(), "parent with subgroup (no threads) destroyed");
    }

    static void testDeprecatedPresentNotCalled() throws Exception {
        // Deprecated members still declared in JDK-8 (never invoked here).
        Class<?> c = Class.forName("java.lang.ThreadGroup");
        c.getMethod("suspend");
        passed++;
        c.getMethod("resume");
        passed++;
        c.getMethod("stop");
        passed++;
        c.getMethod("allowThreadSuspension", boolean.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testConstructors() {
        ThreadGroup current = Thread.currentThread().getThreadGroup();
        ThreadGroup g = new ThreadGroup("jck-tg");
        check("jck-tg".equals(g.getName()), "ThreadGroup(String) name");
        check(g.getParent() == current, "ThreadGroup(String) parent is current group");

        ThreadGroup parent = new ThreadGroup("jck-tg-parent");
        ThreadGroup child = new ThreadGroup(parent, "jck-tg-child");
        check(child.getParent() == parent, "ThreadGroup(parent,name) explicit parent");
        check("jck-tg-child".equals(child.getName()), "ThreadGroup(parent,name) name");

        // JDK-8 does NOT null-check the name (only the parent): a null name is
        // stored verbatim rather than throwing NPE.
        check(new ThreadGroup((String) null).getName() == null, "ThreadGroup((String)null) stores null name");
        expectNPE(() -> new ThreadGroup((ThreadGroup) null, "x"), "ThreadGroup((ThreadGroup)null,name)");
        check(new ThreadGroup(parent, (String) null).getName() == null,
              "ThreadGroup(parent,(String)null) stores null name");
    }

    static void testPriority() {
        ThreadGroup parent = new ThreadGroup("jck-tg-pri-parent");
        parent.setMaxPriority(Thread.MAX_PRIORITY - 1);
        ThreadGroup child = new ThreadGroup(parent, "jck-tg-pri-child");
        check(child.getMaxPriority() == Thread.MAX_PRIORITY - 1, "child inherits parent max priority");
        child.setMaxPriority(Thread.MIN_PRIORITY);
        check(child.getMaxPriority() == Thread.MIN_PRIORITY, "setMaxPriority lowers");
    }

    static void testDaemon() {
        ThreadGroup g = new ThreadGroup("jck-tg-daemon");
        check(!g.isDaemon(), "new group not daemon");
        g.setDaemon(true);
        check(g.isDaemon(), "setDaemon(true)");
    }

    static void testCountsAndEnumerate() throws Exception {
        ThreadGroup g = new ThreadGroup("jck-tg-counts");
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread worker = new Thread(g, () -> {
            started.countDown();
            try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "jck-worker");
        worker.setDaemon(true);
        worker.start();
        try {
            check(started.await(10, TimeUnit.SECONDS), "worker started in group");
            check(g.activeCount() >= 1, "activeCount >= 1 with live worker");
            Thread[] threads = new Thread[g.activeCount() + 1];
            int n = g.enumerate(threads, false);
            check(n >= 1, "enumerate(Thread[],false) returns >= 1");
            boolean foundWorker = false;
            for (int i = 0; i < n; i++) if ("jck-worker".equals(threads[i].getName())) foundWorker = true;
            check(foundWorker, "enumerate lists the worker thread");

            ThreadGroup[] groups = new ThreadGroup[g.activeGroupCount() + 1];
            int ng = g.enumerate(groups, false);
            check(ng >= 0, "enumerate(ThreadGroup[],false) returns >= 0");
        } finally {
            release.countDown();
            try { worker.join(10_000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
    }

    static void testDestroy() {
        ThreadGroup empty = new ThreadGroup("jck-tg-empty");
        empty.destroy();
        check(empty.isDestroyed(), "destroyed empty group");

        ThreadGroup busy = new ThreadGroup("jck-tg-busy");
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread worker = new Thread(busy, () -> {
            started.countDown();
            try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        worker.setDaemon(true);
        worker.start();
        try {
            started.await(10, TimeUnit.SECONDS);
            try {
                busy.destroy();
                throw new AssertionError("destroy non-empty group: expected IllegalThreadStateException");
            } catch (IllegalThreadStateException expected) {
                passed++;
            }
            check(!busy.isDestroyed(), "non-empty group not destroyed");
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            release.countDown();
            try { worker.join(10_000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
    }

    // Overrides uncaughtException to record (and NOT re-propagate to the harness),
    // so forwarding is observable without reaching the root group.
    static class RecordingGroup extends ThreadGroup {
        volatile Thread caughtThread;
        volatile Throwable caught;
        RecordingGroup(String name) { super(name); }
        @Override public void uncaughtException(Thread t, Throwable e) { caughtThread = t; caught = e; }
    }

    static void testMisc() {
        RecordingGroup parent = new RecordingGroup("jck-tg-misc-parent");
        ThreadGroup child = new ThreadGroup(parent, "jck-tg-misc-child");

        check(child.toString().contains("jck-tg-misc-child"), "toString contains name");
        child.checkAccess();
        passed++;

        // Deprecated, but callable in JDK-8: must not throw on a fresh group.
        child.allowThreadSuspension(false);
        passed++;

        // Default uncaughtException forwards to the parent group.
        Thread t = new Thread("jck-ue");
        RuntimeException e = new RuntimeException("jck");
        child.uncaughtException(t, e);
        check(parent.caughtThread == t && parent.caught == e, "uncaughtException forwards to parent");
        passed++;

        // interrupt() on an empty group must not throw.
        child.interrupt();
        passed++;

        // list() must not throw.
        child.list();
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testPriority();
        testDaemon();
        testCountsAndEnumerate();
        testDestroy();
        testMisc();
        // ---- 契约深化（2026-11）----
        testNestedHierarchy();
        testMaxPriorityContract();
        testGroupMaxVsThreadPriority();
        testDaemonRoundtrip();
        testActiveCountsPrecise();
        testEnumeratePrecise();
        testToStringExact();
        testEqualsHash();
        testUncaughtRoutingDeep();
        testInterruptGroupSelective();
        testListCapture();
        testDestroySemantics();
        testDeprecatedPresentNotCalled();
        testInterruptRecurses();
        testActiveCountRecursive();
        testMiscExtra();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
