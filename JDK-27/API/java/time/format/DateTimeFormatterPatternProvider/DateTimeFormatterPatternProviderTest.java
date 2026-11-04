/*
 * @test
 * @summary java.time.format.DateTimeFormatterPatternProvider conformance: white-box structure + black-box behavior.
 * @run main DateTimeFormatterPatternProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.format.DateTimeFormatterPatternProvider;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public class DateTimeFormatterPatternProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.format.DateTimeFormatterPatternProvider");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == LocaleServiceProvider.class, "extends LocaleServiceProvider");

        boolean hasFs = false;
        boolean hasStr = false;
        for (Method m : c.getDeclaredMethods()) {
            if (!m.getName().equals("getDateTimeFormatterPattern")) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p.length == 4 && p[0] == FormatStyle.class && p[1] == FormatStyle.class
                    && p[2] == String.class && p[3] == Locale.class) hasFs = true;
            if (p.length == 3 && p[0] == String.class && p[1] == String.class
                    && p[2] == Locale.class) hasStr = true;
        }
        check(hasFs, "getDateTimeFormatterPattern(FormatStyle, FormatStyle, String, Locale) present");
        check(hasStr, "getDateTimeFormatterPattern(String, String, Locale) present");
    }

    // A minimal concrete SPI implementation that returns fixed, deterministic patterns.
    static class TestProvider extends DateTimeFormatterPatternProvider {
        @Override
        public Locale[] getAvailableLocales() {
            return new Locale[] { Locale.US };
        }

        @Override
        public String getDateTimeFormatterPattern(FormatStyle dateStyle, FormatStyle timeStyle,
                                                  String chronologyId, Locale locale) {
            return "FS-PATTERN";
        }

        @Override
        public String getDateTimeFormatterPattern(String requestedTemplate,
                                                  String chronologyId, Locale locale) {
            return "STR-PATTERN";
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TestProvider p = new TestProvider();
        check(p.getAvailableLocales().length == 1, "one available locale");
        check(p.getAvailableLocales()[0].equals(Locale.US), "available locale is US");
        check(p.isSupportedLocale(Locale.US), "US supported");
        check(!p.isSupportedLocale(Locale.FRANCE), "FRANCE not supported");
        check("FS-PATTERN".equals(
                p.getDateTimeFormatterPattern(FormatStyle.FULL, FormatStyle.FULL,
                                              "ISO", Locale.US)),
              "format-style pattern override");
        check("STR-PATTERN".equals(
                p.getDateTimeFormatterPattern("yyyy-MM-dd", "ISO", Locale.US)),
              "string-template pattern override");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
