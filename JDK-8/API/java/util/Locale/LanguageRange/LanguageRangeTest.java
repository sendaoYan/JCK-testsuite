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


    // ===================== 契约深化：parse/ctor 校验矩阵/equals/mapEquivalents =====================
    static void testLanguageRangeContracts() {
        java.util.List<Locale.LanguageRange> l1 =
              Locale.LanguageRange.parse("fr-CH;q=0.8, en;q=0.9, *;q=0.1");
        check("en".equals(l1.get(0).getRange()) && l1.get(0).getWeight() == 0.9,
              "parse sorted descending by weight");
        check("fr-ch".equals(l1.get(1).getRange()) && l1.get(1).getWeight() == 0.8,
              "parse lowercases range");
        check("*".equals(l1.get(2).getRange()), "wildcard parsed");
        try { Locale.LanguageRange.parse("en;q=1.5"); check(false, "q=1.5 IAE"); }
        catch (IllegalArgumentException iae) { check(true, "q=1.5 IAE"); }
        try { Locale.LanguageRange.parse("en;q=-0.1"); check(false, "q=-0.1 IAE"); }
        catch (IllegalArgumentException iae) { check(true, "q=-0.1 IAE"); }
        try { Locale.LanguageRange.parse("en;q=abc"); check(false, "q=abc IAE"); }
        catch (IllegalArgumentException iae) { check(true, "q=abc IAE"); }
        try { Locale.LanguageRange.parse(""); check(false, "empty ranges IAE"); }
        catch (IllegalArgumentException iae) { check(true, "empty ranges IAE"); }
        try { Locale.LanguageRange.parse(null); check(false, "parse(null) NPE"); }
        catch (NullPointerException npe) { check(true, "parse(null) NPE"); }
        try { new Locale.LanguageRange("en", 1.5); check(false, "ctor weight 1.5 IAE"); }
        catch (IllegalArgumentException iae) { check(true, "ctor weight 1.5 IAE"); }
        try { new Locale.LanguageRange("en", -0.5); check(false, "ctor weight negative IAE"); }
        catch (IllegalArgumentException iae) { check(true, "ctor weight negative IAE"); }
        check(new Locale.LanguageRange("en").equals(new Locale.LanguageRange("en", 1.0)),
              "equals: default weight equals MAX");
        check(!new Locale.LanguageRange("en", 0.5).equals(new Locale.LanguageRange("en")),
              "equals: different weight differs");
        check(new Locale.LanguageRange("en").hashCode()
              == new Locale.LanguageRange("en", 1.0).hashCode(), "hashCode consistent with equals");
        java.util.Map<String, java.util.List<String>> map = new java.util.HashMap<>();
        java.util.List<String> vals = new java.util.ArrayList<>();
        vals.add("ja");
        map.put("ja-JP", vals);
        java.util.List<Locale.LanguageRange> mapped =
              Locale.LanguageRange.mapEquivalents(Locale.LanguageRange.parse("ja-JP"), map);
        check(mapped.size() == 1 && "ja".equals(mapped.get(0).getRange()),
              "mapEquivalents substitutes mapped range");
    }

    public static void main(String[] args) {
        testLanguageRange();
        testLanguageRangeContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
