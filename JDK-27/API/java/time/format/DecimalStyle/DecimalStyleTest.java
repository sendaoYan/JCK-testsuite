/*
 * @test
 * @summary java.time.format.DecimalStyle conformance: white-box structure + black-box behavior.
 * @run main DecimalStyleTest
 */

import java.lang.reflect.Modifier;
import java.time.format.DecimalStyle;
import java.util.Locale;
import java.util.TreeSet;
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.format.DecimalStyle");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(c.getField("STANDARD") != null, "STANDARD static field present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DecimalStyle ds = DecimalStyle.STANDARD;
        check(ds.getZeroDigit() == '0', "zeroDigit");
        check(ds.getPositiveSign() == '+', "positiveSign");
        check(ds.getNegativeSign() == '-', "negativeSign");
        check(ds.getDecimalSeparator() == '.', "decimalSeparator");

        // DecimalStyle.of(Locale.US) is STANDARD for the fixed US locale.
        DecimalStyle us = DecimalStyle.of(Locale.US);
        check(us.getZeroDigit() == '0', "US zeroDigit");
        check(us.getDecimalSeparator() == '.', "US decimalSeparator");
        check(us.equals(DecimalStyle.STANDARD), "US equals STANDARD");

        // Immutable with* builders leave the original untouched.
        DecimalStyle zeroAlt = ds.withZeroDigit('1');
        check(zeroAlt.getZeroDigit() == '1', "withZeroDigit");
        check(ds.getZeroDigit() == '0', "original zeroDigit unchanged");

        DecimalStyle decAlt = ds.withDecimalSeparator(',');
        check(decAlt.getDecimalSeparator() == ',', "withDecimalSeparator");
        check(ds.getDecimalSeparator() == '.', "original separator unchanged");

        DecimalStyle posAlt = ds.withPositiveSign('P');
        check(posAlt.getPositiveSign() == 'P', "withPositiveSign");
        check(ds.getPositiveSign() == '+', "original positive sign unchanged");

        DecimalStyle negAlt = ds.withNegativeSign('N');
        check(negAlt.getNegativeSign() == 'N', "withNegativeSign");
        check(ds.getNegativeSign() == '-', "original negative sign unchanged");

        // getAvailableLocales (sorted) contains the US locale.
        TreeSet<String> locales = new TreeSet<>();
        for (Locale l : DecimalStyle.getAvailableLocales()) locales.add(l.toString());
        check(locales.contains(Locale.US.toString()), "available locales contain US");

        check(ds.equals(DecimalStyle.STANDARD), "equals STANDARD");
        check(ds.hashCode() == DecimalStyle.STANDARD.hashCode(), "hashCode STANDARD");
        check(!ds.equals(decAlt), "not equal to altered style");
        check(ds.toString() != null, "toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
