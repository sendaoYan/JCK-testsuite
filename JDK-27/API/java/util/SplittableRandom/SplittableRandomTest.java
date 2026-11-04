/*
 * @test
 * @summary java.util.SplittableRandom conformance: white-box structure plus black-box behavior
 *          of seeded determinism, nextLong/nextInt/nextInt(bound)/nextInt(origin,bound)/nextDouble
 *          ranges, deterministic split, IllegalArgument matrix (0/negative/NaN/origin>=bound),
 *          nextBoolean, split independence, ints/longs/doubles streams, and splits()/split(source)
 *          JDK-17+ factory methods.
 * @run main SplittableRandomTest
 */

import java.util.SplittableRandom;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.DoubleStream;

public class SplittableRandomTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.SplittableRandom");
        check(!c.isInterface(), "SplittableRandom is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "SplittableRandom is final");
        check(RandomGenerator.class.isAssignableFrom(c), "SplittableRandom implements RandomGenerator");
        check(RandomGenerator.SplittableGenerator.class.isAssignableFrom(c),
              "SplittableRandom implements SplittableGenerator");
        c.getMethod("split");
        c.getMethod("split", RandomGenerator.SplittableGenerator.class);
        c.getMethod("nextLong");
        c.getMethod("nextInt", int.class);
        c.getMethod("nextDouble");
        c.getMethod("splits");
        c.getMethod("splits", long.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testDeterminism();
        testRanges();
        testSplit();

        // ---- 边界值/等价类/等价性（本棒补测） ----
        SplittableRandom r = new SplittableRandom(42L);
        check(r.nextInt(1) == 0, "nextInt(bound=1) == 0");
        check(r.nextLong(1) == 0L, "nextLong(bound=1) == 0");
        check(r.nextInt(5, 6) == 5, "nextInt(origin, origin+1) == origin");
        check(r.nextLong(7, 8) == 7L, "nextLong(origin, origin+1) == origin");
        double db = r.nextDouble(1.0);
        check(db >= 0.0 && db < 1.0, "nextDouble(bound=1) in [0,1)");
        byte[] empty = new byte[0];
        r.nextBytes(empty);
        check(empty.length == 0, "nextBytes(empty) no-op");

        // 越界 origin/bound 抛异常
        assertThrows(IllegalArgumentException.class, () -> new SplittableRandom(1).nextInt(0),
                "nextInt(bound=0)");
        assertThrows(IllegalArgumentException.class, () -> new SplittableRandom(1).nextInt(5, 5),
                "nextInt(origin==bound)");
        assertThrows(IllegalArgumentException.class, () -> new SplittableRandom(1).nextLong(5, 5),
                "nextLong(origin==bound)");
    }

    static void assertThrows(Class<? extends Throwable> type, Runnable r, String msg) {
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

        SplittableRandom x = new SplittableRandom(1);
        int i1 = x.nextInt();
        check(new SplittableRandom(1).nextInt() == i1, "nextInt deterministic");
    }

    static void testRanges() {
        SplittableRandom r = new SplittableRandom(7);
        for (int i = 0; i < 1000; i++) {
            int v = r.nextInt(10);
            check(v >= 0 && v < 10, "nextInt(bound) within [0, bound)");
        }
        for (int i = 0; i < 1000; i++) {
            int v = r.nextInt(5, 10);
            check(v >= 5 && v < 10, "nextInt(origin, bound) within [5, 10)");
        }
        double d = r.nextDouble();
        check(d >= 0.0 && d < 1.0, "nextDouble in [0,1)");
        check(r.nextLong(100) >= 0 && r.nextLong(100) < 100, "nextLong(bound) in [0,100)");
    }

    static void testSplit() {
        SplittableRandom a = new SplittableRandom(123L);
        SplittableRandom b = new SplittableRandom(123L);
        SplittableRandom childA = a.split();
        SplittableRandom childB = b.split();
        check(childA.nextLong() == childB.nextLong(), "split child deterministic for same seed");
        check(a.nextLong() == b.nextLong(), "parent sequence unaffected by split");
        check(childA.split() != null, "nested split returns a child");
    }

    // ---- 本棒新增：完整契约覆盖 ----

    static void testNextIntContracts() {
        SplittableRandom r = new SplittableRandom(11L);
        assertThrows(IllegalArgumentException.class, () -> r.nextInt(0), "nextInt(0) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.nextInt(-5), "nextInt(-5) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.nextInt(6, 5), "nextInt(o>b) IAE");

        SplittableRandom p = new SplittableRandom(99L);
        for (int i = 0; i < 2000; i++) {
            int x = p.nextInt(1024);
            check(x >= 0 && x < 1024, "nextInt(1024) power-of-two in range");
        }
        SplittableRandom o = new SplittableRandom(5L);
        for (int i = 0; i < 2000; i++) {
            int x = o.nextInt(-50, 50);
            check(x >= -50 && x < 50, "nextInt(origin,bound) in range");
        }
    }

    static void testNextLongContracts() {
        SplittableRandom r = new SplittableRandom(13L);
        assertThrows(IllegalArgumentException.class, () -> r.nextLong(0), "nextLong(0) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.nextLong(-1), "nextLong(-1) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.nextLong(6L, 5L), "nextLong(o>b) IAE");

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
        for (int i = 0; i < 5000; i++) {
            double d = r.nextDouble();
            check(d >= 0.0 && d < 1.0, "nextDouble in [0,1)");
        }

        SplittableRandom r2 = new SplittableRandom(1L);
        assertThrows(IllegalArgumentException.class, () -> r2.nextDouble(0.0), "nextDouble(0.0) IAE");
        assertThrows(IllegalArgumentException.class, () -> r2.nextDouble(-1.0), "nextDouble(-1.0) IAE");
        assertThrows(IllegalArgumentException.class, () -> r2.nextDouble(Double.NaN), "nextDouble(NaN) IAE");
        for (int i = 0; i < 1000; i++) {
            double d = new SplittableRandom(29L).nextDouble(3.5);
            check(d >= 0.0 && d < 3.5, "nextDouble(bound) in [0,bound)");
        }

        SplittableRandom r3 = new SplittableRandom(1L);
        assertThrows(IllegalArgumentException.class, () -> r3.nextDouble(1.0, 1.0), "nextDouble(o>=b) IAE");
        assertThrows(IllegalArgumentException.class, () -> r3.nextDouble(Double.NaN, 1.0), "nextDouble(NaN,1) IAE");
        assertThrows(IllegalArgumentException.class, () -> r3.nextDouble(0.0, Double.NaN), "nextDouble(0,NaN) IAE");
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
        check(c1.nextLong() != c2.nextLong() || c1.nextInt() != c2.nextInt(),
              "split children have independent sequences");
    }

    static void testStreams() {
        check(new SplittableRandom(1L).ints(50).count() == 50, "ints(50).count() == 50");
        IntStream is = new SplittableRandom(2L).ints(5000, 10, 20);
        check(is.allMatch(v -> v >= 10 && v < 20), "ints(origin,bound) range");
        LongStream ls = new SplittableRandom(2L).longs(5000, 100L, 200L);
        check(ls.allMatch(v -> v >= 100L && v < 200L), "longs(origin,bound) range");
        DoubleStream ds = new SplittableRandom(2L).doubles(5000, 0.5, 1.5);
        check(ds.allMatch(v -> v >= 0.5 && v < 1.5), "doubles(origin,bound) range");

        SplittableRandom r = new SplittableRandom(1L);
        assertThrows(IllegalArgumentException.class, () -> r.ints(-1L), "ints(-1) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.ints(10, 10, 10), "ints(o>=b) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.longs(-1L), "longs(-1) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.longs(10, 10L, 10L), "longs(o>=b) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.doubles(-1L), "doubles(-1) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.doubles(10, 1.0, 1.0), "doubles(o>=b) IAE");

        check(java.util.Arrays.equals(new SplittableRandom(42L).ints(100).toArray(),
                new SplittableRandom(42L).ints(100).toArray()), "ints deterministic");
        check(java.util.Arrays.equals(new SplittableRandom(42L).longs(100).toArray(),
                new SplittableRandom(42L).longs(100).toArray()), "longs deterministic");
        check(java.util.Arrays.equals(new SplittableRandom(42L).doubles(100).toArray(),
                new SplittableRandom(42L).doubles(100).toArray()), "doubles deterministic");
    }

    static void testSplitsFactories() {
        SplittableRandom r = new SplittableRandom(1L);
        // splits() unlimited, splits(long) sized, splits(-1) IAE.
        check(r.splits().limit(5).count() == 5, "splits().limit(5).count() == 5");
        check(r.splits(10).count() == 10, "splits(10).count() == 10");
        assertThrows(IllegalArgumentException.class, () -> r.splits(-1L), "splits(-1) IAE");

        // split(source): children derive from the given source and are deterministic.
        SplittableRandom source = new SplittableRandom(999L);
        SplittableRandom s1 = r.split(source);
        SplittableRandom s2 = new SplittableRandom(1L).split(new SplittableRandom(999L));
        check(s1 != null && s2 != null, "split(source) returns non-null");
        check(s1.nextLong() == s2.nextLong(), "split(source) deterministic");
    }

    // ===================== 契约深化：IAE 精确消息/首值序列锁/split 确定性/无 setSeed/27 侧 RandomGenerator 新面 =====================

    static void testIaeMessages() {
        try {
            new SplittableRandom(1L).nextInt(0);
            check(false, "nextInt(0) should throw");
        } catch (IllegalArgumentException e) {
            check("bound must be positive".equals(e.getMessage()), "nextInt(0) message exact");
        }
        try {
            new SplittableRandom(1L).nextInt(5, 5);
            check(false, "nextInt(5,5) should throw");
        } catch (IllegalArgumentException e) {
            check("bound must be greater than origin".equals(e.getMessage()),
                  "nextInt(origin==bound) message exact");
        }
        try {
            new SplittableRandom(1L).nextLong(-1);
            check(false, "nextLong(-1) should throw");
        } catch (IllegalArgumentException e) {
            check("bound must be positive".equals(e.getMessage()), "nextLong(-1) message exact");
        }
        try {
            new SplittableRandom(1L).ints(-1L);
            check(false, "ints(-1) should throw");
        } catch (IllegalArgumentException e) {
            check("size must be non-negative".equals(e.getMessage()), "ints(-1) message exact");
        }
        // JDK-27 特有消息：nextDouble 走 RandomGenerator 默认实现，文本与 JDK-8 不同
        try {
            new SplittableRandom(1L).nextDouble(0.0);
            check(false, "nextDouble(0.0) should throw");
        } catch (IllegalArgumentException e) {
            check("bound must be finite and positive".equals(e.getMessage()),
                  "nextDouble(0.0) message exact on JDK-27 (RandomGenerator default)");
        }
    }

    static void testFirstValuesLocked() {
        // 固定 seed 42 的 nextInt() 前 5 个值精确锁（JDK-8 与 JDK-27 实测完全一致）
        int[] expected = {-491277234, 909395113, -1877322334, -1024560952, -1540993733};
        SplittableRandom r = new SplittableRandom(42L);
        for (int i = 0; i < expected.length; i++)
            check(r.nextInt() == expected[i], "SR(42) nextInt #" + i + " locked value");
        SplittableRandom r2 = new SplittableRandom(42L);
        boolean same = true;
        for (int i = 0; i < expected.length; i++) if (r2.nextInt() != expected[i]) same = false;
        check(same, "SR(42) reproduces locked sequence");
        SplittableRandom la = new SplittableRandom(7L), lb = new SplittableRandom(7L);
        boolean lsame = true;
        for (int i = 0; i < 5; i++) if (la.nextLong() != lb.nextLong()) lsame = false;
        check(lsame, "SR(7) nextLong deterministic across instances");
        SplittableRandom da = new SplittableRandom(8L), db = new SplittableRandom(8L);
        boolean dsame = true;
        for (int i = 0; i < 5; i++) if (da.nextDouble() != db.nextDouble()) dsame = false;
        check(dsame, "SR(8) nextDouble deterministic across instances");
        SplittableRandom ba = new SplittableRandom(9L), bb = new SplittableRandom(9L);
        boolean bsame = true;
        for (int i = 0; i < 5; i++) if (ba.nextBoolean() != bb.nextBoolean()) bsame = false;
        check(bsame, "SR(9) nextBoolean deterministic across instances");
    }

    static void testSplitContracts() {
        SplittableRandom p1 = new SplittableRandom(1L), p2 = new SplittableRandom(1L);
        check(p1.split().nextLong() == p2.split().nextLong(),
              "split() children deterministic from equal seeds");
        SplittableRandom child = new SplittableRandom(3L).split();
        for (int i = 0; i < 5; i++) {
            int v = child.nextInt(100);
            check(v >= 0 && v < 100, "split child nextInt(100) in range #" + i);
        }
        SplittableRandom parent = new SplittableRandom(4L);
        SplittableRandom c1 = parent.split();
        long first = c1.nextLong();
        parent.nextLong();
        check(c1.nextLong() != first || c1.nextLong() != first,
              "child sequence continues after parent advances");
    }

    static void testJdk27Surface() {
        // 种子只能经构造传入：SplittableRandom 没有 setSeed
        try {
            SplittableRandom.class.getMethod("setSeed", long.class);
            check(false, "setSeed must not exist");
        } catch (NoSuchMethodException expected) {
            check(true, "no setSeed: seed only via constructor");
        }
        // JDK-17+ 经 RandomGenerator 提供的新面：nextBytes / nextGaussian
        SplittableRandom b1 = new SplittableRandom(9L), b2 = new SplittableRandom(9L);
        byte[] a1 = new byte[8], a2 = new byte[8];
        b1.nextBytes(a1);
        b2.nextBytes(a2);
        check(java.util.Arrays.equals(a1, a2), "nextBytes deterministic from equal seeds");
        check(a1.length == 8, "nextBytes fills the given array length");
        SplittableRandom g1 = new SplittableRandom(7L), g2 = new SplittableRandom(7L);
        boolean gsame = true;
        for (int i = 0; i < 5; i++) if (g1.nextGaussian() != g2.nextGaussian()) gsame = false;
        check(gsame, "nextGaussian deterministic across equal-seed instances");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNextIntContracts();
        testNextLongContracts();
        testNextDoubleContracts();
        testNextBoolean();
        testSplitIndependence();
        testStreams();
        testSplitsFactories();
        testIaeMessages();
        testFirstValuesLocked();
        testSplitContracts();
        testJdk27Surface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
