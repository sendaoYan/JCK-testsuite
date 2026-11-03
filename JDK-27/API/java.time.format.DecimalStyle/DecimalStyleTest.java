/*
 * @test
 * @summary java.time.format.DecimalStyle conformance: white-box structure + black-box behavior.
 * @run main DecimalStyleTest
 */

import java.lang.reflect.Modifier;
import java.time.format.DecimalStyle;
import java.util.Locale;
import java.util.TreeSet;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
