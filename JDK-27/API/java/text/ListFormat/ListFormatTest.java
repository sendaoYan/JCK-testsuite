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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
