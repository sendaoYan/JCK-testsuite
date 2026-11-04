/*
 * @test
 * @summary java.time.format.SignStyle API conformance: constants and valueOf.
 * @run main SignStyleTest
 */

import java.time.format.SignStyle;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class SignStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSignStyle() {
        check("NORMAL".equals(SignStyle.NORMAL.name()), "NORMAL name");
        check(SignStyle.valueOf("ALWAYS") == SignStyle.ALWAYS, "valueOf ALWAYS");
        check(SignStyle.valueOf("EXCEEDS_PAD") == SignStyle.EXCEEDS_PAD,
              "valueOf EXCEEDS_PAD");
    }

    public static void main(String[] args) {
        testSignStyle();
        testDeepSignStyle();
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


    static void testDeepSignStyle() {
        LocalDate d = LocalDate.of(2024, 2, 29);
        xeq(java.util.Arrays.toString(SignStyle.values()), "[NORMAL, ALWAYS, NEVER, NOT_NEGATIVE, EXCEEDS_PAD]", "5 constants order");
        DateTimeFormatter always = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.ALWAYS).toFormatter();
        xeq(d.format(always), "+2024", "ALWAYS +");
        xeq(LocalDate.of(-44, 1, 1).format(always), "-0044", "ALWAYS -");
        DateTimeFormatter normal = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.NORMAL).toFormatter();
        xeq(d.format(normal), "2024", "NORMAL unsigned positive");
        xeq(LocalDate.of(-44, 1, 1).format(normal), "-0044", "NORMAL signed negative");
        DateTimeFormatter never = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR_OF_ERA, 4, 10, SignStyle.NEVER).toFormatter();
        xeq(LocalDate.of(-500, 1, 1).format(never), "0501", "NEVER era year of BCE");
        DateTimeFormatter notNeg = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.NOT_NEGATIVE).toFormatter();
        xeq(d.format(notNeg), "2024", "NOT_NEGATIVE positive");
        xact(() -> LocalDate.of(-1, 1, 1).format(notNeg), DateTimeException.class,
            "Field Year cannot be printed as the value -1 cannot be negative according to the SignStyle", "NOT_NEGATIVE rejects negative");
        DateTimeFormatter exceeds = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 5, 10, SignStyle.EXCEEDS_PAD).toFormatter();
        xeq(d.format(exceeds), "02024", "EXCEEDS_PAD pads to min width, no sign");
        xeq(LocalDate.of(12345, 1, 1).format(exceeds), "12345", "EXCEEDS_PAD at width stays unsigned");
        xeq(LocalDate.of(123456, 1, 1).format(exceeds), "+123456", "EXCEEDS_PAD over width signs");
        DateTimeFormatter exceeds2 = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).toFormatter();
        xeq(LocalDate.of(12345, 1, 1).format(exceeds2), "+12345", "EXCEEDS_PAD signs when digits exceed min width");
        DateTimeFormatter ep = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 5, 10, SignStyle.EXCEEDS_PAD).toFormatter();
        check(ep.parse("12345").getLong(ChronoField.YEAR) == 12345L, "EXCEEDS_PAD parse takes unsigned at pad width");
        check(SignStyle.valueOf("NORMAL") == SignStyle.NORMAL, "valueOf");
        check(SignStyle.NORMAL.ordinal() == 0 && SignStyle.EXCEEDS_PAD.ordinal() == 4, "ordinals");
    }

}
