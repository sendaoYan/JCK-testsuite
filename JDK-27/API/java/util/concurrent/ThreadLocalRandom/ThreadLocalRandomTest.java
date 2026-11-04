/*
 * @test
 * @summary java.util.concurrent.ThreadLocalRandom conformance: white-box structure + black-box behavior.
 * @run main ThreadLocalRandomTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.*;

public class ThreadLocalRandomTest {
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

    static void checkMessage(Runnable r, Class<? extends Throwable> exType, String expect, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (!exType.isInstance(t)) throw new AssertionError(msg + " (threw " + t + ")");
            check(expect.equals(t.getMessage()), msg + " message <" + t.getMessage() + "> equals <" + expect + ">");
            return;
        }
        throw new AssertionError(msg + " (no exception)");
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
        hasMethod(c, "setSeed", long.class);
        hasMethod(c, "nextInt");
        hasMethod(c, "nextInt", int.class);
        hasMethod(c, "nextInt", int.class, int.class);
        hasMethod(c, "nextLong");
        hasMethod(c, "nextLong", long.class);
        hasMethod(c, "nextLong", long.class, long.class);
        hasMethod(c, "nextDouble");
        hasMethod(c, "nextDouble", double.class);
        hasMethod(c, "nextDouble", double.class, double.class);
        hasMethod(c, "nextFloat");
        hasMethod(c, "nextFloat", float.class);
        hasMethod(c, "nextFloat", float.class, float.class);
        hasMethod(c, "nextBoolean");
        hasMethod(c, "nextGaussian");
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

    // ---- 契约深化：setSeed UOE / 边界 IAE 消息 / 无参方法域 / 流 / 线程隔离 / 序列化 ----
    static void testSetSeedUnsupported() {
        // 核心契约：setSeed 不被支持（JDK-27 实测消息为 null）
        ThreadLocalRandom r = ThreadLocalRandom.current();
        try {
            r.setSeed(42L);
        } catch (UnsupportedOperationException e) {
            check(e.getMessage() == null, "setSeed UOE message is null on JDK-27");
            passed++;
            return;
        }
        throw new AssertionError("setSeed must throw UnsupportedOperationException (no exception)");
    }

    static void testBoundValidation() {
        final ThreadLocalRandom r = ThreadLocalRandom.current();
        // JDK-27 实测消息
        checkMessage(() -> r.nextInt(0), IllegalArgumentException.class, "bound must be positive", "nextInt(0)");
        checkMessage(() -> r.nextInt(-5), IllegalArgumentException.class, "bound must be positive", "nextInt(-5)");
        checkMessage(() -> r.nextInt(5, 5), IllegalArgumentException.class,
                "bound must be greater than origin", "nextInt(5,5)");
        checkMessage(() -> r.nextInt(7, 3), IllegalArgumentException.class,
                "bound must be greater than origin", "nextInt(7,3)");
        checkMessage(() -> r.nextLong(0L), IllegalArgumentException.class, "bound must be positive", "nextLong(0)");
        checkMessage(() -> r.nextLong(-1L), IllegalArgumentException.class, "bound must be positive", "nextLong(-1)");
        checkMessage(() -> r.nextLong(5L, 5L), IllegalArgumentException.class,
                "bound must be greater than origin", "nextLong(5,5)");
        checkMessage(() -> r.nextLong(9L, 2L), IllegalArgumentException.class,
                "bound must be greater than origin", "nextLong(9,2)");
        // JDK-27 实测：nextDouble 单参消息为 "bound must be finite and positive"（JDK-8 为 "bound must be positive"）
        checkMessage(() -> r.nextDouble(0.0), IllegalArgumentException.class,
                "bound must be finite and positive", "nextDouble(0)");
        checkMessage(() -> r.nextDouble(-1.0), IllegalArgumentException.class,
                "bound must be finite and positive", "nextDouble(-1)");
        checkMessage(() -> r.nextDouble(Double.POSITIVE_INFINITY), IllegalArgumentException.class,
                "bound must be finite and positive", "nextDouble(+Inf)");
        checkMessage(() -> r.nextDouble(2.0, 2.0), IllegalArgumentException.class,
                "bound must be greater than origin", "nextDouble(2,2)");
        checkMessage(() -> r.nextDouble(3.0, 1.0), IllegalArgumentException.class,
                "bound must be greater than origin", "nextDouble(3,1)");
        // JDK-17+ 的 nextFloat 带参重载的边界契约
        checkMessage(() -> r.nextFloat(0.0f), IllegalArgumentException.class,
                "bound must be finite and positive", "nextFloat(0)");
        checkMessage(() -> r.nextFloat(-1.0f), IllegalArgumentException.class,
                "bound must be finite and positive", "nextFloat(-1)");
        checkMessage(() -> r.nextFloat(2.0f, 2.0f), IllegalArgumentException.class,
                "bound must be greater than origin", "nextFloat(2,2)");
        checkMessage(() -> r.nextFloat(3.0f, 1.0f), IllegalArgumentException.class,
                "bound must be greater than origin", "nextFloat(3,1)");
        // 合法边界可用
        check(r.nextInt(1) == 0, "nextInt(1) always 0");
        check(r.nextLong(1L) == 0L, "nextLong(1) always 0");
        check(r.nextInt(5, 6) == 5, "nextInt(5,6) always 5");
        check(r.nextLong(5L, 6L) == 5L, "nextLong(5,6) always 5");
        double d = r.nextDouble(0.5, 0.5 + Math.ulp(0.5));
        check(d >= 0.5 && d < 0.5 + Math.ulp(0.5), "nextDouble(origin, ulp-bound) in range");
        float f = r.nextFloat(1.0f);
        check(f >= 0.0f && f < 1.0f, "nextFloat(bound) in range");
        float fr = r.nextFloat(0.5f, 1.0f);
        check(fr >= 0.5f && fr < 1.0f, "nextFloat(origin, bound) in range");
    }

    static void testUnboundedMethods() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        // 只断非抛出 + 返回在域内（不断言分布），聚合后一次断言
        boolean intOk = true, longOk = true, dblOk = true, fltOk = true, boolOk = true, gaussOk = true;
        for (int i = 0; i < 100; i++) {
            int v = r.nextInt();
            intOk &= (v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE);
            r.nextLong();
            longOk &= true; // 任意 long 均合法，仅证不抛出
            double d = r.nextDouble();
            dblOk &= (d >= 0.0 && d < 1.0);
            float f = r.nextFloat();
            fltOk &= (f >= 0.0f && f < 1.0f);
            boolean b = r.nextBoolean();
            boolOk &= (b || !b);
            double g = r.nextGaussian();
            gaussOk &= (!Double.isNaN(g) && Double.isFinite(g));
        }
        check(intOk, "nextInt() x100 no exception");
        check(longOk, "nextLong() x100 no exception");
        check(dblOk, "nextDouble() x100 all in [0,1)");
        check(fltOk, "nextFloat() x100 all in [0,1)");
        check(boolOk, "nextBoolean() x100 returns booleans");
        check(gaussOk, "nextGaussian() x100 all finite");
    }

    static void testStreams() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        check(r.ints().limit(50).count() == 50, "ints() limit size");
        check(r.longs().limit(50).count() == 50, "longs() limit size");
        check(r.doubles().limit(50).allMatch(v -> v >= 0.0 && v < 1.0), "doubles() values in [0,1)");
        check(r.ints(1, 6).limit(20).allMatch(v -> v >= 1 && v < 6), "ints(origin,bound) in domain");
        check(r.longs(100, 200).limit(20).allMatch(v -> v >= 100 && v < 200), "longs(origin,bound) in domain");
        check(r.doubles(0.5, 1.0).limit(20).allMatch(v -> v >= 0.5 && v < 1.0), "doubles(origin,bound) in domain");
        check(r.ints(7).count() == 7, "ints(streamSize) size");
        check(r.longs(7).count() == 7, "longs(streamSize) size");
        check(r.doubles(7).count() == 7, "doubles(streamSize) size");
        check(r.ints(7, 1, 6).allMatch(v -> v >= 1 && v < 6), "ints(size,origin,bound) all in domain");
        check(r.longs(7, 100, 200).allMatch(v -> v >= 100 && v < 200), "longs(size,origin,bound) all in domain");
        check(r.doubles(7, 0.5, 1.0).allMatch(v -> v >= 0.5 && v < 1.0), "doubles(size,origin,bound) all in domain");
    }

    static void testThreadIsolation() throws Exception {
        // 同线程两次 current() 是同一实例
        ThreadLocalRandom r1 = ThreadLocalRandom.current();
        check(ThreadLocalRandom.current() == r1, "current() returns same instance within thread");
        // 线程隔离：两个 daemon 线程各自 current().nextInt() 各 100 次不异常且完成
        final CountDownLatch done = new CountDownLatch(2);
        final boolean[] ok = {true, true};
        for (int t = 0; t < 2; t++) {
            final int id = t;
            Thread th = new Thread(() -> {
                try {
                    ThreadLocalRandom mine = ThreadLocalRandom.current();
                    for (int i = 0; i < 100; i++) {
                        int v = mine.nextInt(1000);
                        if (v < 0 || v >= 1000) ok[id] = false;
                    }
                } catch (Throwable err) {
                    ok[id] = false;
                } finally {
                    done.countDown();
                }
            });
            th.setDaemon(true);
            th.start();
        }
        check(done.await(10, TimeUnit.SECONDS), "both isolated threads finished within bound");
        check(ok[0] && ok[1], "isolated threads drew 100 values each without exception");
    }

    static void testSerialization() throws Exception {
        // TLR 可序列化；实测 round-trip 解析回 current()（readResolve 语义）
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(ThreadLocalRandom.current());
        }
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy instanceof ThreadLocalRandom, "round-trip yields ThreadLocalRandom");
        check(ThreadLocalRandom.current() == copy, "deserialized TLR resolves to current() instance (readResolve)");
        check(((ThreadLocalRandom) copy).nextInt(10) >= 0, "deserialized instance remains usable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSetSeedUnsupported();
        testBoundValidation();
        testUnboundedMethods();
        testStreams();
        testThreadIsolation();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
