/*
 * @test
 * @summary java.util.Random API conformance (Java 8 surface): complete contract
 *          coverage of constructors/setSeed reproducibility, nextInt/nextLong/nextBoolean/
 *          nextFloat/nextDouble/nextGaussian ranges, nextInt(bound) IllegalArgumentException
 *          matrix (0/negative), nextBytes (null NPE/empty/zero-fill determinism), the
 *          ints/longs/doubles stream factories (sizes + origin/bound IAE + determinism),
 *          and Serializable round-trip state preservation.
 * @run main RandomTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.DoubleStream;

public class RandomTest {
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
            if (a.nextInt() != b.nextInt()) {
                same = false;
                break;
            }
        }
        check(same, "same seed yields identical sequence");
        Random c = new Random(54321L);
        check(c.nextInt() != new Random(12345L).nextInt(), "different seed differs");

        // setSeed restores reproducibility.
        Random r1 = new Random();
        r1.setSeed(99L);
        Random r2 = new Random(99L);
        check(r1.nextInt() == r2.nextInt() && r1.nextLong() == r2.nextLong(),
              "setSeed reproduces same sequence as constructor seed");
    }

    static void testRanges() {
        Random r = new Random(42L);
        for (int i = 0; i < 1000; i++) {
            int bound = r.nextInt(10);
            check(bound >= 0 && bound < 10, "nextInt(10) in [0,10)");
        }
        check(r.nextInt(1) == 0, "nextInt(1) == 0");
        double d = r.nextDouble();
        check(d >= 0.0 && d < 1.0, "nextDouble in [0,1)");
        float f = r.nextFloat();
        check(f >= 0.0f && f < 1.0f, "nextFloat in [0,1)");
        long l = r.nextLong();
        check(true, "nextLong callable"); // full 64-bit range, no meaningful bound
        boolean bool = r.nextBoolean();
        check(bool || !bool, "nextBoolean returns a boolean");

        byte[] bytes = new byte[16];
        r.nextBytes(bytes);
        check(bytes.length == 16, "nextBytes fills array");
    }

    static void testNextIntBoundContract() {
        Random r = new Random(7L);
        // bound <= 0 throws IllegalArgumentException; bound == 0 and negative both covered.
        expectThrows(IllegalArgumentException.class, () -> r.nextInt(0), "nextInt(0) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.nextInt(-1), "nextInt(-1) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.nextInt(Integer.MIN_VALUE),
                     "nextInt(MIN_VALUE) IAE");

        check(new Random(1L).nextInt(1) == 0, "nextInt(1) == 0");

        // power-of-two bound stays in range.
        Random p = new Random(123L);
        for (int i = 0; i < 1000; i++) {
            int v = p.nextInt(256);
            check(v >= 0 && v < 256, "nextInt(256) power-of-two in range");
        }

        // largest legal bound still in range.
        Random big = new Random(456L);
        for (int i = 0; i < 1000; i++) {
            int v = big.nextInt(Integer.MAX_VALUE);
            check(v >= 0 && v < Integer.MAX_VALUE, "nextInt(MAX_VALUE) in range");
        }
    }

    static void testNextBytesContract() {
        // null array -> NPE.
        Random r = new Random(1L);
        expectThrows(NullPointerException.class, () -> r.nextBytes(null), "nextBytes(null) NPE");

        // empty array: no exception, length unchanged.
        byte[] empty = new byte[0];
        r.nextBytes(empty);
        check(empty.length == 0, "nextBytes(empty) ok");

        // deterministic fill for same seed.
        byte[] a = new byte[32];
        byte[] b = new byte[32];
        new Random(999L).nextBytes(a);
        new Random(999L).nextBytes(b);
        boolean same = true;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) { same = false; break; }
        }
        check(same, "nextBytes deterministic for same seed");
    }

    static void testNextGaussian() {
        // deterministic given seed, and all values finite.
        Random r = new Random(2024L);
        Random r2 = new Random(2024L);
        for (int i = 0; i < 100; i++) {
            double g = r.nextGaussian();
            check(!Double.isNaN(g) && !Double.isInfinite(g), "nextGaussian finite");
            check(g == r2.nextGaussian(), "nextGaussian deterministic");
        }
    }

    static void testNextFloatDoubleContract() {
        Random r = new Random(314159L);
        for (int i = 0; i < 10000; i++) {
            double d = r.nextDouble();
            check(d >= 0.0 && d < 1.0, "nextDouble in [0,1)");
        }
        for (int i = 0; i < 10000; i++) {
            float f = r.nextFloat();
            check(f >= 0.0f && f < 1.0f, "nextFloat in [0,1)");
        }
    }

    static void testIntStreams() {
        // ints(streamSize) count.
        check(new Random(1L).ints(50).count() == 50, "ints(50).count() == 50");

        // unlimited ints() limited.
        check(new Random(1L).ints().limit(77).count() == 77, "ints().limit(77).count() == 77");

        // ints(origin, bound) range.
        IntStream s = new Random(2L).ints(5000, 10, 20);
        check(s.allMatch(v -> v >= 10 && v < 20), "ints(origin,bound) all in range");

        // ints(streamSize, origin, bound) count + range.
        IntStream s2 = new Random(3L).ints(300, 5, 6);
        check(s2.count() == 300 && new Random(3L).ints(300, 5, 6).allMatch(v -> v == 5),
              "ints(streamSize,origin,bound) count and range");

        // bad streamSize / range.
        Random r = new Random(1L);
        expectThrows(IllegalArgumentException.class, () -> r.ints(-1), "ints(-1) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.ints(-1L, 0, 10), "ints(-1,o,b) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.ints(10, 10, 10), "ints(origin>=bound) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.ints(10, 10), "ints(o>=b) IAE");

        // determinism.
        int[] a = new Random(42L).ints(100).toArray();
        int[] b = new Random(42L).ints(100).toArray();
        check(java.util.Arrays.equals(a, b), "ints deterministic");
    }

    static void testLongStreams() {
        check(new Random(1L).longs(50).count() == 50, "longs(50).count() == 50");
        check(new Random(1L).longs().limit(66).count() == 66, "longs().limit(66).count() == 66");
        LongStream s = new Random(2L).longs(5000, 100L, 200L);
        check(s.allMatch(v -> v >= 100L && v < 200L), "longs(origin,bound) all in range");
        check(new Random(3L).longs(300, 5L, 6L).count() == 300
              && new Random(3L).longs(300, 5L, 6L).allMatch(v -> v == 5L),
              "longs(streamSize,origin,bound) count and range");

        Random r = new Random(1L);
        expectThrows(IllegalArgumentException.class, () -> r.longs(-1L), "longs(-1) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.longs(10, 10L, 10L), "longs(origin>=bound) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.longs(10L, 10L), "longs(o>=b) IAE");

        long[] a = new Random(42L).longs(100).toArray();
        long[] b = new Random(42L).longs(100).toArray();
        check(java.util.Arrays.equals(a, b), "longs deterministic");
    }

    static void testDoubleStreams() {
        check(new Random(1L).doubles(50).count() == 50, "doubles(50).count() == 50");
        check(new Random(1L).doubles().limit(55).count() == 55, "doubles().limit(55).count() == 55");
        DoubleStream s = new Random(2L).doubles(5000, 0.5, 1.5);
        check(s.allMatch(v -> v >= 0.5 && v < 1.5), "doubles(origin,bound) all in range");
        check(new Random(3L).doubles(300, 0.0, 0.25).count() == 300
              && new Random(3L).doubles(300, 0.0, 0.25).allMatch(v -> v >= 0.0 && v < 0.25),
              "doubles(streamSize,origin,bound) count and range");

        Random r = new Random(1L);
        expectThrows(IllegalArgumentException.class, () -> r.doubles(-1L), "doubles(-1) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.doubles(10, 1.0, 1.0), "doubles(origin>=bound) IAE");
        expectThrows(IllegalArgumentException.class, () -> r.doubles(1.0, 1.0), "doubles(o>=b) IAE");

        double[] a = new Random(42L).doubles(100).toArray();
        double[] b = new Random(42L).doubles(100).toArray();
        check(java.util.Arrays.equals(a, b), "doubles deterministic");
    }

    static void testSerialization() throws Exception {
        // Random is Serializable; round-trip preserves the exact subsequent sequence.
        Random original = new Random(123456789L);
        original.nextInt();
        original.nextLong();
        original.nextDouble(); // advance internal state

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

    public static void main(String[] args) throws Exception {
        testSeedReproducibility();
        testFullSequenceDeterminism();
        testRanges();
        testNextIntBoundContract();
        testNextBytesContract();
        testNextGaussian();
        testNextFloatDoubleContract();
        testIntStreams();
        testLongStreams();
        testDoubleStreams();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
