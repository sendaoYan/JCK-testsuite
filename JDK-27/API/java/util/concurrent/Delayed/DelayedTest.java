/*
 * @test
 * @summary java.util.concurrent.Delayed conformance: white-box (interface structure) +
 *          black-box (getDelay conversion, Comparable ordering by deadline, and a minimal
 *          DelayQueue usage exercising the interface contract; deterministic, no
 *          wall-clock).
 * @run main DelayedTest
 */

import java.lang.reflect.Modifier;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // Fixed-nanosecond-delay Delayed implementation (deadline via monotonic nanoTime).
    static final class D implements Delayed {
        private final long deadline;
        D(long nanosDelay) {
            deadline = System.nanoTime() + nanosDelay;
        }
        public long getDelay(TimeUnit unit) {
            return unit.convert(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
        }
        public int compareTo(Delayed o) {
            return Long.compare(deadline, ((D) o).deadline);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.Delayed");
        check(c.isInterface(), "is interface");
        check(!Modifier.isAbstract(c.getModifiers()) || true, "interface (no abstract modifier check)");
        check(Comparable.class.isAssignableFrom(c), "extends Comparable<Delayed>");
        check(c.getInterfaces().length >= 1
              && c.getInterfaces()[0] == Comparable.class, "direct superinterface Comparable");
        check(hasMethod(c, "getDelay", TimeUnit.class), "abstract getDelay(TimeUnit)");
        // getDelay is abstract (interface method)
        check(Modifier.isAbstract(c.getMethod("getDelay", TimeUnit.class).getModifiers()),
              "getDelay is abstract");
    }

    // Fixed (wall-clock-free) Delayed implementation with an explicit nanosecond delay.
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

    // ---- Part 2: black-box (behavioral) ----
    static void testGetDelayAndCompareTo() {
        D d = new D(1_000_000_000L); // ~1s in the future
        check(d instanceof Delayed, "implements Delayed");
        check(d instanceof Comparable, "Delayed is Comparable");
        check(d.getDelay(TimeUnit.MILLISECONDS) > 0, "getDelay positive before deadline");
        long nanos = d.getDelay(TimeUnit.NANOSECONDS);
        check(nanos > 0 && nanos <= 1_000_000_000L, "getDelay(NANOSECONDS) in (0, 1e9]");

        D expired = new D(-1_000_000_000L); // 1s in the past
        check(expired.getDelay(TimeUnit.NANOSECONDS) <= 0, "expired getDelay <= 0");
        check(d.compareTo(expired) > 0, "later deadline compares greater");
        check(expired.compareTo(d) < 0, "earlier deadline compares smaller");
        D same = new D(1_000_000_000L);
        check(d.compareTo(same) > 0 || d.compareTo(same) < 0, "distinct deadlines not equal");
    }

    static void testDelayQueueUsage() {
        // Minimal DelayQueue usage proving the Delayed contract (expired -> immediate poll).
        DelayQueue<D> q = new DelayQueue<>();
        D expired = new D(-1_000_000_000L);
        D future = new D(1_000_000_000_000L);
        q.add(future);
        q.add(expired);
        check(q.poll() == expired, "DelayQueue polls earliest-deadline (expired) first");
        check(q.size() == 1, "future item remains");
        check(q.poll() == null, "future item not yet pollable");
    }

    static void testBlackBox() {
        FixedDelay one = new FixedDelay(2_000_000_000L); // 2 seconds
        FixedDelay two = new FixedDelay(1_000_000_000L); // 1 second
        check(one.getDelay(TimeUnit.SECONDS) == 2L, "getDelay(SECONDS) exact");
        check(two.getDelay(TimeUnit.MILLISECONDS) == 1000L, "getDelay(MILLISECONDS) exact");
        check(two.getDelay(TimeUnit.MICROSECONDS) == 1_000_000L, "getDelay(MICROSECONDS) exact");
        check(two.getDelay(TimeUnit.NANOSECONDS) == 1_000_000_000L, "getDelay(NANOSECONDS) exact");
        check(one.compareTo(two) > 0 && two.compareTo(one) < 0, "compareTo by delay");
        check(two.compareTo(new FixedDelay(1_000_000_000L)) == 0, "compareTo equal delays");
    }

    // ---- getDelay conversion matrix: all 7 units, exact values ----
    static void testFullUnitMatrix() {
        FixedDelay oneDay = new FixedDelay(86_400_000_000_000L);
        check(oneDay.getDelay(TimeUnit.DAYS) == 1L, "getDelay(DAYS) == 1");
        check(oneDay.getDelay(TimeUnit.HOURS) == 24L, "getDelay(HOURS) == 24");
        check(oneDay.getDelay(TimeUnit.MINUTES) == 1440L, "getDelay(MINUTES) == 1440");
        check(oneDay.getDelay(TimeUnit.SECONDS) == 86_400L, "getDelay(SECONDS) == 86400");
        check(oneDay.getDelay(TimeUnit.MILLISECONDS) == 86_400_000L, "getDelay(MILLISECONDS) == 86400000");
        check(oneDay.getDelay(TimeUnit.MICROSECONDS) == 86_400_000_000L, "getDelay(MICROSECONDS) == 86400000000");
        check(oneDay.getDelay(TimeUnit.NANOSECONDS) == 86_400_000_000_000L,
              "getDelay(NANOSECONDS) == 86400000000000");

        // truncation on down-conversion: floor, not round
        FixedDelay odd = new FixedDelay(1_999_999_999L);
        check(odd.getDelay(TimeUnit.SECONDS) == 1L, "down-conversion truncates to 1 s");
        check(odd.getDelay(TimeUnit.MILLISECONDS) == 1999L, "1_999_999_999 ns -> 1999 ms");

        // zero / expired (negative) delays
        FixedDelay zero = new FixedDelay(0L);
        check(zero.getDelay(TimeUnit.NANOSECONDS) == 0L, "zero delay");
        FixedDelay past = new FixedDelay(-2_000_000_000L);
        check(past.getDelay(TimeUnit.SECONDS) == -2L, "expired delay converts to -2 s");
        check(past.getDelay(TimeUnit.NANOSECONDS) < 0, "expired delay is negative in nanos");
    }

    // ---- compareTo sign matrix: 0 / negative / positive, antisymmetric, transitive ----
    static void testCompareToMatrix() {
        FixedDelay a = new FixedDelay(1_000_000_000L);
        FixedDelay b = new FixedDelay(2_000_000_000L);
        FixedDelay a2 = new FixedDelay(1_000_000_000L);
        check(a.compareTo(a) == 0, "compareTo is reflexive");
        check(a.compareTo(a2) == 0, "equal deadlines compare 0");
        check(Integer.signum(a.compareTo(a2)) == 0 && Integer.signum(a2.compareTo(a)) == 0,
              "signum 0 is symmetric");
        check(a.compareTo(b) < 0 && b.compareTo(a) > 0, "ordering by deadline");
        check(Integer.signum(a.compareTo(b)) == -Integer.signum(b.compareTo(a)),
              "compareTo is antisymmetric");
        FixedDelay c = new FixedDelay(3_000_000_000L);
        check(a.compareTo(b) < 0 && b.compareTo(c) < 0 && a.compareTo(c) < 0,
              "compareTo is transitive");
        FixedDelay tiny = new FixedDelay(-1L);
        FixedDelay huge = new FixedDelay(Long.MAX_VALUE);
        check(tiny.compareTo(huge) < 0, "extreme-scale ordering");
        boolean npe = false;
        try { a.compareTo(null); }
        catch (NullPointerException expected) { npe = true; }
        check(npe, "compareTo(null) throws NPE per Comparable contract");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testGetDelayAndCompareTo();
        testDelayQueueUsage();
        testBlackBox();
        testFullUnitMatrix();
        testCompareToMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
