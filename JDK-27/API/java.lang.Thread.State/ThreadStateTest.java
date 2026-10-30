/*
 * @test
 * @summary java.lang.Thread.State enum conformance: 6 constants, valueOf, and getState for a
 *          new thread and the current thread.
 * @run main ThreadStateTest
 */

public class ThreadStateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThreadState() {
        check(Thread.State.values().length == 6, "6 states");
        check(Thread.State.valueOf("RUNNABLE") == Thread.State.RUNNABLE, "valueOf");
        Thread t = new Thread(() -> {});
        check(t.getState() == Thread.State.NEW, "new thread state NEW");
        check(Thread.currentThread().getState() == Thread.State.RUNNABLE,
              "current thread RUNNABLE");
    }

    public static void main(String[] args) {
        testThreadState();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
