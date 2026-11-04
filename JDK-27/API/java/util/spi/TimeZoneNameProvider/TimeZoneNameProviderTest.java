/*
 * @test
 * @summary java.util.spi.TimeZoneNameProvider conformance: white-box structure + black-box behavior.
 * @run main TimeZoneNameProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.TimeZone;
import java.util.spi.LocaleServiceProvider;
import java.util.spi.TimeZoneNameProvider;

public class TimeZoneNameProviderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.spi.TimeZoneNameProvider");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()),
              "public abstract class");
        check(c.getSuperclass() == LocaleServiceProvider.class, "extends LocaleServiceProvider");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected ctor");

        Method dn = c.getMethod("getDisplayName", String.class, boolean.class, int.class, Locale.class);
        check(Modifier.isAbstract(dn.getModifiers()) && dn.getReturnType() == String.class,
              "getDisplayName abstract String");
        Method gn = c.getMethod("getGenericDisplayName", String.class, int.class, Locale.class);
        check(!Modifier.isAbstract(gn.getModifiers()) && gn.getReturnType() == String.class,
              "getGenericDisplayName concrete String");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TimeZoneNameProvider p = new TimeZoneNameProvider() {
            public Locale[] getAvailableLocales() { return new Locale[]{Locale.US}; }
            public String getDisplayName(String ID, boolean daylight, int style, Locale l) {
                return ID + "|" + daylight + "|" + style;
            }
        };

        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check("America/New_York|false|1".equals(
                p.getDisplayName("America/New_York", false, TimeZone.LONG, Locale.US)),
              "getDisplayName standard LONG round-trip");
        check("America/New_York|true|1".equals(
                p.getDisplayName("America/New_York", true, TimeZone.LONG, Locale.US)),
              "getDisplayName daylight LONG round-trip");
        check("America/New_York|false|0".equals(
                p.getDisplayName("America/New_York", false, TimeZone.SHORT, Locale.US)),
              "getDisplayName standard SHORT round-trip");
        check(p.getGenericDisplayName("America/New_York", TimeZone.LONG, Locale.US) == null,
              "getGenericDisplayName default null");
    }


    // ===================== 契约深化：匿名实现两种 style + null NPE =====================
    static void testContracts() {
        final Locale[] locs = {Locale.JAPAN};
        TimeZoneNameProvider p = new TimeZoneNameProvider() {
            public Locale[] getAvailableLocales() { return locs; }
            public String getDisplayName(String id, boolean daylight, int style, Locale locale) {
                return (daylight ? "DST-" : "STD-") + (style == java.util.TimeZone.LONG ? "LONG-" : "SHORT-") + id; }
        };
        check("STD-LONG-Asia/Tokyo".equals(
                p.getDisplayName("Asia/Tokyo", false, java.util.TimeZone.LONG, Locale.JAPAN)),
              "getDisplayName LONG standard behavior");
        check("DST-SHORT-Asia/Tokyo".equals(
                p.getDisplayName("Asia/Tokyo", true, java.util.TimeZone.SHORT, Locale.JAPAN)),
              "getDisplayName SHORT daylight behavior");
        // 自有实现：null 实参直通（NPE 属内建 provider 语义）
        check(("STD-LONG-null").equals(
                p.getDisplayName(null, false, java.util.TimeZone.LONG, Locale.JAPAN)), "own impl handles null id");
        check(p.getDisplayName("Asia/Tokyo", false, java.util.TimeZone.LONG, null) != null,
              "own impl handles null locale");
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
