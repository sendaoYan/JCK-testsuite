/*
 * @test
 * @summary java.util.concurrent.TimeUnit conformance: white-box structure + black-box
 *          behavior (convert/toChronoUnit/of/sleep/timedJoin/timedWait no-early-return).
 * @run main TimeUnitTest
 */

import java.time.Duration;
import java.time.temporal.ChronoUnit;
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.TimeUnit");
        check(c.isEnum(), "TimeUnit is an enum");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        check(c.getEnumConstants().length == 7, "seven constants");
        hasMethod(c, "convert", long.class, TimeUnit.class);
        hasMethod(c, "convert", Duration.class);
        hasMethod(c, "toNanos", long.class);
        hasMethod(c, "toMillis", long.class);
        hasMethod(c, "timedWait", Object.class, long.class);
        hasMethod(c, "timedJoin", Thread.class, long.class);
        hasMethod(c, "sleep", long.class);
        hasMethod(c, "toChronoUnit");
        hasMethod(c, "of", ChronoUnit.class);
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

        // Duration conversions (truncation towards zero for coarser units)
        check(TimeUnit.MILLISECONDS.convert(Duration.ofSeconds(2)) == 2000L, "convert(Duration) millis");
        check(TimeUnit.SECONDS.convert(Duration.ofMillis(1500)) == 1L, "convert(Duration) truncates to seconds");

        // ChronoUnit mapping
        check(TimeUnit.HOURS.toChronoUnit() == ChronoUnit.HOURS, "toChronoUnit");
        check(TimeUnit.of(ChronoUnit.MILLIS) == TimeUnit.MILLISECONDS, "of(ChronoUnit)");
        check(TimeUnit.of(ChronoUnit.NANOS) == TimeUnit.NANOSECONDS, "of(ChronoUnit.NANOS)");
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
        check(TimeUnit.of(ChronoUnit.MICROS) == TimeUnit.MICROSECONDS, "of(MICROS)");
        check(TimeUnit.of(ChronoUnit.SECONDS) == TimeUnit.SECONDS, "of(SECONDS)");
        check(TimeUnit.of(ChronoUnit.DAYS) == TimeUnit.DAYS, "of(DAYS)");
        check(TimeUnit.SECONDS.toChronoUnit() == ChronoUnit.SECONDS, "SECONDS.toChronoUnit");
        check(TimeUnit.NANOSECONDS.convert(Duration.ofNanos(123)) == 123L, "convert(Duration) nanos");
        check(TimeUnit.MICROSECONDS.convert(Duration.ofNanos(1500)) == 1L, "convert(Duration) truncates nanos to micros");
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
