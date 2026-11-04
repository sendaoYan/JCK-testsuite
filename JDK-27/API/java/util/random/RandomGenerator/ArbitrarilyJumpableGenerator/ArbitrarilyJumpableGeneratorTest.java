/*
 * @test
 * @summary java.util.random.RandomGenerator$ArbitrarilyJumpableGenerator conformance: white-box structure + black-box behavior.
 * @run main ArbitrarilyJumpableGeneratorTest
 */

import java.util.random.*;

public class ArbitrarilyJumpableGeneratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.random.RandomGenerator$ArbitrarilyJumpableGenerator");
        check(c.isInterface(), "RandomGenerator.ArbitrarilyJumpableGenerator is an interface");
        check(RandomGenerator.LeapableGenerator.class.isAssignableFrom(c), "extends RandomGenerator.LeapableGenerator");
        hasMethod(c, "copy");
        hasMethod(c, "jumpPowerOfTwo", int.class);
        hasMethod(c, "jump", double.class);
        hasMethod(c, "jump");
        hasMethod(c, "jumps", double.class);
        hasMethod(c, "jumps", long.class, double.class);
        hasMethod(c, "leap");
        hasMethod(c, "copyAndJump", double.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    // No JDK algorithm is arbitrarily jumpable in this build, so exercise the
    // contract with a minimal in-file implementation sharing a mutable state.
    static void testBlackBox() {
        final long[] st = { 0x9E3779B97F4A7C15L };
        RandomGenerator.ArbitrarilyJumpableGenerator g = new RandomGenerator.ArbitrarilyJumpableGenerator() {
            public long nextLong() {
                long x = st[0];
                x ^= (x << 6); x ^= (x >>> 21); x ^= (x << 7);
                st[0] = x;
                return x;
            }
            public RandomGenerator.ArbitrarilyJumpableGenerator copy() {
                final long snapshot = st[0];
                return new RandomGenerator.ArbitrarilyJumpableGenerator() {
                    long s = snapshot;
                    public long nextLong() {
                        long x = s;
                        x ^= (x << 6); x ^= (x >>> 21); x ^= (x << 7);
                        s = x;
                        return x;
                    }
                    public RandomGenerator.ArbitrarilyJumpableGenerator copy() { return this; }
                    public void jump(double distance) { nextLong(); }
                    public void jumpPowerOfTwo(int p) { nextLong(); }
                    public double jumpDistance() { return 1.0; }
                    public double leapDistance() { return 2.0; }
                };
            }
            public void jump(double distance) { nextLong(); }
            public void jumpPowerOfTwo(int p) { nextLong(); }
            public double jumpDistance() { return 1.0; }
            public double leapDistance() { return 2.0; }
        };

        check(Double.isFinite(g.jumpDistance()), "jumpDistance finite");
        RandomGenerator.ArbitrarilyJumpableGenerator copy = g.copy();
        check(copy.nextLong() == g.nextLong(), "copy shares state");
        // copyAndJump(d) returns the pre-jump copy and jumps the original forward.
        RandomGenerator.ArbitrarilyJumpableGenerator snapshot = g.copy();
        RandomGenerator.ArbitrarilyJumpableGenerator c2 = g.copyAndJump(3.0);
        check(c2.nextLong() == snapshot.nextLong(), "copyAndJump(d) returns pre-jump copy");
        g.jumpPowerOfTwo(2);
        check(Double.isFinite(g.jumpDistance()), "jumpPowerOfTwo usable");
        g.jump();
        check(Double.isFinite(g.jumpDistance()), "jump() usable");
        g.leap();
        check(Double.isFinite(g.leapDistance()), "leap() usable");
        check(g.jumps(2.0).limit(5).count() == 5, "jumps(d) unbounded");
        check(g.jumps(8, 2.0).count() == 8, "jumps(size, d)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
