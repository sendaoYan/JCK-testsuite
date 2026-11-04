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
        jt.setDaemon(true);
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

    // ---- 契约深化：values 顺序 / 7×7 换算矩阵 / 饱和矩阵 / name·toString / 截断 / J9+ 缺席 ----
    static void testValuesOrderAndNames() {
        TimeUnit[] us = TimeUnit.values();
        check(us.length == 7, "seven units");
        check(us[0] == TimeUnit.NANOSECONDS, "ordinal 0 NANOSECONDS");
        check(us[1] == TimeUnit.MICROSECONDS, "ordinal 1 MICROSECONDS");
        check(us[2] == TimeUnit.MILLISECONDS, "ordinal 2 MILLISECONDS");
        check(us[3] == TimeUnit.SECONDS, "ordinal 3 SECONDS");
        check(us[4] == TimeUnit.MINUTES, "ordinal 4 MINUTES");
        check(us[5] == TimeUnit.HOURS, "ordinal 5 HOURS");
        check(us[6] == TimeUnit.DAYS, "ordinal 6 DAYS");
        for (TimeUnit u : us) {
            check(u.ordinal() >= 0 && u.ordinal() <= 6, "ordinal in range for " + u);
            check(u.name().equals(u.toString()), "name equals toString for " + u);
            check(TimeUnit.valueOf(u.name()) == u, "valueOf round-trip for " + u);
            check(u.compareTo(u) == 0, "compareTo self is 0 for " + u);
        }
        check(TimeUnit.NANOSECONDS.compareTo(TimeUnit.DAYS) < 0, "finer unit sorts first");
        check(TimeUnit.DAYS.compareTo(TimeUnit.NANOSECONDS) > 0, "coarser unit sorts last");
        expectThrows(IllegalArgumentException.class, () -> TimeUnit.valueOf("BOGUS"),
                "valueOf(BOGUS) throws IAE");
        expectThrows(NullPointerException.class, () -> TimeUnit.valueOf(null),
                "valueOf(null) throws NPE");
    }

    static void testFullConversionMatrix() {
        // 每个单位对 1 的精确换算表（7×7 上/下三角全表，按 toX(1) 计）
        long[][] table = {
            // toNanos, toMicros, toMillis, toSeconds, toMinutes, toHours, toDays
            {1L, 0L, 0L, 0L, 0L, 0L, 0L},                    // NANOSECONDS
            {1_000L, 1L, 0L, 0L, 0L, 0L, 0L},                // MICROSECONDS
            {1_000_000L, 1_000L, 1L, 0L, 0L, 0L, 0L},        // MILLISECONDS
            {1_000_000_000L, 1_000_000L, 1_000L, 1L, 0L, 0L, 0L}, // SECONDS
            {60_000_000_000L, 60_000_000L, 60_000L, 60L, 1L, 0L, 0L}, // MINUTES
            {3_600_000_000_000L, 3_600_000_000L, 3_600_000L, 3_600L, 60L, 1L, 0L}, // HOURS
            {86_400_000_000_000L, 86_400_000_000L, 86_400_000L, 86_400L, 1_440L, 24L, 1L}, // DAYS
        };
        TimeUnit[] us = TimeUnit.values();
        for (int i = 0; i < us.length; i++) {
            TimeUnit u = us[i];
            check(u.toNanos(1) == table[i][0], u + ".toNanos(1)==" + table[i][0]);
            check(u.toMicros(1) == table[i][1], u + ".toMicros(1)==" + table[i][1]);
            check(u.toMillis(1) == table[i][2], u + ".toMillis(1)==" + table[i][2]);
            check(u.toSeconds(1) == table[i][3], u + ".toSeconds(1)==" + table[i][3]);
            check(u.toMinutes(1) == table[i][4], u + ".toMinutes(1)==" + table[i][4]);
            check(u.toHours(1) == table[i][5], u + ".toHours(1)==" + table[i][5]);
            check(u.toDays(1) == table[i][6], u + ".toDays(1)==" + table[i][6]);
        }
        // 关键跨级精确点
        check(TimeUnit.MINUTES.toSeconds(90) == 5400L, "MINUTES.toSeconds(90)==5400");
        check(TimeUnit.DAYS.toMillis(1) == 86_400_000L, "DAYS.toMillis(1)==86400000");
        check(TimeUnit.HOURS.toMinutes(90) == 5400L, "HOURS.toMinutes(90)==5400");
        check(TimeUnit.SECONDS.toMicros(-3) == -3_000_000L, "SECONDS.toMicros(-3)");
        // convert 双向
        check(TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES) == 60L, "SECONDS.convert(1,MINUTES)==60");
        check(TimeUnit.SECONDS.convert(1, TimeUnit.NANOSECONDS) == 0L, "SECONDS.convert(1,NANOS)==0");
        check(TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS) == 2000L, "MILLIS.convert(2,SECONDS)");
        check(TimeUnit.DAYS.convert(48, TimeUnit.HOURS) == 2L, "DAYS.convert(48,HOURS)");
        // 截断（朝零）
        check(TimeUnit.NANOSECONDS.toSeconds(999_999_999L) == 0L, "NANOS.toSeconds(999999999)==0 truncation");
        check(TimeUnit.MILLISECONDS.toSeconds(1999) == 1L, "MILLIS.toSeconds(1999)==1 truncation");
        check(TimeUnit.NANOSECONDS.toMillis(-999_999L) == 0L, "NANOS.toMillis(-999999)==0 negative truncates toward zero");
        check(TimeUnit.SECONDS.toMinutes(119) == 1L, "SECONDS.toMinutes(119)==1 truncation");
    }

    static void testSaturation() {
        // 溢出饱和契约：向上换算溢出 → Long.MAX_VALUE / Long.MIN_VALUE，不抛异常
        for (TimeUnit u : new TimeUnit[]{TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS}) {
            check(u.toNanos(Long.MAX_VALUE) == Long.MAX_VALUE, u + ".toNanos(MAX) saturates to MAX");
            check(u.toNanos(Long.MIN_VALUE) == Long.MIN_VALUE, u + ".toNanos(MIN) saturates to MIN");
        }
        check(TimeUnit.MILLISECONDS.toNanos(Long.MAX_VALUE) == Long.MAX_VALUE, "MILLIS.toNanos(MAX) saturates");
        check(TimeUnit.MICROSECONDS.toNanos(Long.MAX_VALUE) == Long.MAX_VALUE, "MICROS.toNanos(MAX) saturates");
        check(TimeUnit.HOURS.toMillis(Long.MAX_VALUE) == Long.MAX_VALUE, "HOURS.toMillis(MAX) saturates");
        check(TimeUnit.MINUTES.toMicros(Long.MAX_VALUE) == Long.MAX_VALUE, "MINUTES.toMicros(MAX) saturates");
        check(TimeUnit.DAYS.toSeconds(Long.MAX_VALUE) == Long.MAX_VALUE, "DAYS.toSeconds(MAX) saturates");
        check(TimeUnit.SECONDS.toMillis(Long.MAX_VALUE) == Long.MAX_VALUE, "SECONDS.toMillis(MAX) saturates");
        // 不溢出的换算不饱和（除法下取整可精确）
        check(TimeUnit.SECONDS.toMinutes(Long.MAX_VALUE) == 153722867280912930L,
                "SECONDS.toMinutes(MAX)==153722867280912930 (fits, no saturation)");
        check(TimeUnit.MILLISECONDS.toSeconds(Long.MAX_VALUE) == 9223372036854775L,
                "MILLIS.toSeconds(MAX) exact division");
        check(TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE) == 9223372036854L,
                "NANOS.toMillis(MAX) truncating division");
        // 负向饱和
        check(TimeUnit.DAYS.toNanos(-1L) == -86_400_000_000_000L, "DAYS.toNanos(-1) exact");
        check(TimeUnit.HOURS.toNanos(Long.MIN_VALUE) == Long.MIN_VALUE, "HOURS.toNanos(MIN) saturates");
        // convert 饱和（源单位大）
        check(TimeUnit.NANOSECONDS.convert(Long.MAX_VALUE, TimeUnit.DAYS) == Long.MAX_VALUE,
                "NANOS.convert(MAX, DAYS) saturates");
        check(TimeUnit.NANOSECONDS.convert(Long.MIN_VALUE, TimeUnit.DAYS) == Long.MIN_VALUE,
                "NANOS.convert(MIN, DAYS) saturates");
    }

    // JDK-9+ 新增方法在 JDK-8 中缺席（反射断言记录差异）
    static void testAbsentJdk9Methods() throws Exception {
        Class<?> c = TimeUnit.class;
        boolean hasConvertDuration = false, hasToChronoUnit = false, hasOf = false;
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals("convert") && m.getParameterCount() == 1
                    && m.getParameterTypes()[0].getName().equals("java.time.Duration")) hasConvertDuration = true;
            if (m.getName().equals("toChronoUnit")) hasToChronoUnit = true;
            if (m.getName().equals("of")) hasOf = true;
        }
        check(!hasConvertDuration, "convert(Duration) must be absent in JDK-8");
        check(!hasToChronoUnit, "toChronoUnit() must be absent in JDK-8");
        check(!hasOf, "of(ChronoUnit) must be absent in JDK-8");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConvert();
        testSleep();
        testTimedJoin();
        testTimedWait();
        testBlackBox();
        testValuesOrderAndNames();
        testFullConversionMatrix();
        testSaturation();
        testAbsentJdk9Methods();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
