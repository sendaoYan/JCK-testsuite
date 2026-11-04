/*
 * @test
 * @summary java.time.Clock conformance: white-box shape + black-box fixed/offset/tick/system clocks,
 *          instant/zone/millis consistency, withZone, and equality.
 * @run main ClockTest
 *
 * 设计案（黑盒）：
 * 1. 契约：Clock 是当前时刻与时区的抽象源，固定时刻测试用 fixed/offset/tick 保证确定性。
 * 2. 场景：Clock.fixed 固定 instant+zone；Clock.offset 偏移；Clock.tick/tickSeconds 截断；withZone 换时区；system* 探测环境。
 * 3. 组合链路：fixed -> offset(Duration) -> instant 校验 -> withZone -> millis 与 instant 一致 -> tick 截断。
 * 4. 不变量：millis()==instant().toEpochMilli()；withZone 保持 instant 而换 zone；tick 向下截断到 tick 边界。
 * 5. 边界/异常：tick 的 tickDuration 必须为正，否则抛 IllegalArgumentException。
 */

import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class ClockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName()
                    + " but got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.Clock");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(java.time.InstantSource.class.isAssignableFrom(c), "InstantSource");
    }

    static void testBlackBox() {
        testFixed();
        testOffsetAndWithZone();
        testTick();
        testSystemClocks();
        testErrors();

        // ---- 边界值/等价类/等价性（本棒补测） ----
        // tick 边界：恰好落在 tick 边界时不截断
        check(Clock.tick(Clock.fixed(Instant.ofEpochSecond(10), ZoneOffset.UTC),
                Duration.ofSeconds(5)).instant().equals(Instant.ofEpochSecond(10)),
                "tick exact boundary unchanged");
        check(Clock.tick(Clock.fixed(Instant.ofEpochSecond(14), ZoneOffset.UTC),
                Duration.ofSeconds(5)).instant().equals(Instant.ofEpochSecond(10)),
                "tick truncates down to 10");

        // offset 负偏移（回退）
        check(Clock.offset(Clock.fixed(Instant.ofEpochSecond(100), ZoneOffset.UTC),
                Duration.ofSeconds(-50)).instant().equals(Instant.ofEpochSecond(50)),
                "offset negative");

        // millis 与 instant 一致（纳秒截断）
        check(Clock.fixed(Instant.ofEpochSecond(1, 999_000_000), ZoneOffset.UTC).millis()
                == Instant.ofEpochSecond(1, 999_000_000).toEpochMilli(), "millis truncates nanos");

        // withZone 保持 instant
        Instant base = Instant.ofEpochSecond(500);
        check(Clock.fixed(base, ZoneOffset.UTC).withZone(ZoneOffset.ofHours(-5)).instant()
                .equals(base), "withZone keeps instant");

        // equals/hashCode
        Clock c1 = Clock.fixed(Instant.ofEpochSecond(1), ZoneOffset.UTC);
        Clock c2 = Clock.fixed(Instant.ofEpochSecond(1), ZoneOffset.UTC);
        check(c1.equals(c2) && c1.hashCode() == c2.hashCode(), "fixed equals/hashCode");
        check(!c1.equals(Clock.fixed(Instant.ofEpochSecond(2), ZoneOffset.UTC)),
                "fixed not equals different instant");

        // tick 分钟边界截断
        check(Clock.tick(Clock.fixed(Instant.ofEpochSecond(123), ZoneOffset.UTC),
                Duration.ofSeconds(60)).instant().equals(Instant.ofEpochSecond(120)),
                "tick truncates to minute boundary");
    }

    static void testFixed() {
        Instant instant = Instant.ofEpochSecond(123, 456_000_000);
        Clock fixed = Clock.fixed(instant, ZoneOffset.UTC);
        check(fixed.instant().equals(instant), "fixed instant");
        check(fixed.getZone().equals(ZoneOffset.UTC), "fixed zone");
        check(fixed.millis() == 123_456L, "fixed millis == instant millis");
        check(fixed.millis() == instant.toEpochMilli(), "millis consistent with instant");
        Clock fixed2 = Clock.fixed(instant, ZoneOffset.UTC);
        check(fixed.equals(fixed2), "fixed equals same instant+zone");
        check(!fixed.equals(Clock.fixed(Instant.ofEpochSecond(1), ZoneOffset.UTC)),
                "fixed not equals different instant");
    }

    static void testOffsetAndWithZone() {
        Instant base = Instant.ofEpochSecond(100);
        Clock offset = Clock.offset(Clock.fixed(base, ZoneOffset.UTC), Duration.ofHours(1));
        check(offset.instant().equals(Instant.ofEpochSecond(3700)), "offset adds duration");
        Clock rezoned = Clock.fixed(base, ZoneOffset.UTC).withZone(ZoneOffset.ofHours(2));
        check(rezoned.instant().equals(base), "withZone keeps instant");
        check(rezoned.getZone().equals(ZoneOffset.ofHours(2)), "withZone changes zone");
    }

    static void testTick() {
        Clock base = Clock.fixed(Instant.ofEpochSecond(7), ZoneOffset.UTC);
        Clock tick5 = Clock.tick(base, Duration.ofSeconds(5));
        check(tick5.instant().equals(Instant.ofEpochSecond(5)), "tick truncates down to 5s");
        Clock tickSeconds = Clock.tickSeconds(ZoneOffset.UTC);
        check(tickSeconds.getZone().equals(ZoneOffset.UTC), "tickSeconds zone");
        Clock tickMinutes = Clock.tickMinutes(ZoneOffset.UTC);
        check(tickMinutes.getZone().equals(ZoneOffset.UTC), "tickMinutes zone");
        Clock tickMillis = Clock.tickMillis(ZoneOffset.UTC);
        check(tickMillis.getZone().equals(ZoneOffset.UTC), "tickMillis zone");
        check(Clock.tick(Clock.fixed(Instant.ofEpochSecond(59, 999_999_999), ZoneOffset.UTC),
                Duration.ofSeconds(30)).instant().equals(Instant.ofEpochSecond(30)),
                "tick truncates to 30s boundary");
    }

    static void testSystemClocks() {
        Clock sysUtc = Clock.systemUTC();
        check(sysUtc.getZone().equals(ZoneOffset.UTC), "systemUTC zone");
        check(sysUtc.instant() != null, "systemUTC instant non-null");
        Clock sysDef = Clock.systemDefaultZone();
        check(sysDef.getZone() != null, "systemDefaultZone non-null");
        Clock sysZone = Clock.system(ZoneId.of("UTC"));
        check(sysZone.getZone().equals(ZoneId.of("UTC")), "system(zone) zone");
    }

    static void testErrors() {
        expectThrows(IllegalArgumentException.class,
                () -> Clock.tick(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), Duration.ofNanos(999)),
                "tick fractional nanos not dividing second");
        expectThrows(IllegalArgumentException.class,
                () -> Clock.tick(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), Duration.ofSeconds(-1)),
                "tick negative duration");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepClock();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== contract deepening harness (appended) ====
    interface DeepRun { void run() throws Throwable; }

    static void xact(DeepRun r, Class<?> type, String exactMsg, String tag) {
        try {
            r.run();
            throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but nothing was thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but got "
                        + t.getClass().getName() + ": " + t.getMessage(), t);
            if (exactMsg != null && !exactMsg.equals(t.getMessage()))
                throw new AssertionError(tag + ": " + type.getSimpleName() + " message <" + t.getMessage()
                        + "> want <" + exactMsg + ">");
            passed++;
        }
    }

    static void xeq(Object got, Object want, String tag) {
        if (got == null ? want != null : !got.equals(want))
            throw new AssertionError(tag + ": got <" + got + "> want <" + want + ">");
        passed++;
    }


    static void testDeepClock() {
        Instant fixed = Instant.ofEpochSecond(1709206530, 700000000);
        ZoneId z1 = ZoneOffset.ofHours(1);
        Clock c = Clock.fixed(fixed, z1);
        check(c.instant().equals(fixed), "fixed instant");
        check(c.millis() == 1709206530700L, "fixed millis");
        check(c.getZone().equals(z1), "fixed zone");
        check(c.instant().equals(c.instant()), "fixed is stable");
        Clock tickQ = Clock.tick(Clock.fixed(fixed, ZoneOffset.UTC), Duration.ofMillis(250));
        xeq(tickQ.instant().toString(), "2024-02-29T11:35:30.500Z", "tick 250ms floors");
        xeq(Clock.tick(Clock.fixed(fixed, ZoneOffset.UTC), Duration.ofHours(1)).instant().toString(),
            "2024-02-29T11:00:00Z", "tickHours");
        xeq(Clock.tick(Clock.fixed(fixed, ZoneOffset.UTC), Duration.ofSeconds(60)).instant().toString(),
            "2024-02-29T11:35:00Z", "tickMinutes");
        check(Clock.tickSeconds(ZoneOffset.UTC).instant().getNano() == 0, "tickSeconds floors nanos");
        check(Clock.tickMinutes(ZoneOffset.UTC).instant().getNano() == 0
            && Clock.tickMinutes(ZoneOffset.UTC).instant().getEpochSecond() % 60 == 0, "tickMinutes floors");
        Clock off = Clock.offset(Clock.fixed(fixed, ZoneOffset.UTC), Duration.ofHours(1));
        xeq(off.instant().toString(), "2024-02-29T12:35:30.700Z", "offset adds duration");
        Clock offNeg = Clock.offset(Clock.fixed(fixed, ZoneOffset.UTC), Duration.ofMinutes(-60));
        xeq(offNeg.instant().toString(), "2024-02-29T10:35:30.700Z", "offset subtracts");
        check(Clock.systemUTC().instant().isAfter(Instant.ofEpochSecond(1700000000)), "systemUTC monotonic-ish");
        check(Clock.system(ZoneOffset.UTC).getZone().equals(ZoneOffset.UTC), "system zone");
        check(Clock.systemDefaultZone().getZone().equals(ZoneId.systemDefault()), "systemDefaultZone zone");
        long m1 = Clock.systemUTC().millis();
        check(Clock.systemUTC().millis() - m1 >= 0, "millis monotonic");
        xeq(c.withZone(ZoneOffset.ofHours(2)).getZone().toString(), "+02:00", "withZone");
        check(c.withZone(z1) == c, "withZone same returns this");
        xact(() -> Clock.fixed(null, z1), NullPointerException.class, "fixedInstant", "fixed null instant");
        xact(() -> Clock.fixed(fixed, null), NullPointerException.class, "zone", "fixed null zone");
        xact(() -> Clock.tick(null, Duration.ofSeconds(1)), NullPointerException.class, "baseClock", "tick null clock");
        xact(() -> Clock.tick(c, null), NullPointerException.class, "tickDuration", "tick null duration");
        check(Clock.tick(c, Duration.ofSeconds(0)).instant().equals(c.instant()), "tick zero duration is identity");
        xact(() -> Clock.tick(c, Duration.ofSeconds(-1)), IllegalArgumentException.class,
            "Tick duration must not be negative", "tick negative");
        xact(() -> Clock.offset(null, Duration.ofSeconds(1)), NullPointerException.class, "baseClock", "offset null clock");
        check(!c.instant().equals(Clock.offset(c, Duration.ofSeconds(1)).instant()), "offset differs");
        check(c.equals(Clock.fixed(fixed, z1)), "fixed equals same instant+zone");
        check(c.hashCode() == Clock.fixed(fixed, z1).hashCode(), "fixed hashCode");
        check(!c.equals(Clock.fixed(fixed, ZoneOffset.UTC)), "fixed differs by zone");
        check(Clock.tickMillis(ZoneOffset.UTC).getZone().equals(ZoneOffset.UTC), "tickMillis zone");
    }

}
