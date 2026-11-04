/*
 * @test
 * @summary java.text.spi.DateFormatProvider conformance: white-box hierarchy + black-box anonymous-subclass factory behavior.
 * @run main DateFormatProviderTest
 */

import java.text.DateFormat;
import java.text.spi.DateFormatProvider;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public class DateFormatProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.spi.DateFormatProvider");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "abstract class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.util.spi.LocaleServiceProvider"), "extends LocaleServiceProvider");
        check(c.getConstructors().length == 0, "no public constructor");
        check(c.getMethod("getTimeInstance", int.class, Locale.class) != null, "getTimeInstance(int,Locale)");
        check(c.getMethod("getDateInstance", int.class, Locale.class) != null, "getDateInstance(int,Locale)");
        check(c.getMethod("getDateTimeInstance", int.class, int.class, Locale.class) != null,
              "getDateTimeInstance(int,int,Locale)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DateFormatProvider p = new DateFormatProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public DateFormat getTimeInstance(int style, Locale l) {
                return DateFormat.getTimeInstance(style, Locale.US);
            }
            public DateFormat getDateInstance(int style, Locale l) {
                return DateFormat.getDateInstance(style, Locale.US);
            }
            public DateFormat getDateTimeInstance(int ds, int ts, Locale l) {
                return DateFormat.getDateTimeInstance(ds, ts, Locale.US);
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getAvailableLocales().length == 1 && p.getAvailableLocales()[0] == Locale.US,
              "getAvailableLocales");
        check(p.isSupportedLocale(Locale.US), "isSupportedLocale US");
        check(!p.isSupportedLocale(Locale.FRANCE), "isSupportedLocale FRANCE false");
        check(p.getTimeInstance(DateFormat.SHORT, Locale.US) != null, "getTimeInstance");
        check(p.getDateInstance(DateFormat.SHORT, Locale.US) != null, "getDateInstance");
        check(p.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US) != null,
              "getDateTimeInstance");
        check(p.getDateInstance(DateFormat.SHORT, Locale.US) instanceof DateFormat, "factory returns DateFormat");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
