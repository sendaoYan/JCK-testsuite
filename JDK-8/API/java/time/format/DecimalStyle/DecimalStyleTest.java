/*
 * @test
 * @summary java.time.format.DecimalStyle conformance: STANDARD separators and signs.
 * @run main DecimalStyleTest
 */

import java.time.format.DecimalStyle;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class DecimalStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDecimalStyle() {
        DecimalStyle ds = DecimalStyle.STANDARD;
        check(ds.getZeroDigit() == '0', "zeroDigit");
        check(ds.getPositiveSign() == '+', "positiveSign");
        check(ds.getNegativeSign() == '-', "negativeSign");
        check(ds.getDecimalSeparator() == '.', "decimalSeparator");
        DecimalStyle alt = ds.withDecimalSeparator(',');
        check(alt.getDecimalSeparator() == ',', "withDecimalSeparator");
        check(ds.getDecimalSeparator() == '.', "original unchanged");
        check(ds.equals(DecimalStyle.STANDARD), "equals STANDARD");
        check(ds.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testDecimalStyle();
        testDeepDecimalStyle();
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


    static void testDeepDecimalStyle() {
        DecimalStyle std = DecimalStyle.STANDARD;
        check(std.getZeroDigit() == '0' && std.getPositiveSign() == '+' && std.getNegativeSign() == '-'
            && std.getDecimalSeparator() == '.', "standard chars");
        xeq(std.toString(), "DecimalStyle[0+-.]", "standard toString");
        DecimalStyle alt = std.withZeroDigit('a').withPositiveSign('p').withNegativeSign('n').withDecimalSeparator(';');
        check(alt.getZeroDigit() == 'a' && alt.getPositiveSign() == 'p' && alt.getNegativeSign() == 'n'
            && alt.getDecimalSeparator() == ';', "with* chars");
        xeq(alt.toString(), "DecimalStyle[apn;]", "alt toString");
        check(!std.equals(alt), "not equals");
        check(std.equals(DecimalStyle.of(java.util.Locale.US)), "US == STANDARD-ish");
        check(std.hashCode() == DecimalStyle.STANDARD.hashCode(), "hashCode");
        DateTimeFormatter f = new DateTimeFormatterBuilder().appendFraction(ChronoField.NANO_OF_SECOND, 1, 1, true).toFormatter();
        xeq(LocalTime.of(10, 15, 30, 500000000).format(f), ".5", "fraction with standard");
        DateTimeFormatter fa = f.withDecimalStyle(alt);
        xeq(LocalTime.of(10, 15, 30, 500000000).format(fa), ";f", "fraction alt separator and digit alphabet");
        check(DecimalStyle.of(java.util.Locale.FRENCH).getDecimalSeparator() == ',', "FR decimal comma");
        check(DecimalStyle.ofDefaultLocale() != null, "ofDefaultLocale");
        check(std.equals(std.withZeroDigit('0')), "with same is equal");
        xeq(f.withDecimalStyle(alt).getDecimalStyle().toString(), "DecimalStyle[apn;]", "formatter carries style");
    }

}
