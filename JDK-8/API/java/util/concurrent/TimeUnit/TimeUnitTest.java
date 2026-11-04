/*
 * @test
 * @summary java.util.concurrent.TimeUnit conformance: white-box structure + black-box
 *          behavior (convert/toNanos/toMillis/toSeconds/toMinutes/toHours/toDays,
 *          saturation on overflow, negative truncation toward zero, valueOf/values,
 *          sleep/timedJoin/timedWait no-early-return).
 * @run main TimeUnitTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TimeUnitTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n + " on " + c.getName(), e); }
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.TimeUnit");
        check(c.isEnum(), "TimeUnit is an enum");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        check(c.getEnumConstants().length == 7, "seven constants");
        hasMethod(c, "convert", long.class, TimeUnit.class);
        hasMethod(c, "toNanos", long.class);
        hasMethod(c, "toMillis", long.class);
        hasMethod(c, "timedWait", Object.class, long.class);
        hasMethod(c, "timedJoin", Thread.class, long.class);
        hasMethod(c, "sleep", long.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testConvert() {
        check(TimeUnit.SECONDS.toMillis(2) == 2000L, "seconds to millis");
        check(TimeUnit.MILLISECONDS.toSeconds(2000) == 2L, "millis to seconds");
        check(TimeUnit.HOURS.toMinutes(1) == 60L, "hours to minutes");
        check(TimeUnit.MINUTES.toSeconds(2) == 120L, "minutes to seconds");
        check(TimeUnit.DAYS.toHours(1) == 24L, "days to hours");
        check(TimeUnit.HOURS.toDays(48) == 2L, "hours to days");
        check(TimeUnit.SECONDS.toNanos(1) == 1_000_000_000L, "seconds to nanos");
        check(TimeUnit.MILLISECONDS.toMicros(1) == 1000L, "millis to micros");
        check(TimeUnit.MICROSECONDS.convert(1, TimeUnit.MILLISECONDS) == 1000L, "convert");
        check(TimeUnit.SECONDS.convert(120_000, TimeUnit.MILLISECONDS) == 120L, "convert millis->seconds");
        check(TimeUnit.valueOf("HOURS") == TimeUnit.HOURS, "valueOf");
        check(TimeUnit.values().length == 7, "values length");
    }

    static void testSleep() throws Exception {
        long start = System.nanoTime();
        TimeUnit.MILLISECONDS.sleep(100);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        check(elapsedMs >= 50, "sleep does not return early (elapsed " + elapsedMs + "ms)");
    }

    static void testTimedJoin() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        Thread jt = new Thread(() -> {
            try { release.await(); } catch (InterruptedException ignored) { }
        });
        jt.start();
        long start = System.nanoTime();
        TimeUnit.MILLISECONDS.timedJoin(jt, 100); // times out while thread is still alive
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        check(jt.isAlive(), "timedJoin returned on timeout while thread still alive");
        check(elapsedMs >= 50, "timedJoin does not return early (elapsed " + elapsedMs + "ms)");
        release.countDown();
        jt.join(5000);
        check(!jt.isAlive(), "worker joined");
    }

    static void testTimedWait() throws Exception {
        Object lock = new Object();
        long start = System.nanoTime();
        synchronized (lock) {
            TimeUnit.MILLISECONDS.timedWait(lock, 100); // nobody notifies
        }
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        check(elapsedMs >= 50, "timedWait does not return early (elapsed " + elapsedMs + "ms)");
    }

    static void testBlackBox() {
        check(TimeUnit.DAYS.toDays(2) == 2L, "DAYS.toDays identity");
        check(TimeUnit.MICROSECONDS.toMicros(5) == 5L, "MICROSECONDS.toMicros identity");
        check(TimeUnit.NANOSECONDS.toNanos(7) == 7L, "NANOSECONDS.toNanos identity");
        check(TimeUnit.MINUTES.toSeconds(-2) == -120L, "negative conversion minutes->seconds");

        // ---- 边界值/等价类/等价性 ----
        // 溢出饱和（不是环绕）
        check(TimeUnit.NANOSECONDS.convert(Long.MAX_VALUE, TimeUnit.DAYS) == Long.MAX_VALUE,
                "convert saturates to MAX");
        check(TimeUnit.NANOSECONDS.convert(Long.MIN_VALUE, TimeUnit.DAYS) == Long.MIN_VALUE,
                "convert saturates to MIN");

        // 负值截断朝零
        check(TimeUnit.DAYS.convert(-25L, TimeUnit.HOURS) == -1L,
                "convert negative truncates toward zero");
        check(TimeUnit.SECONDS.convert(-1500L, TimeUnit.MILLISECONDS) == -1L,
                "convert negative milliseconds truncates toward zero");

        // valueOf 非法字符串抛 IllegalArgumentException
        expectThrows(IllegalArgumentException.class, () -> TimeUnit.valueOf("FOO"),
                "valueOf invalid name");

        // 精确往返：每个常量在自身单位下 convert 恒等
        for (TimeUnit u : TimeUnit.values()) {
            check(u.convert(42, u) == 42L, "convert identity for " + u);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConvert();
        testSleep();
        testTimedJoin();
        testTimedWait();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
