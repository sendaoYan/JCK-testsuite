/*
 * @test
 * @summary java.util.Random conformance: white-box structure plus black-box behavior of
 *          seed reproducibility, nextInt(bound)/nextInt/nextLong/nextDouble/nextFloat ranges,
 *          nextBoolean/nextBytes, nextGaussian determinism, setSeed, ints/longs/doubles
 *          streams, RandomGenerator default methods (origin/bound + nextExponential +
 *          nextGaussian(mean,stddev)), Random.from factory, and Serializable round-trip.
 * @run main RandomTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.DoubleStream;

public class RandomTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Random");
        check(!c.isInterface(), "Random is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "Random is concrete");
        check(Serializable.class.isAssignableFrom(c), "Random implements Serializable");
        check(RandomGenerator.class.isAssignableFrom(c), "Random implements RandomGenerator");
        c.getMethod("setSeed", long.class);
        c.getMethod("nextInt");
        c.getMethod("nextInt", int.class);
        c.getMethod("nextLong");
        c.getMethod("nextDouble");
        c.getMethod("nextBytes", byte[].class);
        c.getMethod("ints", long.class);
        c.getMethod("from", RandomGenerator.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testSeedReproducibility();
        testFullSequenceDeterminism();
        testRanges();
        testSetSeed();
        testStreams();
        testGaussian();

        // ---- 边界值/等价类/等价性（本棒补测） ----
        Random r = new Random(42L);
        check(r.nextLong(1) == 0L, "nextLong(bound=1) == 0");
        check(r.nextInt(5, 6) == 5, "nextInt(origin, origin+1) == origin");
        check(r.nextLong(7, 8) == 7L, "nextLong(origin, origin+1) == origin");
        double db = r.nextDouble(10.0);
        check(db >= 0.0 && db < 10.0, "nextDouble(bound) in [0,bound)");
        double dor = r.nextDouble(5.0, 20.0);
        check(dor >= 5.0 && dor < 20.0, "nextDouble(origin,bound) in [origin,bound)");
        byte[] empty = new byte[0];
        r.nextBytes(empty);
        check(empty.length == 0, "nextBytes(empty) no-op");

        // 越界 origin/bound 抛异常
        assertThrows(IllegalArgumentException.class, () -> new Random(1).nextInt(5, 5),
                "nextInt(origin==bound)");
        assertThrows(IllegalArgumentException.class, () -> new Random(1).nextLong(5, 5),
                "nextLong(origin==bound)");
    }

    // ---- 契约深化：跨方法全序列确定性闭环 ----
    static void testFullSequenceDeterminism() {
        Random a = new Random(-42L);
        Random b = new Random(-42L);
        boolean same = true;
        for (int i = 0; i < 64; i++) {
            same &= a.nextInt() == b.nextInt();
            same &= a.nextLong() == b.nextLong();
            same &= a.nextInt(1000) == b.nextInt(1000);
            same &= a.nextDouble() == b.nextDouble();
            same &= a.nextFloat() == b.nextFloat();
            same &= a.nextBoolean() == b.nextBoolean();
            same &= a.nextGaussian() == b.nextGaussian();
        }
        check(same, "all next* methods deterministic for same seed");
        // 序列推进：连续 nextInt 不重复同值（固定 seed 的已知首值）
        Random r = new Random(42L);
        check(r.nextInt() == -1170105035, "Random(42) first nextInt known value");
        check(r.nextInt() != -1170105035, "sequence advances");
        // 已知向量锁定：seed 0 的前三个 nextInt
        Random z = new Random(0L);
        int z1 = z.nextInt(), z2 = z.nextInt(), z3 = z.nextInt();
        check(z1 == -1155484576 && z2 == -723955400 && z3 == 1033096058,
              "Random(0) first three nextInt known vector");
    }

    static void testSeedReproducibility() {
        Random a = new Random(12345L);
        Random b = new Random(12345L);
        boolean same = true;
        for (int i = 0; i < 100; i++) {
            if (a.nextInt() != b.nextInt()) { same = false; break; }
        }
        check(same, "same seed yields identical sequence");
        Random c = new Random(54321L);
        check(c.nextInt() != new Random(12345L).nextInt(), "different seed differs");
    }

    static void testRanges() {
        Random r = new Random(42L);
        for (int i = 0; i < 1000; i++) {
            int bound = r.nextInt(10);
            check(bound >= 0 && bound < 10, "nextInt(10) in [0,10)");
        }
        check(r.nextInt(1) == 0, "nextInt(1) == 0");
        assertThrows(IllegalArgumentException.class, () -> new Random(1).nextInt(0),
                "nextInt(0) throws IllegalArgumentException");

        double d = r.nextDouble();
        check(d >= 0.0 && d < 1.0, "nextDouble in [0,1)");
        float f = r.nextFloat();
        check(f >= 0.0f && f < 1.0f, "nextFloat in [0,1)");

        long l = r.nextLong();
        check(true, "nextLong callable");

        byte[] bytes = new byte[16];
        new Random(42L).nextBytes(bytes);
        check(bytes.length == 16, "nextBytes fills array");
        byte[] replay = new byte[16];
        new Random(42L).nextBytes(replay);
        check(Arrays.equals(bytes, replay), "nextBytes deterministic for same seed");
    }

    static void testSetSeed() {
        Random a = new Random(12345L);
        int first = a.nextInt();
        a.setSeed(12345L);
        check(a.nextInt() == first, "setSeed resets sequence");
        Random b = new Random(12345L);
        check(b.nextInt() == first, "setSeed matches constructor seed");
    }

    static void testStreams() {
        Random r = new Random(99L);
        int[] first = r.ints(5).toArray();
        check(first.length == 5, "ints(5) length");
        int[] second = new Random(99L).ints(5).toArray();
        check(Arrays.equals(first, second), "ints stream deterministic for same seed");

        long[] longs = new Random(7L).longs(3).toArray();
        check(longs.length == 3, "longs(3) length");
        double[] doubles = new Random(7L).doubles(4).toArray();
        check(doubles.length == 4, "doubles(4) length");
        for (double dd : doubles) {
            check(dd >= 0.0 && dd < 1.0, "doubles values in [0,1)");
        }
    }

    static void testGaussian() {
        Random a = new Random(12345L);
        Random b = new Random(12345L);
        a.nextGaussian();
        b.nextGaussian();
        check(a.nextGaussian() == b.nextGaussian(), "nextGaussian deterministic for same seed");
    }

    // ---- 本棒新增：完整契约覆盖 ----

    static void testNextIntBoundMatrix() {
        Random r = new Random(7L);
        assertThrows(IllegalArgumentException.class, () -> r.nextInt(0), "nextInt(0) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.nextInt(-1), "nextInt(-1) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.nextInt(Integer.MIN_VALUE),
                "nextInt(MIN_VALUE) IAE");

        Random p = new Random(123L);
        for (int i = 0; i < 1000; i++) {
            int v = p.nextInt(256);
            check(v >= 0 && v < 256, "nextInt(256) power-of-two in range");
        }
        Random big = new Random(456L);
        for (int i = 0; i < 1000; i++) {
            int v = big.nextInt(Integer.MAX_VALUE);
            check(v >= 0 && v < Integer.MAX_VALUE, "nextInt(MAX_VALUE) in range");
        }
    }

    static void testNextBytesContract() {
        Random r = new Random(1L);
        assertThrows(NullPointerException.class, () -> r.nextBytes(null), "nextBytes(null) NPE");
        byte[] empty = new byte[0];
        r.nextBytes(empty);
        check(empty.length == 0, "nextBytes(empty) ok");
    }

    static void testGaussianFinite() {
        Random r = new Random(2024L);
        Random r2 = new Random(2024L);
        for (int i = 0; i < 100; i++) {
            double g = r.nextGaussian();
            check(!Double.isNaN(g) && !Double.isInfinite(g), "nextGaussian finite");
            check(g == r2.nextGaussian(), "nextGaussian deterministic");
        }
    }

    static void testStreamContracts() {
        Random r = new Random(1L);
        assertThrows(IllegalArgumentException.class, () -> r.ints(-1L), "ints(-1) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.ints(-1L, 0, 10), "ints(-1,o,b) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.ints(10, 10, 10), "ints(origin>=bound) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.ints(10, 10), "ints(o>=b) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.longs(-1L), "longs(-1) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.longs(10, 10L, 10L), "longs(o>=b) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.longs(10L, 10L), "longs(o>=b) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.doubles(-1L), "doubles(-1) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.doubles(10, 1.0, 1.0), "doubles(o>=b) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.doubles(1.0, 1.0), "doubles(o>=b) IAE");

        IntStream is = new Random(2L).ints(5000, 10, 20);
        check(is.allMatch(v -> v >= 10 && v < 20), "ints(origin,bound) range");
        LongStream ls = new Random(2L).longs(5000, 100L, 200L);
        check(ls.allMatch(v -> v >= 100L && v < 200L), "longs(origin,bound) range");
        DoubleStream ds = new Random(2L).doubles(5000, 0.5, 1.5);
        check(ds.allMatch(v -> v >= 0.5 && v < 1.5), "doubles(origin,bound) range");

        check(new Random(1L).ints(50).count() == 50, "ints(50).count() == 50");
        check(new Random(1L).ints().limit(77).count() == 77, "ints().limit(77).count() == 77");

        check(Arrays.equals(new Random(42L).ints(100).toArray(), new Random(42L).ints(100).toArray()),
                "ints deterministic");
        check(Arrays.equals(new Random(42L).longs(100).toArray(), new Random(42L).longs(100).toArray()),
                "longs deterministic");
        check(Arrays.equals(new Random(42L).doubles(100).toArray(), new Random(42L).doubles(100).toArray()),
                "doubles deterministic");
    }

    static void testSerialization() throws Exception {
        Random original = new Random(123456789L);
        original.nextInt();
        original.nextLong();
        original.nextDouble();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Random revived = (Random) ois.readObject();
        ois.close();

        boolean same = true;
        for (int i = 0; i < 50; i++) {
            if (original.nextInt() != revived.nextInt()) { same = false; break; }
        }
        check(same, "Serializable round-trip preserves sequence");
        check(revived.nextDouble() >= 0.0 && revived.nextDouble() < 1.0,
                "revived Random still operational");
    }

    static void testRandomGeneratorDefaults() {
        Random r = new Random(5L);
        // nextInt(origin, bound) / nextLong(origin, bound) range + IAE.
        for (int i = 0; i < 1000; i++) {
            int v = r.nextInt(-50, 50);
            check(v >= -50 && v < 50, "nextInt(origin,bound) range");
        }
        for (int i = 0; i < 1000; i++) {
            long v = r.nextLong(-100L, 100L);
            check(v >= -100L && v < 100L, "nextLong(origin,bound) range");
        }
        assertThrows(IllegalArgumentException.class, () -> r.nextInt(5, 5), "nextInt(o==b) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.nextLong(5L, 5L), "nextLong(o==b) IAE");

        // nextDouble(bound) / nextDouble(origin, bound) range + IAE.
        for (int i = 0; i < 1000; i++) {
            double v = r.nextDouble(10.0);
            check(v >= 0.0 && v < 10.0, "nextDouble(bound) range");
        }
        for (int i = 0; i < 1000; i++) {
            double v = r.nextDouble(2.0, 5.0);
            check(v >= 2.0 && v < 5.0, "nextDouble(origin,bound) range");
        }
        assertThrows(IllegalArgumentException.class, () -> r.nextDouble(0.0), "nextDouble(0) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.nextDouble(Double.NaN), "nextDouble(NaN) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.nextDouble(1.0, 1.0), "nextDouble(o==b) IAE");
        assertThrows(IllegalArgumentException.class, () -> r.nextDouble(Double.NaN, 1.0), "nextDouble(NaN,b) IAE");

        // nextExponential: finite and >= 0.
        for (int i = 0; i < 100; i++) {
            double e = r.nextExponential();
            check(!Double.isNaN(e) && !Double.isInfinite(e) && e >= 0.0, "nextExponential finite and >= 0");
        }

        // nextGaussian(mean, stddev): finite.
        for (int i = 0; i < 100; i++) {
            double g = r.nextGaussian(10.0, 2.0);
            check(!Double.isNaN(g) && !Double.isInfinite(g), "nextGaussian(mean,stddev) finite");
        }
    }

    static void testFromFactory() {
        Random source = new Random(777L);
        Random a = Random.from(source);
        Random b = Random.from(new Random(777L));
        check(a != null && b != null, "from returns non-null");
        check(a.nextLong() == b.nextLong() && a.nextInt() == b.nextInt(),
                "from(factory) reproduces same sequence as source");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNextIntBoundMatrix();
        testNextBytesContract();
        testGaussianFinite();
        testStreamContracts();
        testSerialization();
        testRandomGeneratorDefaults();
        testFromFactory();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
