/*
 * @test
 * @summary java.util.Locale API conformance: language/country accessors, ENGLISH/US/ROOT
 *          constants, the (language, country) constructor, Locale.Builder, and getDefault.
 * @run main LocaleTest
 */

import java.util.Locale;

public class LocaleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConstants() {
        Locale en = Locale.ENGLISH;
        check("en".equals(en.getLanguage()) && "".equals(en.getCountry()), "ENGLISH");
        Locale us = Locale.US;
        check("en".equals(us.getLanguage()) && "US".equals(us.getCountry()), "US");
        Locale root = Locale.ROOT;
        check("".equals(root.getLanguage()) && "".equals(root.getCountry()), "ROOT");
    }

    static void testConstruction() {
        Locale fr = new Locale("fr", "FR");
        check("fr".equals(fr.getLanguage()) && "FR".equals(fr.getCountry()), "(language, country)");
        Locale built = new Locale.Builder().setLanguage("de").setRegion("DE").build();
        check("de".equals(built.getLanguage()) && "DE".equals(built.getCountry()), "Builder");
    }

    static void testDefault() {
        Locale def = Locale.getDefault();
        check(def != null, "getDefault non-null");
    }

    public static void main(String[] args) {
        testConstants();
        testConstruction();
        testDefault();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
