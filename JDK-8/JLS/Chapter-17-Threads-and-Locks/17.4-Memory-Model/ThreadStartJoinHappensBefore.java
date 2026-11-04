/*
 * @test
 * @summary JLS 17.4.5 (Java SE 8): Thread.start() happens-before the first action
 *          in the started thread, and the final action in a thread happens-before
 *          a successful return from Thread.join().
 * @run main ThreadStartJoinHappensBefore
 */

public class ThreadStartJoinHappensBefore {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int data = 0;

    public static void main(String[] args) throws Exception {
        final int[] seen = {-1};

        data = 42;
        Thread t = new Thread(() -> { seen[0] = data; });
        t.start();
        t.join(5000);

        check(!t.isAlive(), "thread terminated");
        check(seen[0] == 42, "start() happens-before thread body; join() after final action");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
