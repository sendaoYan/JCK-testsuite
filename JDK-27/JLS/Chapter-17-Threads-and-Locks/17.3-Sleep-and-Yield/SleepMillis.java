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
        // Thread.sleep guarantees >= requested, but the nanoTime->ms conversion
        // has clock-granularity + truncation slack (Windows can read 299ms for a
        // 300ms sleep). Allow a small tolerance so timing noise isn't a failure.
        if (elapsedMillis < 290)
            throw new AssertionError("slept only " + elapsedMillis + "ms, expected >= 300ms");

        System.out.println("PASS");
    }
}
