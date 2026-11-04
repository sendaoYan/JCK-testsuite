/*
 * @test
 * @summary java.time.format.ResolverStyle API conformance: constants and valueOf.
 * @run main ResolverStyleTest
 */

import java.time.format.ResolverStyle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

public class ResolverStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testResolverStyle() {
        check("STRICT".equals(ResolverStyle.STRICT.name()), "STRICT name");
        check("LENIENT".equals(ResolverStyle.LENIENT.name()), "LENIENT name");
        check(ResolverStyle.valueOf("SMART") == ResolverStyle.SMART, "valueOf SMART");
    }

    public static void main(String[] args) {
        testResolverStyle();
        testDeepResolverStyle();
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


    static void testDeepResolverStyle() {
        DateTimeFormatter strict = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter smart = DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.SMART);
        DateTimeFormatter lenient = DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.LENIENT);
        xeq(LocalDate.parse("2023-02-28", strict).toString(), "2023-02-28", "strict ok");
        xact(() -> LocalDate.parse("2023-02-29", strict), DateTimeParseException.class,
            "Text '2023-02-29' could not be parsed: Invalid date 'February 29' as '2023' is not a leap year", "strict rejects");
        xeq(LocalDate.parse("2023-02-29", smart).toString(), "2023-02-28", "smart clamps to month end");
        xeq(LocalDate.parse("2023-02-29", lenient).toString(), "2023-03-01", "lenient rolls to March");
        xact(() -> LocalDate.parse("2024-04-31", strict), DateTimeParseException.class,
            "Text '2024-04-31' could not be parsed: Invalid date 'APRIL 31'", "strict Apr 31");
        xeq(LocalDate.parse("2024-04-31", smart).toString(), "2024-04-30", "smart Apr 31 clamps");
        xeq(LocalDate.parse("2024-04-31", lenient).toString(), "2024-05-01", "lenient Apr 31 rolls");
        DateTimeFormatter moStrict = new DateTimeFormatterBuilder().appendValue(ChronoField.MONTH_OF_YEAR).toFormatter();
        check(moStrict.parseUnresolved("13", new java.text.ParsePosition(0)).getLong(ChronoField.MONTH_OF_YEAR) == 13L,
            "parseUnresolved keeps raw 13 without resolving");
        check(smart.getResolverStyle() == ResolverStyle.SMART, "getResolverStyle");
        check(lenient.getResolverStyle() == ResolverStyle.LENIENT, "lenient style");
        check(DateTimeFormatter.ISO_LOCAL_DATE.getResolverStyle() == ResolverStyle.STRICT, "ISO default STRICT");
        check(ResolverStyle.values().length == 3, "3 styles");
        check(ResolverStyle.SMART.toString().equals("SMART"), "toString");
        check(java.util.EnumSet.allOf(ResolverStyle.class).contains(ResolverStyle.LENIENT), "enum set");
    }

}
