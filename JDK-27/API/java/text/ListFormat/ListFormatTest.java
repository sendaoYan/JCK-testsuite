/*
 * @test
 * @summary java.text.ListFormat conformance: white-box structure + black-box format/parse/patterns/iterator/equality.
 * @run main ListFormatTest
 */

import java.text.AttributedCharacterIterator;
import java.text.ListFormat;
import java.text.ParsePosition;
import java.util.List;
import java.util.Locale;

public class ListFormatTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.ListFormat");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "final concrete class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass().getName().equals("java.text.Format"), "extends Format");
        check(c.getMethod("getInstance") != null, "getInstance()");
        check(c.getMethod("getInstance", Locale.class, ListFormat.Type.class, ListFormat.Style.class) != null,
              "getInstance(Locale,Type,Style)");
        check(c.getMethod("format", List.class) != null, "format(List)");
        check(c.getMethod("parse", String.class) != null, "parse(String)");
        check(c.getMethod("formatToCharacterIterator", Object.class) != null, "formatToCharacterIterator");
        check(c.getMethod("getPatterns") != null, "getPatterns");
        check(c.getMethod("getLocale") != null, "getLocale");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ListFormat shortF = ListFormat.getInstance(Locale.US, ListFormat.Type.STANDARD, ListFormat.Style.SHORT);
        check("Apple & Banana".equals(shortF.format(List.of("Apple", "Banana"))), "SHORT two elements");
        ListFormat fullF = ListFormat.getInstance(Locale.US, ListFormat.Type.STANDARD, ListFormat.Style.FULL);
        check("Apple, Banana, and Cherry".equals(fullF.format(List.of("Apple", "Banana", "Cherry"))),
              "FULL three elements");
        check(shortF.getLocale().equals(Locale.US), "getLocale");
        check(shortF.getPatterns() != null && shortF.getPatterns().length > 0, "getPatterns");
        check(ListFormat.getAvailableLocales().length > 0, "getAvailableLocales");
        check(shortF.parse("Apple & Banana").size() == 2, "parse");
        check(shortF.format(List.of("solo")).equals("solo"), "single element");

        // parseObject with ParsePosition
        ParsePosition pp = new ParsePosition(0);
        Object parsed = shortF.parseObject("Apple & Banana", pp);
        check(parsed instanceof List, "parseObject returns List");
        check(((List<?>) parsed).size() == 2, "parseObject size 2");
        check("Apple".equals(((List<?>) parsed).get(0)) && "Banana".equals(((List<?>) parsed).get(1)),
              "parseObject elements");
        check(pp.getIndex() == "Apple & Banana".length(), "parseObject advances index");

        // formatToCharacterIterator
        AttributedCharacterIterator it = shortF.formatToCharacterIterator(List.of("Apple", "Banana"));
        StringBuilder sb = new StringBuilder();
        for (char ch = it.first(); ch != AttributedCharacterIterator.DONE; ch = it.next()) sb.append(ch);
        check("Apple & Banana".equals(sb.toString()), "formatToCharacterIterator text");

        // hashCode + toString
        ListFormat same = ListFormat.getInstance(Locale.US, ListFormat.Type.STANDARD, ListFormat.Style.SHORT);
        check(shortF.hashCode() == same.hashCode(), "equal instances have equal hashCode");
        check(shortF.equals(same), "equal instances are equal");
        check(shortF.toString().contains("ListFormat"), "toString contains class name");
    }


    // ---- deepening helper (contract-level additions) ----
    interface TestBlock { void go() throws Exception; }

    static void expectX(Class<? extends Throwable> expected, TestBlock r, String m) {
        try {
            r.go();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    // ================= contract deepening =================
    // pattern tables locked on jdk-27.0.1 (CLDR en_US)

    static void testTypeStyleMatrixContract() {
        String[][] want = {
            // STANDARD
            {"{0}, {1}", "{0}, {1}", "{0}, and {1}", "{0} and {1}", "{0}, {1}, and {2}"},
            {"{0}, {1}", "{0}, {1}", "{0}, & {1}", "{0} & {1}", "{0}, {1}, & {2}"},
            {"{0}, {1}", "{0}, {1}", "{0}, {1}", "{0}, {1}", "{0}, {1}, {2}"},
            // UNIT
            {"{0}, {1}", "{0}, {1}", "{0}, {1}", "{0}, {1}", "{0}, {1}, {2}"},
            {"{0}, {1}", "{0}, {1}", "{0}, {1}", "{0}, {1}", "{0}, {1}, {2}"},
            {"{0} {1}", "{0} {1}", "{0} {1}", "{0} {1}", "{0} {1} {2}"},
            // OR
            {"{0}, {1}", "{0}, {1}", "{0}, or {1}", "{0} or {1}", "{0}, {1}, or {2}"},
            {"{0}, {1}", "{0}, {1}", "{0}, or {1}", "{0} or {1}", "{0}, {1}, or {2}"},
            {"{0}, {1}", "{0}, {1}", "{0}, or {1}", "{0} or {1}", "{0}, {1}, or {2}"},
        };
        java.text.ListFormat.Type[] types = {java.text.ListFormat.Type.STANDARD,
                java.text.ListFormat.Type.UNIT, java.text.ListFormat.Type.OR};
        java.text.ListFormat.Style[] styles = {java.text.ListFormat.Style.FULL,
                java.text.ListFormat.Style.SHORT, java.text.ListFormat.Style.NARROW};
        int row = 0;
        for (java.text.ListFormat.Type t : types)
            for (java.text.ListFormat.Style s : styles) {
                ListFormat f = ListFormat.getInstance(Locale.US, t, s);
                check(java.util.Arrays.toString(want[row]).equals(java.util.Arrays.toString(f.getPatterns())),
                      t + "/" + s + " pattern table");
                row++;
            }
        check(row == 9, "3 types x 3 styles");
    }

    static void testFormatArityContract() {
        ListFormat f = ListFormat.getInstance(Locale.US, ListFormat.Type.STANDARD, ListFormat.Style.FULL);
        check("x".equals(f.format(new String[]{"x"})), "one element: as-is");
        check("x and y".equals(f.format(new String[]{"x", "y"})), "two elements use 'two' pattern");
        check("x, y, and z".equals(f.format(new String[]{"x", "y", "z"})), "three elements use 'three' pattern");
        check("x, y, z, and w".equals(f.format(new String[]{"x", "y", "z", "w"})),
              "four elements: start+middle+end");
        expectX(IllegalArgumentException.class, () -> f.format(new String[]{}),
                "empty array -> IAE");
        expectX(NullPointerException.class, () -> f.format((Object[]) null),
                "null array -> NPE");
        check(f.format(new Object[]{new Object()}) != null,
              "non-String elements formatted via toString");
        java.util.List<String> list = java.util.List.of("x", "y");
        check("x and y".equals(f.format(list)), "format(List) parity with format(Object[])");
    }

    static void testCustomPatternContract() {
        ListFormat custom = ListFormat.getInstance(new String[]{
                "{0}; {1}", "{0}; {1}", "{0}; and {1}", "", ""});
        check("x; and y".equals(custom.format(new String[]{"x", "y"})),
              "custom two-element uses 'end' when 'two' empty");
        check("x; y; and z".equals(custom.format(new String[]{"x", "y", "z"})),
              "custom three-element uses start+end");
        check(java.util.Arrays.toString(new String[]{"{0}; {1}", "{0}; {1}", "{0}; and {1}",
                "{0}; and {1}", "{0}; {1}; and {2}"}).equals(java.util.Arrays.toString(custom.getPatterns())),
              "empty two/three auto-derived from start/end");
        check(java.util.Locale.ROOT.equals(custom.getLocale()),
              "custom-pattern instance is locale-less (Locale.ROOT)");
        expectX(IllegalArgumentException.class, () -> ListFormat.getInstance(new String[]{"{0}"}),
                "fewer than 5 patterns -> IAE");
        check(ListFormat.getInstance(new String[]{"{0} {1}", "{0} {1}", "{0} {1}", "", ""}) != null,
              "patterns without {0} placeholder accepted");
        expectX(NullPointerException.class, () -> ListFormat.getInstance((String[]) null),
                "null patterns -> NPE");
    }

    static void testParseContract() throws java.text.ParseException {
        ListFormat f = ListFormat.getInstance(Locale.US, ListFormat.Type.STANDARD, ListFormat.Style.FULL);
        java.text.ParsePosition pp = new java.text.ParsePosition(0);
        Object parsed = f.parseObject("x, y, and z", pp);
        check(parsed instanceof java.util.List, "parseObject returns a List (Arrays$ArrayList impl)");
        check(parsed.toString().equals("[x, y, z]") && pp.getIndex() == 11,
              "parsed elements and final index 11");
        java.text.ParsePosition junk = new java.text.ParsePosition(0);
        Object j = f.parseObject("junk", junk);
        check(j != null && junk.getIndex() == 4 && junk.getErrorIndex() == -1,
              "unknown text parses as single element consuming to end, no error");
        java.util.List<String> direct = f.parse("x, y, and z");
        check(direct.size() == 3 && "z".equals(direct.get(2)), "parse(String) returns element list");
        check("solo".equals(f.parse("solo").get(0)), "parse single element");
    }
    public static void main(String[] args) throws Exception {
        testTypeStyleMatrixContract();
        testFormatArityContract();
        testCustomPatternContract();
        testParseContract();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
