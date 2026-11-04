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

    // Fixed (wall-clock-free) implementation returning a constant nanosecond delay.
    static final class FixedDelay implements Delayed {
        private final long delayNanos;
        FixedDelay(long delayNanos) { this.delayNanos = delayNanos; }
        public long getDelay(TimeUnit unit) {
            return unit.convert(delayNanos, TimeUnit.NANOSECONDS);
        }
        public int compareTo(Delayed o) {
            return Long.compare(delayNanos, ((FixedDelay) o).delayNanos);
        }
    }

    static void testDelayed() {
        D d = new D(1000);
        check(d instanceof Delayed, "implements Delayed");
        check(d.getDelay(TimeUnit.MILLISECONDS) > 0, "getDelay positive before deadline");
        check(d.compareTo(new D(1)) > 0, "compareTo later deadline");
    }

    // ---- white-box: interface shape ----
    static void testWhiteBoxStructure() throws Exception {
        Class<Delayed> c = Delayed.class;
        check(c.isInterface(), "Delayed is an interface");
        check(c.getInterfaces().length == 1
                  && c.getInterfaces()[0] == java.lang.Comparable.class,
              "direct superinterface is Comparable");
        check(java.lang.Comparable.class.isAssignableFrom(c), "Delayed is Comparable");
        check(c.getDeclaredMethods().length == 1, "exactly one abstract method getDelay");
        check(c.getMethod("getDelay", TimeUnit.class).getReturnType() == long.class,
              "getDelay returns long");
        check(c.getMethod("compareTo", Object.class).getDeclaringClass() == java.lang.Comparable.class,
              "compareTo is inherited from Comparable, not redeclared");
    }

    // ---- getDelay unit conversion matrix (exact, wall-clock-free) ----
    static void testGetDelayConversion() {
        // one day = 86_400_000_000_000 ns
        FixedDelay oneDay = new FixedDelay(86_400_000_000_000L);
        check(oneDay.getDelay(TimeUnit.DAYS) == 1L, "getDelay(DAYS) == 1");
        check(oneDay.getDelay(TimeUnit.HOURS) == 24L, "getDelay(HOURS) == 24");
        check(oneDay.getDelay(TimeUnit.MINUTES) == 1440L, "getDelay(MINUTES) == 1440");
        check(oneDay.getDelay(TimeUnit.SECONDS) == 86_400L, "getDelay(SECONDS) == 86400");
        check(oneDay.getDelay(TimeUnit.MILLISECONDS) == 86_400_000L, "getDelay(MILLISECONDS) == 86400000");
        check(oneDay.getDelay(TimeUnit.MICROSECONDS) == 86_400_000_000L, "getDelay(MICROSECONDS) == 86400000000");
        check(oneDay.getDelay(TimeUnit.NANOSECONDS) == 86_400_000_000_000L,
              "getDelay(NANOSECONDS) == 86400000000000");

        // truncation on down-conversion: 1_999_999_999 ns -> 1 s (floor, not round)
        FixedDelay odd = new FixedDelay(1_999_999_999L);
        check(odd.getDelay(TimeUnit.SECONDS) == 1L, "down-conversion truncates to 1 s");
        check(odd.getDelay(TimeUnit.MILLISECONDS) == 1999L, "1_999_999_999 ns -> 1999 ms");

        // zero and expired (negative) delays
        FixedDelay zero = new FixedDelay(0L);
        check(zero.getDelay(TimeUnit.NANOSECONDS) == 0L, "zero delay");
        check(zero.getDelay(TimeUnit.SECONDS) == 0L, "zero delay in seconds");
        FixedDelay past = new FixedDelay(-2_000_000_000L);
        check(past.getDelay(TimeUnit.SECONDS) == -2L, "expired (negative) delay converts to -2 s");
        check(past.getDelay(TimeUnit.NANOSECONDS) < 0, "expired delay is negative in nanos");
    }

    // ---- compareTo sign matrix (0 / negative / positive, antisymmetric) ----
    static void testCompareToMatrix() {
        FixedDelay a = new FixedDelay(1_000_000_000L);
        FixedDelay b = new FixedDelay(2_000_000_000L);
        FixedDelay a2 = new FixedDelay(1_000_000_000L);
        check(a.compareTo(b) < 0, "earlier deadline < later deadline");
        check(b.compareTo(a) > 0, "later deadline > earlier deadline");
        check(a.compareTo(a2) == 0, "equal deadlines compare 0");
        check(a.compareTo(a) == 0, "compareTo is reflexive");
        // antisymmetry: sign(x,y) == -sign(y,x)
        check(Integer.signum(a.compareTo(b)) == -Integer.signum(b.compareTo(a)),
              "compareTo is antisymmetric");
        check(Integer.signum(a.compareTo(a2)) == 0
                  && Integer.signum(a2.compareTo(a)) == 0,
              "signum 0 is symmetric");
        // transitivity across three values
        FixedDelay c = new FixedDelay(3_000_000_000L);
        check(a.compareTo(b) < 0 && b.compareTo(c) < 0 && a.compareTo(c) < 0,
              "compareTo is transitive");
        // cross-implementation compare (D uses nanoTime deadlines; compare via a wrapper)
        FixedDelay tiny = new FixedDelay(-1L);
        FixedDelay huge = new FixedDelay(Long.MAX_VALUE);
        check(tiny.compareTo(huge) < 0, "Long.MIN..MAX scale ordering");
        boolean npe = false;
        try { a.compareTo(null); }
        catch (NullPointerException expected) { npe = true; }
        check(npe, "compareTo(null) throws NPE per Comparable contract");
    }

    public static void main(String[] args) throws Exception {
        testDelayed();
        testWhiteBoxStructure();
        testGetDelayConversion();
        testCompareToMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
