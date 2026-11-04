/*
 * @test
 * @summary java.util.concurrent.ThreadLocalRandom conformance: white-box structure + black-box behavior.
 * @run main ThreadLocalRandomTest
 */

import java.util.concurrent.*;

public class ThreadLocalRandomTest {
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
        Class<?> c = cls("java.util.concurrent.ThreadLocalRandom");
        check(!c.isInterface() && java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        hasMethod(c, "current");
        hasMethod(c, "nextInt", int.class);
        hasMethod(c, "nextInt", int.class, int.class);
        hasMethod(c, "nextLong", long.class);
        hasMethod(c, "nextLong", long.class, long.class);
        hasMethod(c, "nextDouble");
        hasMethod(c, "nextDouble", double.class);
        hasMethod(c, "nextDouble", double.class, double.class);
        hasMethod(c, "ints");
        hasMethod(c, "longs");
        hasMethod(c, "doubles");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int v = r.nextInt(10);
        check(v >= 0 && v < 10, "nextInt(bound) range");
        int vr = r.nextInt(5, 10);
        check(vr >= 5 && vr < 10, "nextInt(origin, bound) range");
        long w = r.nextLong(5);
        check(w >= 0 && w < 5, "nextLong(bound) range");
        long wr = r.nextLong(100, 200);
        check(wr >= 100 && wr < 200, "nextLong(origin, bound) range");
        double d = r.nextDouble();
        check(d >= 0.0 && d < 1.0, "nextDouble range");
        double dr = r.nextDouble(1.0, 2.0);
        check(dr >= 1.0 && dr < 2.0, "nextDouble(origin, bound) range");
        check(r.ints().limit(10).count() == 10, "ints() stream");
        check(r.longs().limit(10).count() == 10, "longs() stream");
        check(r.doubles().limit(10).count() == 10, "doubles() stream");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
