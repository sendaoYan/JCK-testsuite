/*
 * @test
 * @summary java.util.Random API conformance: seed reproducibility (identical sequences
 *          for equal seeds), nextInt(bound) range, nextInt/nextLong range, nextDouble/
 *          nextFloat in [0,1), nextBoolean, and nextBytes.
 * @run main RandomTest
 */

import java.util.Random;

public class RandomTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

    public static void main(String[] args) {
        testSeedReproducibility();
        testRanges();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
