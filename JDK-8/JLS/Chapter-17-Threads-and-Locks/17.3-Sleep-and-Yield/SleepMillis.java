/*
 * @test
 * @summary JLS 17.3 (Java SE 8): Thread.sleep suspends the current thread for at
 *          least the requested duration.
 * @run main SleepMillis
 */

public class SleepMillis {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        long start = System.nanoTime();
        Thread.sleep(300);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        check(elapsedMillis >= 300, "slept at least the requested 300ms (slept " + elapsedMillis + "ms)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
