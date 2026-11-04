/*
 * @test
 * @summary java.util.spi.CalendarDataProvider conformance: white-box structure + black-box behavior.
 * @run main CalendarDataProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.Locale;
import java.util.spi.CalendarDataProvider;
import java.util.spi.LocaleServiceProvider;

public class CalendarDataProviderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.spi.CalendarDataProvider");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()),
              "public abstract class");
        check(c.getSuperclass() == LocaleServiceProvider.class, "extends LocaleServiceProvider");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected ctor");

        Method fd = c.getMethod("getFirstDayOfWeek", Locale.class);
        check(Modifier.isAbstract(fd.getModifiers()) && fd.getReturnType() == int.class,
              "getFirstDayOfWeek abstract int");
        Method md = c.getMethod("getMinimalDaysInFirstWeek", Locale.class);
        check(Modifier.isAbstract(md.getModifiers()) && md.getReturnType() == int.class,
              "getMinimalDaysInFirstWeek abstract int");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CalendarDataProvider p = new CalendarDataProvider() {
            public Locale[] getAvailableLocales() { return new Locale[]{Locale.US, Locale.FRANCE}; }
            public int getFirstDayOfWeek(Locale l) {
                return Locale.US.equals(l) ? Calendar.SUNDAY : Calendar.MONDAY;
            }
            public int getMinimalDaysInFirstWeek(Locale l) {
                return Locale.US.equals(l) ? 4 : 1;
            }
        };

        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getFirstDayOfWeek(Locale.US) == Calendar.SUNDAY, "getFirstDayOfWeek US == SUNDAY");
        check(p.getFirstDayOfWeek(Locale.US) == 1, "getFirstDayOfWeek US == 1");
        check(p.getFirstDayOfWeek(Locale.FRANCE) == Calendar.MONDAY, "getFirstDayOfWeek FR == MONDAY");
        check(p.getMinimalDaysInFirstWeek(Locale.US) == 4, "getMinimalDaysInFirstWeek US == 4");
        check(p.getMinimalDaysInFirstWeek(Locale.FRANCE) == 1, "getMinimalDaysInFirstWeek FR == 1");
    }


    // ===================== 契约深化：匿名实现 firstDayOfWeek/minimalDaysInWeek + null NPE =====================
    static void testContracts() {
        final Locale[] locs = {new Locale("th", "TH")};
        CalendarDataProvider p = new CalendarDataProvider() {
            public Locale[] getAvailableLocales() { return locs; }
            public int getFirstDayOfWeek(Locale locale) { return java.util.Calendar.SUNDAY; }
            public int getMinimalDaysInFirstWeek(Locale locale) { return 4; }
        };
        check(p.getFirstDayOfWeek(new Locale("th", "TH")) == java.util.Calendar.SUNDAY,
              "getFirstDayOfWeek override behavior");
        check(p.getMinimalDaysInFirstWeek(new Locale("th", "TH")) == 4,
              "getMinimalDaysInFirstWeek override behavior");
        // 自有实现：null 实参不抛（返回常量）
        check(p.getFirstDayOfWeek(null) == java.util.Calendar.SUNDAY, "own impl tolerates null locale");
        check(p.getMinimalDaysInFirstWeek(null) == 4, "own impl tolerates null locale (minDays)");
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
