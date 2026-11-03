/*
 * @test
 * @summary java.time.ZoneId conformance: white-box shape + black-box of/normalized/equality,
 *          getRules offsets, system default availability, and ofOffset.
 * @run main ZoneIdTest
 *
 * 设计案（黑盒）：
 * 1. 契约：ZoneId 是时区标识符，不可变、可序列化；UTC/Z 等别名为 ZoneOffset.UTC。
 * 2. 场景：of("UTC")/of("Z")/of("Europe/Paris") 构造，normalized 归约，getRules 查偏移，systemDefault/getAvailableZoneIds 探测环境。
 * 3. 组合链路：of -> normalized -> equals(ZoneOffset.UTC) -> getRules().getOffset(instant) -> getAvailableZoneIds 包含 UTC。
 * 4. 不变量：of("Z") 直接为 ZoneOffset.UTC；of("UTC") 是区域，normalized 归约为 ZoneOffset.UTC；equals/hashCode 一致。
 * 5. 边界/异常：of 未知时区抛 ZoneRulesException；of(null) 抛 NullPointerException。
 */

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRulesException;
import java.util.Map;
import java.util.Set;

public class ZoneIdTest {
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
        Class<?> c = Class.forName("java.time.ZoneId");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
    }

    static void testBlackBox() {
        testFactoriesAndEquality();
        testRules();
        testSystemZones();
        testOfOffset();
        testErrors();
    }

    static void testFactoriesAndEquality() {
        ZoneId utc = ZoneId.of("UTC");
        check("UTC".equals(utc.getId()), "of/getId");
        check(ZoneId.of("Z").equals(ZoneOffset.UTC), "of Z equals ZoneOffset.UTC");
        check(!utc.equals(ZoneOffset.UTC), "of UTC is region not offset");
        check(utc.normalized().equals(ZoneOffset.UTC), "normalized UTC -> ZoneOffset.UTC");
        check(ZoneId.of("+02:00").equals(ZoneOffset.ofHours(2)), "of +02:00 is ZoneOffset");
        check(ZoneId.of("Europe/Paris").getId().equals("Europe/Paris"), "of named zone");
        check(ZoneId.of("UTC").equals(ZoneId.of("Z")) == false, "UTC and Z not equal (region vs offset)");
        check(ZoneId.of("Z").hashCode() == ZoneOffset.UTC.hashCode(), "Z hashCode == UTC offset hashCode");
    }

    static void testRules() {
        ZoneId utc = ZoneId.of("UTC");
        check(utc.getRules().getOffset(Instant.parse("2024-03-01T12:00:00Z")).equals(ZoneOffset.UTC),
                "UTC rules offset at instant");
        ZoneId paris = ZoneId.of("Europe/Paris");
        check(paris.getRules() != null, "named zone rules non-null");
        check(paris.getRules().getOffset(Instant.parse("2024-01-15T00:00:00Z")).equals(ZoneOffset.ofHours(1)),
                "Paris winter offset +01:00");
        check(paris.getRules().getOffset(Instant.parse("2024-07-15T00:00:00Z")).equals(ZoneOffset.ofHours(2)),
                "Paris summer offset +02:00");
        check(paris.getRules().isDaylightSavings(Instant.parse("2024-07-15T00:00:00Z")),
                "Paris DST in July");
        check(!paris.getRules().isDaylightSavings(Instant.parse("2024-01-15T00:00:00Z")),
                "Paris no DST in January");
    }

    static void testSystemZones() {
        ZoneId sys = ZoneId.systemDefault();
        check(sys != null && sys.getId() != null && !sys.getId().isEmpty(), "systemDefault non-empty");
        Set<String> ids = ZoneId.getAvailableZoneIds();
        check(ids.contains("UTC") && ids.contains("Europe/Paris") && ids.size() > 100,
                "getAvailableZoneIds has UTC/Paris and many");
    }

    static void testOfOffset() {
        ZoneId gmt1 = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(1));
        check(gmt1.getId().equals("GMT+01:00"), "ofOffset id");
        check(gmt1.normalized().equals(ZoneOffset.ofHours(1)), "ofOffset normalizes");
        Map<String, String> shortIds = ZoneId.SHORT_IDS;
        check(shortIds.containsKey("CST") && "America/Chicago".equals(shortIds.get("CST")),
                "SHORT_IDS CST -> America/Chicago");
        check(ZoneId.of("CST", shortIds).getId().equals("America/Chicago"), "of with alias map");
    }

    static void testErrors() {
        expectThrows(ZoneRulesException.class, () -> ZoneId.of("Not/AZone"), "of unknown zone");
        expectThrows(NullPointerException.class, () -> ZoneId.of(null), "of null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
