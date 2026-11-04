/*
 * @test
 * @summary java.util.SplittableRandom API conformance (Java 8 surface): complete contract
 *          coverage of seeded determinism, nextInt/nextLong/nextDouble (bounded + origin/
 *          bound) IllegalArgumentException matrix (0/negative/NaN/origin>=bound), ranges,
 *          nextBoolean, split independence, the ints/longs/doubles stream factories
 *          (sizes + origin/bound IAE + determinism).
 * @run main SplittableRandomTest
 */

import java.util.SplittableRandom;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.DoubleStream;

public class SplittableRandomTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static void testDeterminism() {
        SplittableRandom r = new SplittableRandom(42);
        long a = r.nextLong();
        long b = r.nextLong();
        check(a != b, "nextLong produces variation");
        SplittableRandom r2 = new SplittableRandom(42);
        check(r2.nextLong() == a && r2.nextLong() == b, "same seed same sequence");
    }

    static void testBoundAndSplit() {
        SplittableRandom r = new SplittableRandom(7);
        int v = r.nextInt(10);
        check(v >= 0 && v < 10, "nextInt(bound) within [0, bound)");
        check(r.split() != null, "split returns a child");
    }

    static void testNextIntContracts() {
        SplittableRandom r = new SplittableRandom(11L);
        expectThrows(IllegalArgumentException.class, () -> r.nextInt(0), "nextInt(0) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.nextInt(-5), "nextInt(-5) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.nextInt(5, 5), "nextInt(o>=b) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.nextInt(6, 5), "nextInt(o>b) IAE");

        check(new SplittableRandom(1L).nextInt(1) == 0, "nextInt(1) == 0");

        SplittableRandom p = new SplittableRandom(99L);
        for (int i = 0; i < 2000; i++) {
            int x = p.nextInt(1024); // power of two
            check(x >= 0 && x < 1024, "nextInt(1024) power-of-two in range");
        }

        SplittableRandom o = new SplittableRandom(5L);
        for (int i = 0; i < 2000; i++) {
            int x = o.nextInt(-50, 50);
            check(x >= -50 && x < 50, "nextInt(origin,bound) in range");
        }

        // determinism of bounded forms.
        int[] a = new int[100];
        int[] b = new int[100];
        SplittableRandom ra = new SplittableRandom(7L);
        SplittableRandom rb = new SplittableRandom(7L);
        for (int i = 0; i < 100; i++) { a[i] = ra.nextInt(1000); b[i] = rb.nextInt(1000); }
        check(java.util.Arrays.equals(a, b), "nextInt(bound) deterministic");
    }

    static void testNextLongContracts() {
        SplittableRandom r = new SplittableRandom(13L);
        expectThrows(IllegalArgumentException.class, () -> r.nextLong(0), "nextLong(0) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.nextLong(-1), "nextLong(-1) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.nextLong(5L, 5L), "nextLong(o>=b) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.nextLong(6L, 5L), "nextLong(o>b) IAE");

        check(new SplittableRandom(1L).nextLong(1) == 0, "nextLong(1) == 0");

        SplittableRandom p = new SplittableRandom(17L);
        for (int i = 0; i < 2000; i++) {
            long x = p.nextLong(1024L);
            check(x >= 0L && x < 1024L, "nextLong(1024) in range");
        }
        SplittableRandom o = new SplittableRandom(19L);
        for (int i = 0; i < 2000; i++) {
            long x = o.nextLong(-100L, 100L);
            check(x >= -100L && x < 100L, "nextLong(origin,bound) in range");
        }
    }

    static void testNextDoubleContracts() {
        SplittableRandom r = new SplittableRandom(23L);
        // nextDouble() in [0,1).
        for (int i = 0; i < 5000; i++) {
            double d = r.nextDouble();
            check(d >= 0.0 && d < 1.0, "nextDouble in [0,1)");
        }

        // nextDouble(bound): bound <= 0 or NaN -> IAE; +Infinity is legal.
        SplittableRandom r2 = new SplittableRandom(1L);
        expectThrows(IllegalArgumentException.class, () -> r2.nextDouble(0.0), "nextDouble(0.0) IAE");
        expectThrows(IllegalArgumentException.class, () -> r2.nextDouble(-1.0), "nextDouble(-1.0) IAE");
        expectThrows(IllegalArgumentException.class, () -> r2.nextDouble(Double.NaN), "nextDouble(NaN) IAE");
        for (int i = 0; i < 1000; i++) {
            double d = new SplittableRandom(29L).nextDouble(3.5);
            check(d >= 0.0 && d < 3.5, "nextDouble(bound) in [0,bound)");
        }

        // nextDouble(origin, bound): !(origin < bound) -> IAE (covers NaN).
        SplittableRandom r3 = new SplittableRandom(1L);
        expectThrows(IllegalArgumentException.class, () -> r3.nextDouble(1.0, 1.0), "nextDouble(o>=b) IAE");
        expectThrows(IllegalArgumentException.class, () -> r3.nextDouble(Double.NaN, 1.0), "nextDouble(NaN,1) IAE");
        expectThrows(IllegalArgumentException.class, () -> r3.nextDouble(0.0, Double.NaN), "nextDouble(0,NaN) IAE");
        for (int i = 0; i < 1000; i++) {
            double d = new SplittableRandom(31L).nextDouble(-2.0, 3.0);
            check(d >= -2.0 && d < 3.0, "nextDouble(origin,bound) in range");
        }
    }

    static void testNextBoolean() {
        SplittableRandom r = new SplittableRandom(37L);
        boolean sawTrue = false, sawFalse = false;
        for (int i = 0; i < 1000; i++) {
            boolean v = r.nextBoolean();
            if (v) sawTrue = true; else sawFalse = true;
        }
        check(sawTrue && sawFalse, "nextBoolean produces both values");
    }

    static void testSplitIndependence() {
        SplittableRandom parent = new SplittableRandom(41L);
        SplittableRandom c1 = parent.split();
        SplittableRandom c2 = parent.split();
        check(c1 != null && c2 != null, "split returns non-null children");
        check(c1 != parent && c2 != parent, "split returns distinct objects");
        // children have distinct sequences.
        check(c1.nextLong() != c2.nextLong() || c1.nextInt() != c2.nextInt(),
              "split children have independent sequences");
    }

    static void testStreams() {
        check(new SplittableRandom(1L).ints(50).count() == 50, "ints(50).count() == 50");
        check(new SplittableRandom(1L).ints().limit(60).count() == 60, "ints().limit(60).count() == 60");
        IntStream is = new SplittableRandom(2L).ints(5000, 10, 20);
        check(is.allMatch(v -> v >= 10 && v < 20), "ints(origin,bound) range");

        check(new SplittableRandom(1L).longs(50).count() == 50, "longs(50).count() == 50");
        LongStream ls = new SplittableRandom(2L).longs(5000, 100L, 200L);
        check(ls.allMatch(v -> v >= 100L && v < 200L), "longs(origin,bound) range");

        check(new SplittableRandom(1L).doubles(50).count() == 50, "doubles(50).count() == 50");
        DoubleStream ds = new SplittableRandom(2L).doubles(5000, 0.5, 1.5);
        check(ds.allMatch(v -> v >= 0.5 && v < 1.5), "doubles(origin,bound) range");

        SplittableRandom r = new SplittableRandom(1L);
        expectThrows(IllegalArgumentException.class, () -> r.ints(-1L), "ints(-1) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.ints(10, 10, 10), "ints(o>=b) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.longs(-1L), "longs(-1) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.longs(10, 10L, 10L), "longs(o>=b) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.doubles(-1L), "doubles(-1) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.doubles(10, 1.0, 1.0), "doubles(o>=b) IAE");

        // determinism of streams.
        int[] ia = new SplittableRandom(42L).ints(100).toArray();
        int[] ib = new SplittableRandom(42L).ints(100).toArray();
        check(java.util.Arrays.equals(ia, ib), "ints deterministic");
        long[] la = new SplittableRandom(42L).longs(100).toArray();
        long[] lb = new SplittableRandom(42L).longs(100).toArray();
        check(java.util.Arrays.equals(la, lb), "longs deterministic");
        double[] da = new SplittableRandom(42L).doubles(100).toArray();
        double[] db = new SplittableRandom(42L).doubles(100).toArray();
        check(java.util.Arrays.equals(da, db), "doubles deterministic");
    }

    public static void main(String[] args) {
        testDeterminism();
        testBoundAndSplit();
        testNextIntContracts();
        testNextLongContracts();
        testNextDoubleContracts();
        testNextBoolean();
        testSplitIndependence();
        testStreams();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
