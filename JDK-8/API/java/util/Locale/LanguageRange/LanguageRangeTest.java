/*
 * @test
 * @summary java.util.Locale.LanguageRange conformance: range/weight/parse.
 * @run main LanguageRangeTest
 */

import java.util.List;
import java.util.Locale;

public class LanguageRangeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLanguageRange() {
        Locale.LanguageRange r = new Locale.LanguageRange("en-US", 0.5);
        check("en-us".equals(r.getRange()), "getRange (lowercased)");
        check(r.getWeight() == 0.5, "getWeight");
        Locale.LanguageRange r2 = new Locale.LanguageRange("fr");
        check(r2.getWeight() == Locale.LanguageRange.MAX_WEIGHT, "default weight max");
        check(Locale.LanguageRange.MIN_WEIGHT == 0.0
              && Locale.LanguageRange.MAX_WEIGHT == 1.0, "weight constants");
        List<Locale.LanguageRange> parsed = Locale.LanguageRange.parse("en-US;q=0.5, fr");
        check(parsed.size() == 2, "parse count");
        check("fr".equals(parsed.get(0).getRange()) && parsed.get(0).getWeight() == 1.0,
              "parse first (higher weight first)");
        check("en-us".equals(parsed.get(1).getRange()) && parsed.get(1).getWeight() == 0.5,
              "parse second");
    }

    public static void main(String[] args) {
        testLanguageRange();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
