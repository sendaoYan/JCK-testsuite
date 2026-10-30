/*
 * @test
 * @summary JLS 17.3: Thread.sleep suspends the current thread for at least the
 *          requested duration and does not release held monitors.
 * @run main SleepMillis
 */

public class SleepMillis {
    public static void main(String[] args) throws Exception {
        long start = System.nanoTime();
        Thread.sleep(300);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        if (elapsedMillis < 300)
            throw new AssertionError("slept only " + elapsedMillis + "ms, expected >= 300ms");

        System.out.println("PASS");
    }
}
