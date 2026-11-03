/*
 * @test
 * @summary java.lang.ThreadGroup conformance: white-box structure + black-box behavior.
 * @run main ThreadGroupTest
 */

public class ThreadGroupTest {
    static int passed;

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCurrentGroup();
        testNewGroup();
        testEnumerate();
        testEnumerateStartedThread();
        testDaemonFlag();
        testActiveGroupCountAndList();
        testDestroyEmpty();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
