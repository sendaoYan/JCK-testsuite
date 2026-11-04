/*
 * @test
 * @summary java.util.concurrent.Delayed API conformance: getDelay and Comparable.
 * @run main DelayedTest
 */

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class D implements Delayed {
        private final long deadline;
        D(long ms) {
            deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(ms);
        }
        public long getDelay(TimeUnit unit) {
            return unit.convert(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
        }
        public int compareTo(Delayed o) {
            return Long.compare(deadline, ((D) o).deadline);
        }
    }

    static void testDelayed() {
        D d = new D(1000);
        check(d instanceof Delayed, "implements Delayed");
        check(d.getDelay(TimeUnit.MILLISECONDS) > 0, "getDelay positive before deadline");
        check(d.compareTo(new D(1)) > 0, "compareTo later deadline");
    }

    public static void main(String[] args) {
        testDelayed();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
