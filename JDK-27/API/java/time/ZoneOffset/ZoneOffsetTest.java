/*
 * @test
 * @summary java.time.ZoneOffset conformance: white-box shape + black-box factories,
 *          total seconds, getId/normalized/rules, comparisons, and range errors.
 * @run main ZoneOffsetTest
 *
 * 设计案（黑盒）：
 * 1. 契约：ZoneOffset 是距 UTC 的固定偏移（-18:00..+18:00，秒精度），是 ZoneId 子类，不可变。
 * 2. 场景：ofHours/ofHoursMinutes/ofHoursMinutesSeconds/of(String)/ofTotalSeconds 构造，getTotalSeconds/getId/getRules 查询。
 * 3. 组合链路：ofHoursMinutes -> getTotalSeconds -> ofTotalSeconds 往返 -> getId -> getRules().getOffset == 自身 -> compareTo。
 * 4. 不变量：id 为 "+HH:MM[:SS]" 或 "Z"；normalized 返回自身；compareTo 按总秒数降序。
 * 5. 边界/异常：越界（|offset|>18:00 或缺分钟）抛 DateTimeException。
 */

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;

public class ZoneOffsetTest {
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
        Class<?> c = Class.forName("java.time.ZoneOffset");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == ZoneId.class, "superclass ZoneId");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.time.temporal.TemporalAccessor.class.isAssignableFrom(c), "TemporalAccessor");
        check(java.time.temporal.TemporalAdjuster.class.isAssignableFrom(c), "TemporalAdjuster");
        check(java.lang.Comparable.class.isAssignableFrom(c), "Comparable");
    }

    static void testBlackBox() {
        testFactories();
        testGettersAndRules();
        testComparison();
        testErrors();

        // ---- 边界值/等价类/等价性（本棒补测） ----
        check(ZoneOffset.MIN.getTotalSeconds() == -64800, "MIN totalSeconds");
        check(ZoneOffset.MAX.getTotalSeconds() == 64800, "MAX totalSeconds");
        check(ZoneOffset.ofTotalSeconds(64800).getId().equals("+18:00"), "ofTotalSeconds MAX id");
        check(ZoneOffset.ofTotalSeconds(-64800).getId().equals("-18:00"), "ofTotalSeconds MIN id");

        // round-trip：of(id) == ofTotalSeconds(totalSeconds)
        for (int sec : new int[]{0, 1, -1, 19800, -19800, 64800, -64800}) {
            ZoneOffset z = ZoneOffset.ofTotalSeconds(sec);
            check(ZoneOffset.of(z.getId()).equals(z), "of(id) round-trip " + sec);
        }

        // getRules 恒等
        ZoneOffset o = ZoneOffset.ofHours(3);
        check(o.getRules().getOffset(Instant.EPOCH).equals(o), "rules epoch offset");

        // compareTo 反对称
        ZoneOffset za = ZoneOffset.ofHours(1);
        ZoneOffset zb = ZoneOffset.ofHours(2);
        check(za.compareTo(zb) == -zb.compareTo(za), "compareTo anti-symmetry");

        // ofTotalSeconds 越界
        expectThrows(DateTimeException.class, () -> ZoneOffset.ofTotalSeconds(64801),
                "ofTotalSeconds above MAX");
        expectThrows(DateTimeException.class, () -> ZoneOffset.ofTotalSeconds(-64801),
                "ofTotalSeconds below MIN");
    }

    static void testFactories() {
        check(ZoneOffset.ofHours(5).getTotalSeconds() == 5 * 3600, "ofHours");
        check(ZoneOffset.ofHours(-3).getTotalSeconds() == -3 * 3600, "ofHours negative");
        check(ZoneOffset.ofHoursMinutes(5, 30).getTotalSeconds() == 19800, "ofHoursMinutes");
        check(ZoneOffset.ofHoursMinutes(-5, -30).getTotalSeconds() == -19800, "ofHoursMinutes negative");
        check(ZoneOffset.ofHoursMinutesSeconds(1, 2, 3).getTotalSeconds() == 3723,
                "ofHoursMinutesSeconds");
        check(ZoneOffset.of("+05:00").getTotalSeconds() == 18000, "of(+05:00)");
        check(ZoneOffset.of("-05:30").getTotalSeconds() == -19800, "of(-05:30)");
        check(ZoneOffset.of("+05:00:30").getTotalSeconds() == 18030, "of with seconds");
        check(ZoneOffset.of("Z").equals(ZoneOffset.UTC), "of Z equals UTC");
        check(ZoneOffset.ofTotalSeconds(18000).getId().equals("+05:00"), "ofTotalSeconds roundtrip");
        check(ZoneOffset.ofTotalSeconds(-19800).getId().equals("-05:30"), "ofTotalSeconds negative");
        check(ZoneOffset.ofTotalSeconds(0).equals(ZoneOffset.UTC), "ofTotalSeconds 0 = UTC");
    }

    static void testGettersAndRules() {
        ZoneOffset o = ZoneOffset.ofHours(2);
        check(o.getId().equals("+02:00"), "getId");
        check(o.normalized() == o, "normalized returns self");
        check(o.getRules().getOffset(Instant.parse("2024-03-01T00:00:00Z")).equals(o),
                "rules offset is itself");
        check(o.getRules().isDaylightSavings(Instant.parse("2024-07-15T00:00:00Z")) == false,
                "fixed offset never DST");
        check(ZoneOffset.UTC.getId().equals("Z"), "UTC id Z");
        check(ZoneOffset.MIN.getTotalSeconds() == -64800, "MIN = -18:00");
        check(ZoneOffset.MAX.getTotalSeconds() == 64800, "MAX = +18:00");
    }

    static void testComparison() {
        ZoneOffset a = ZoneOffset.ofHours(1);
        ZoneOffset b = ZoneOffset.ofHours(2);
        ZoneOffset a2 = ZoneOffset.ofHours(1);
        check(a.compareTo(b) > 0 && b.compareTo(a) < 0 && a.compareTo(a2) == 0,
                "compareTo descending by total seconds");
        check(a.equals(a2) && a.hashCode() == a2.hashCode(), "equals/hashCode");
        check(!a.equals(b), "equals different");
    }

    static void testErrors() {
        expectThrows(DateTimeException.class, () -> ZoneOffset.ofHours(19), "ofHours 19 out of range");
        expectThrows(DateTimeException.class, () -> ZoneOffset.ofHours(-19), "ofHours -19 out of range");
        expectThrows(DateTimeException.class, () -> ZoneOffset.of("+18:01"), "of +18:01 out of range");
        expectThrows(DateTimeException.class, () -> ZoneOffset.of("05:00"), "of missing sign");
        expectThrows(DateTimeException.class, () -> ZoneOffset.ofHoursMinutes(0, 60), "ofHoursMinutes 60 min");
        expectThrows(DateTimeException.class, () -> ZoneOffset.ofHoursMinutesSeconds(0, 0, 60),
                "ofHoursMinutesSeconds 60 sec");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepZoneOffset();
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


    static void testDeepZoneOffset() {
        check(ZoneOffset.UTC == ZoneOffset.of("+00:00") && ZoneOffset.UTC.getId().equals("Z"), "UTC==of(+00:00), id Z");
        xeq(ZoneOffset.of("Z").toString(), "Z", "of Z");
        xeq(ZoneOffset.of("+01:00").toString(), "+01:00", "of +01:00");
        xeq(ZoneOffset.of("-0530").toString(), "-05:30", "of -0530 normalized");
        xeq(ZoneOffset.of("+010105").toString(), "+01:01:05", "of +010105");
        check(ZoneOffset.of("+010105").getTotalSeconds() == 3665, "totalSeconds 3665");
        check(ZoneOffset.of("-0530").getTotalSeconds() == -19800, "totalSeconds -19800");
        xeq(ZoneOffset.ofHours(1).toString(), "+01:00", "ofHours");
        xeq(ZoneOffset.ofHoursMinutes(-1, -30).toString(), "-01:30", "ofHoursMinutes neg");
        xeq(ZoneOffset.ofHoursMinutesSeconds(0, 0, 1).toString(), "+00:00:01", "ofHMS seconds");
        xeq(ZoneOffset.ofTotalSeconds(3665).toString(), "+01:01:05", "ofTotalSeconds");
        xeq(ZoneOffset.ofTotalSeconds(-1).toString(), "-00:00:01", "ofTotalSeconds -1");
        xeq(ZoneOffset.MAX.toString(), "+18:00", "MAX");
        xeq(ZoneOffset.MIN.toString(), "-18:00", "MIN");
        check(ZoneOffset.MAX.getTotalSeconds() == 64800, "MAX secs");
        check(ZoneOffset.MIN.getTotalSeconds() == -64800, "MIN secs");
        xact(() -> ZoneOffset.of("+19:00"), DateTimeException.class,
            "Zone offset hours not in valid range: value 19 is not in the range -18 to 18", "of +19");
        xact(() -> ZoneOffset.of("+18:00:01"), DateTimeException.class,
            "Zone offset not in valid range: -18:00 to +18:00", "of +18:00:01");
        xact(() -> ZoneOffset.of("ABC"), DateTimeException.class,
            "Invalid ID for ZoneOffset, non numeric characters found: ABC", "of ABC");
        xact(() -> ZoneOffset.of(""), DateTimeException.class,
            "Invalid ID for ZoneOffset, invalid format: ", "of empty");
        xact(() -> ZoneOffset.of(null), NullPointerException.class, null, "of null");
        xact(() -> ZoneOffset.ofTotalSeconds(64801), DateTimeException.class,
            "Zone offset not in valid range: -18:00 to +18:00", "ofTotalSeconds 64801");
        xact(() -> ZoneOffset.ofTotalSeconds(-64801), DateTimeException.class,
            "Zone offset not in valid range: -18:00 to +18:00", "ofTotalSeconds -64801");
        xact(() -> ZoneOffset.ofHours(19), DateTimeException.class,
            "Zone offset hours not in valid range: value 19 is not in the range -18 to 18", "ofHours 19");
        xact(() -> ZoneOffset.ofHoursMinutes(19, 0), DateTimeException.class,
            "Zone offset hours not in valid range: value 19 is not in the range -18 to 18", "ofHM 19h");
        xact(() -> ZoneOffset.ofHoursMinutes(0, -60), DateTimeException.class,
            "Zone offset minutes not in valid range: value -60 is not in the range -59 to 59", "ofHM -60m");
        xact(() -> ZoneOffset.ofHoursMinutesSeconds(0, 0, 60), DateTimeException.class,
            "Zone offset seconds not in valid range: value 60 is not in the range -59 to 59", "ofHMS 60s");
        ZoneOffset z = ZoneOffset.ofHours(2);
        check(z.get(ChronoField.OFFSET_SECONDS) == 7200, "get OFFSET_SECONDS");
        check(z.getLong(ChronoField.OFFSET_SECONDS) == 7200L, "getLong");
        xeq(z.range(ChronoField.OFFSET_SECONDS).toString(), "-64800 - 64800", "range");
        check(z.query(TemporalQueries.offset()) == z, "query offset self");
        check(z.query(TemporalQueries.zoneId()) == null, "query zoneId null for offset");
        check(z.isSupported(ChronoField.OFFSET_SECONDS), "supported");
        check(!z.isSupported(ChronoField.YEAR), "not supported YEAR");
        xeq(z.adjustInto(OffsetTime.of(LocalTime.of(10, 0), ZoneOffset.UTC)).toString(), "10:00+02:00", "adjustInto OffsetTime");
        xact(() -> z.adjustInto(LocalTime.of(10, 0)), UnsupportedTemporalTypeException.class,
            "Unsupported field: OffsetSeconds", "adjustInto LocalTime throws");
        check(ZoneOffset.of("+01:00").hashCode() != ZoneOffset.of("+02:00").hashCode(), "hash distinct");
        check(ZoneOffset.of("+01:00").equals(ZoneOffset.ofTotalSeconds(3600)), "equals cross factory");
        check(ZoneOffset.of("+01:00").compareTo(ZoneOffset.of("+02:00")) > 0, "compareTo is descending (larger offset sorts first)");
        check(ZoneOffset.UTC.compareTo(ZoneOffset.MIN) == -64800, "compareTo returns raw seconds difference MIN");
        check(ZoneOffset.ofHours(1).compareTo(ZoneOffset.ofHours(2)) == 3600, "compareTo returns 3600 for 1h vs 2h");
        check(ZoneOffset.ofHours(1).compareTo(ZoneOffset.ofHours(1)) == 0, "compareTo equal 0");
        xeq(ZoneOffset.from(OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.ofHours(5))).toString(), "+05:00", "from");
        xact(() -> ZoneOffset.from(LocalDate.of(2024, 1, 1)), DateTimeException.class,
            "Unable to obtain ZoneOffset from TemporalAccessor: 2024-01-01 of type java.time.LocalDate", "from LocalDate");
    }

}
