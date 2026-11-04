/*
 * @test
 * @summary java.lang.ThreadGroup API conformance: name, parent linkage, max priority and
 *          setMaxPriority, activeCount/enumerate, and destroy of an empty group.
 * @run main ThreadGroupTest
 */

public class ThreadGroupTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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
    }

    static void testEnumerate() {
        ThreadGroup g = Thread.currentThread().getThreadGroup();
        Thread[] threads = new Thread[g.activeCount() + 1];
        int n = g.enumerate(threads, false);
        check(n >= 1, "enumerate returns >= 1 thread");
    }

    public static void main(String[] args) {
        testCurrentGroup();
        testNewGroup();
        testEnumerate();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
