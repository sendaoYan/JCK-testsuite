/*
 * @test
 * @summary java.util.concurrent.ThreadLocalRandom API conformance: nextInt(bound),
 *          nextLong(bound), and nextDouble range.
 * @run main ThreadLocalRandomTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ThreadLocalRandom;

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

    static void testThreadLocalRandom() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int v = r.nextInt(10);
        check(v >= 0 && v < 10, "nextInt(bound) range");
        long w = r.nextLong(5);
        check(w >= 0 && w < 5, "nextLong(bound) range");
        double d = r.nextDouble();
        check(d >= 0.0 && d < 1.0, "nextDouble range");
    }

    // ---- 契约深化：setSeed UOE / 边界 IAE 消息 / 无参方法域 / 流 / 线程隔离 / 序列化 / J17+ 缺席 ----
    static void testSetSeedUnsupported() {
        // 核心契约：setSeed 不被支持（JDK-8 实测消息为 null）
        ThreadLocalRandom r = ThreadLocalRandom.current();
        try {
            r.setSeed(42L);
        } catch (UnsupportedOperationException e) {
            check(e.getMessage() == null, "setSeed UOE message is null on JDK-8");
            passed++;
            return;
        }
        throw new AssertionError("setSeed must throw UnsupportedOperationException (no exception)");
    }

    static void testBoundValidation() {
        final ThreadLocalRandom r = ThreadLocalRandom.current();
        // JDK-8 实测消息
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
        // JDK-8 实测：nextDouble 单参消息为 "bound must be positive"（JDK-17+ 为 "finite and positive"）
        checkMessage(() -> r.nextDouble(0.0), IllegalArgumentException.class, "bound must be positive", "nextDouble(0)");
        checkMessage(() -> r.nextDouble(-1.0), IllegalArgumentException.class, "bound must be positive", "nextDouble(-1)");
        checkMessage(() -> r.nextDouble(2.0, 2.0), IllegalArgumentException.class,
                "bound must be greater than origin", "nextDouble(2,2)");
        checkMessage(() -> r.nextDouble(3.0, 1.0), IllegalArgumentException.class,
                "bound must be greater than origin", "nextDouble(3,1)");
        // 合法边界可用
        check(r.nextInt(1) == 0, "nextInt(1) always 0");
        check(r.nextLong(1L) == 0L, "nextLong(1) always 0");
        check(r.nextInt(5, 6) == 5, "nextInt(5,6) always 5");
        check(r.nextLong(5L, 6L) == 5L, "nextLong(5,6) always 5");
        double d = r.nextDouble(0.5, 0.5 + Math.ulp(0.5));
        check(d >= 0.5 && d < 0.5 + Math.ulp(0.5), "nextDouble(origin, ulp-bound) in range");
    }

    static void testUnboundedMethods() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        // 只断非抛出 + 返回在域内（不断言分布），聚合后一次断言
        boolean intOk = true, longOk = true, dblOk = true, fltOk = true, boolOk = true, gaussOk = true;
        for (int i = 0; i < 100; i++) {
            int v = r.nextInt();
            intOk &= (v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE);
            longOk &= true; // nextLong() 任意 long 均合法，仅证不抛出
            r.nextLong();
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
        // limit 后 size 与每个值域
        check(r.ints().limit(50).count() == 50, "ints() limit size");
        check(r.ints().limit(50).allMatch(v -> true), "ints() values consumable");
        check(r.longs().limit(50).count() == 50, "longs() limit size");
        check(r.doubles().limit(50).allMatch(v -> v >= 0.0 && v < 1.0), "doubles() values in [0,1)");
        // 带范围重载
        check(r.ints(10, 1, 6).limit(20).allMatch(v -> v >= 1 && v < 6), "ints(origin,bound) in domain");
        check(r.longs(100, 200).limit(20).allMatch(v -> v >= 100 && v < 200), "longs(origin,bound) in domain");
        check(r.doubles(0.5, 1.0).limit(20).allMatch(v -> v >= 0.5 && v < 1.0), "doubles(origin,bound) in domain");
        // 带 streamSize 重载：size 精确
        check(r.ints(7).count() == 7, "ints(streamSize) size");
        check(r.longs(7).count() == 7, "longs(streamSize) size");
        check(r.doubles(7).count() == 7, "doubles(streamSize) size");
        check(r.ints(7, 1, 6).allMatch(v -> v >= 1 && v < 6), "ints(size,origin,bound) all in domain");
    }

    static void testThreadIsolation() throws Exception {
        // 同线程两次 current() 是同一实例
        ThreadLocalRandom r1 = ThreadLocalRandom.current();
        check(ThreadLocalRandom.current() == r1, "current() returns same instance within thread");
        // 线程隔离：两个 daemon 线程各自 current().nextInt() 各 100 次不异常且完成
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(2);
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
        check(done.await(10, java.util.concurrent.TimeUnit.SECONDS), "both isolated threads finished within bound");
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

    // JDK-17+/JDK-27 新增方法在 JDK-8 中缺席（反射断言记录差异）
    static void testAbsentNewerMethods() throws Exception {
        Class<?> c = ThreadLocalRandom.class;
        // nextFloat(float, float) 与 nextFloat(float) 是 JDK-17 加入
        for (java.lang.reflect.Method m : c.getMethods()) {
            check(m.getParameterCount() != 1 || m.getParameterTypes()[0] != float.class
                    || !m.getName().equals("nextFloat"), "nextFloat(float) must be absent in JDK-8");
        }
        for (java.lang.reflect.Method m : c.getMethods()) {
            check(m.getParameterCount() != 2 || m.getParameterTypes()[0] != float.class
                    || !m.getName().equals("nextFloat"), "nextFloat(float,float) must be absent in JDK-8");
        }
    }

    public static void main(String[] args) throws Exception {
        testThreadLocalRandom();
        testSetSeedUnsupported();
        testBoundValidation();
        testUnboundedMethods();
        testStreams();
        testThreadIsolation();
        testSerialization();
        testAbsentNewerMethods();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
