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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        testDeepPatternProvider();
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


    static void testDeepPatternProvider() {
        // behavioral: concrete SPI returns usable localized patterns; feed them back into ofPattern
        DateTimeFormatterPatternProvider p = new DateTimeFormatterPatternProvider() {
            public String getDateTimeFormatterPattern(FormatStyle dateStyle, FormatStyle timeStyle, String calendarType, java.util.Locale locale) {
                if (dateStyle == FormatStyle.MEDIUM && timeStyle == FormatStyle.MEDIUM && calendarType.equals("iso8601"))
                    return "MMM d, yyyy h:mm:ss a";
                return "yyyy-MM-dd";
            }
            public String getDateTimeFormatterPattern(String dateStyle, String timeStyle, java.util.Locale locale) {
                return "d/M/yyyy";
            }
            public java.util.Locale[] getAvailableLocales() {
                return new java.util.Locale[] { java.util.Locale.ENGLISH };
            }
        };
        java.util.Locale en = java.util.Locale.ENGLISH;
        xeq(p.getDateTimeFormatterPattern(FormatStyle.MEDIUM, FormatStyle.MEDIUM, "iso8601", en), "MMM d, yyyy h:mm:ss a", "4-arg SPI");
        xeq(p.getDateTimeFormatterPattern("medium", "medium", en), "d/M/yyyy", "3-arg SPI");
        DateTimeFormatter f = DateTimeFormatter.ofPattern(p.getDateTimeFormatterPattern(FormatStyle.MEDIUM, FormatStyle.MEDIUM, "iso8601", en), en);
        xeq(LocalDateTime.of(2024, 2, 29, 10, 15, 30).format(f), "Feb 29, 2024 10:15:30 AM", "pattern round trip via ofPattern");
        DateTimeFormatter f2 = DateTimeFormatter.ofPattern(p.getDateTimeFormatterPattern("medium", "medium", en), en);
        xeq(LocalDate.of(2024, 2, 29).format(f2), "29/2/2024", "3-arg pattern formats");
        check(p instanceof java.util.spi.LocaleServiceProvider, "SPI base");
        check(p.getDateTimeFormatterPattern(null, "medium", en).equals("d/M/yyyy"), "null dateStyle falls to default branch in impl");
        check(java.util.Arrays.toString(p.getAvailableLocales()).equals("[en]"), "available locales from impl");
    }

}
