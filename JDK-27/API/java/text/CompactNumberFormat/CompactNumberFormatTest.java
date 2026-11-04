/*
 * @test
 * @summary java.text.CompactNumberFormat conformance: white-box structure + black-box constructor/format/parse/digit/grouping/clone behavior.
 * @run main CompactNumberFormatTest
 */

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.CompactNumberFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class CompactNumberFormatTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.CompactNumberFormat");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "final concrete class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass().getName().equals("java.text.NumberFormat"), "extends NumberFormat");
        check(c.getConstructor(String.class, DecimalFormatSymbols.class, String[].class) != null,
              "ctor(String,DecimalFormatSymbols,String[])");
        check(c.getConstructor(String.class, DecimalFormatSymbols.class, String[].class, String.class) != null,
              "ctor(String,DecimalFormatSymbols,String[],String)");
        check(c.getMethod("formatToCharacterIterator", Object.class) != null, "formatToCharacterIterator");
        check(c.getMethod("getGroupingSize") != null, "getGroupingSize");
        check(c.getMethod("setGroupingSize", int.class) != null, "setGroupingSize(int)");
        check(c.getMethod("isStrict") != null && c.getMethod("setStrict", boolean.class) != null, "strict methods");
        check(c.getMethod("isParseBigDecimal") != null && c.getMethod("setParseBigDecimal", boolean.class) != null,
              "parseBigDecimal methods");
        check(c.getMethod("toString") != null, "toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        String[] pats = {"", "", "", "0K", "00K", "000K", "0M", "00M", "000M", "0B", "00B", "000B", "0T", "00T", "000T"};
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance(Locale.US);

        // direct constructors
        CompactNumberFormat c3 = new CompactNumberFormat("#,##0.0#", sym, pats);
        check("1K".equals(c3.format(1000)), "ctor(String,Symbols,String[]) format 1000");
        check("2K".equals(c3.format(1500)), "ctor format 1500 rounds");
        check("1M".equals(c3.format(1000000)), "ctor format 1000000");

        CompactNumberFormat c4 = new CompactNumberFormat("#,##0.0#", sym, pats, "one:n = 1");
        check("1K".equals(c4.format(1000)), "ctor(...,pluralRules) format 1000");

        CompactNumberFormat cnf = new CompactNumberFormat("#,##0.0#", sym, pats);

        // clone
        CompactNumberFormat clone = (CompactNumberFormat) cnf.clone();
        check(clone != null && clone != cnf, "clone non-null and distinct");
        check(clone.equals(cnf) && cnf.equals(clone), "clone equals original");
        check(clone.hashCode() == cnf.hashCode(), "clone equal hashCode");

        // formatToCharacterIterator
        AttributedCharacterIterator it = cnf.formatToCharacterIterator(1500);
        StringBuilder sb = new StringBuilder();
        for (char ch = it.first(); ch != AttributedCharacterIterator.DONE; ch = it.next()) sb.append(ch);
        check("2K".equals(sb.toString()), "formatToCharacterIterator text");

        // grouping size / used
        check(cnf.getGroupingSize() == 0, "getGroupingSize default 0");
        check(!cnf.isGroupingUsed(), "isGroupingUsed default false");
        cnf.setGroupingSize(2);
        cnf.setGroupingUsed(true);
        check(cnf.getGroupingSize() == 2, "setGroupingSize/getGroupingSize");
        check(cnf.isGroupingUsed(), "setGroupingUsed(true)/isGroupingUsed");

        // parse flags
        check(!cnf.isParseBigDecimal(), "isParseBigDecimal default false");
        check(!cnf.isParseIntegerOnly(), "isParseIntegerOnly default false");
        check(!cnf.isStrict(), "isStrict default false");
        cnf.setParseBigDecimal(true);
        check(cnf.isParseBigDecimal(), "setParseBigDecimal(true)");
        check(cnf.parse("1K").getClass() == BigDecimal.class, "parseBigDecimal returns BigDecimal");
        cnf.setParseIntegerOnly(true);
        check(cnf.isParseIntegerOnly(), "setParseIntegerOnly(true)");
        cnf.setStrict(true);
        check(cnf.isStrict(), "setStrict(true)");

        // integer/fraction digits
        cnf.setMaximumFractionDigits(3);
        cnf.setMinimumFractionDigits(2);
        cnf.setMaximumIntegerDigits(5);
        cnf.setMinimumIntegerDigits(1);
        check(cnf.getMaximumFractionDigits() == 3, "setMaximumFractionDigits/getMaximumFractionDigits");
        check(cnf.getMinimumFractionDigits() == 2, "setMinimumFractionDigits/getMinimumFractionDigits");
        check(cnf.getMaximumIntegerDigits() == 5, "setMaximumIntegerDigits/getMaximumIntegerDigits");
        check(cnf.getMinimumIntegerDigits() == 1, "setMinimumIntegerDigits/getMinimumIntegerDigits");

        // rounding mode
        cnf.setRoundingMode(RoundingMode.HALF_UP);
        check(cnf.getRoundingMode() == RoundingMode.HALF_UP, "setRoundingMode/getRoundingMode");

        // toString
        check(cnf.toString().contains("CompactNumberFormat"), "toString contains class name");

        // factory-instance behavior (preserved shallow assertions)
        CompactNumberFormat factory = (CompactNumberFormat) NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        check(factory instanceof CompactNumberFormat, "factory instanceof CompactNumberFormat");
        check("1K".equals(factory.format(1000)), "factory format 1000");
        check(factory.parse("1K").longValue() == 1000, "factory parse 1K");
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
    // values locked on jdk-27.0.1 (CLDR en_US compact patterns)

    static void testScaleTableContract() {
        CompactNumberFormat sh = (CompactNumberFormat) NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        check("1K".equals(sh.format(1234)), "SHORT 1234 -> 1K");
        check("1M".equals(sh.format(1234567)), "SHORT 1234567 -> 1M");
        check("1K".equals(sh.format(1200)), "SHORT 1200 -> 1K (fraction digits clamped to 0)");
        check("0".equals(sh.format(0)), "SHORT 0 -> 0");
        check("-1K".equals(sh.format(-1234)), "SHORT -1234 -> -1K (minus sign from symbols)");
        check("999".equals(sh.format(999)), "below first scale: 999 unchanged");
        check("0".equals(sh.format(0.5)), "0.5 rounds to 0");
        check("123".equals(sh.format(123)) && "12".equals(sh.format(12)),
              "unscaled values pass through");
        check("10B".equals(sh.format(1e10)), "1e10 -> 10B");
        check("2K".equals(sh.format(1500)), "default HALF_EVEN rounds 1500 -> 2K");
        CompactNumberFormat lg = (CompactNumberFormat) NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.LONG);
        check("1 thousand".equals(lg.format(1234)), "LONG 1234 -> 1 thousand");
        check("1 million".equals(lg.format(1234567)), "LONG 1234567 -> 1 million");
        check("0".equals(lg.format(0)) && "12".equals(lg.format(12)),
              "LONG unscaled values unchanged");
    }

    static void testParseMatrixContract() throws java.text.ParseException {
        CompactNumberFormat sh = (CompactNumberFormat) NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        check(sh.parse("1.2K").doubleValue() == 1200.0, "parse 1.2K -> 1200");
        java.text.ParsePosition p5 = new java.text.ParsePosition(0);
        check(sh.parse("5K", p5).longValue() == 5000 && p5.getIndex() == 2,
              "parse 5K -> 5000, index 2");
        java.text.ParsePosition low = new java.text.ParsePosition(0);
        check(sh.parse("5k", low).longValue() == 5 && low.getIndex() == 1 && low.getErrorIndex() == -1,
              "lowercase suffix not recognized: 5k parses as 5 at index 1");
        java.text.ParsePosition junk = new java.text.ParsePosition(0);
        check(sh.parse("bogus", junk) == null && junk.getIndex() == 0 && junk.getErrorIndex() == 0,
              "unparseable text: null result, index 0, errorIndex 0");
        java.text.ParsePosition n999 = new java.text.ParsePosition(0);
        check(sh.parse("999", n999).longValue() == 999 && n999.getIndex() == 3,
              "plain number parses fully");
        CompactNumberFormat lg = (CompactNumberFormat) NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.LONG);
        java.text.ParsePosition pt = new java.text.ParsePosition(0);
        check(lg.parse("1 thousand", pt).longValue() == 1000 && pt.getIndex() == 10,
              "LONG parse '1 thousand' -> 1000 consumed to 10");
        java.text.ParsePosition pm = new java.text.ParsePosition(0);
        check(lg.parse("5 million", pm).longValue() == 5000000 && pm.getIndex() == 9,
              "LONG parse '5 million' -> 5000000");
    }

    static void testDefaultsAndDigitControlContract() {
        CompactNumberFormat sh = (CompactNumberFormat) NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        check(sh.getRoundingMode() == java.math.RoundingMode.HALF_EVEN, "default rounding HALF_EVEN");
        check(!sh.isGroupingUsed(), "default grouping not used");
        sh.setGroupingUsed(true);
        check("1M".equals(sh.format(1234567)),
              "enabling grouping does not change SHORT compact output (patterns govern)");
        sh.setGroupingSize(3);
        check("1M".equals(sh.format(1234567)), "setGroupingSize accepted, output stable");
        sh.setMinimumIntegerDigits(3);
        check("002K".equals(sh.format(1500)), "minimumIntegerDigits 3 pads: 1500 -> 002K");
        sh.setMaximumFractionDigits(1);
        check("001.2K".equals(sh.format(1234)), "maximumFractionDigits 1: 1234 -> 001.2K");
        sh.setMinimumFractionDigits(1);
        check("001.0K".equals(sh.format(1000)), "minimumFractionDigits 1 pads: 1000 -> 001.0K");
        CompactNumberFormat up = (CompactNumberFormat) NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        up.setRoundingMode(java.math.RoundingMode.CEILING);
        check("2K".equals(up.format(1200)), "CEILING: 1200 -> 2K");
        check(!up.isParseBigDecimal() && !up.isParseIntegerOnly(),
              "parseBigDecimal / parseIntegerOnly default false");
        java.text.CompactNumberFormat fresh = (java.text.CompactNumberFormat)
                NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        check(fresh.format(1234L).equals("1K"), "format(Object) with Long 1234 -> 1K");
        check("2K".equals(up.format(1234L)), "CEILING instance formats Long 1234 -> 2K");
        check("NaN".equals(up.format(Double.NaN)), "NaN formatted with symbols NaN string");
        check("\u221E".equals(java.text.DecimalFormatSymbols.getInstance(Locale.US)
                .getInfinity()) && up.format(Double.POSITIVE_INFINITY).equals(
                java.text.DecimalFormatSymbols.getInstance(Locale.US).getInfinity()),
              "Infinity formatted with symbols infinity string");
        expectX(IllegalArgumentException.class,
                () -> up.format("x", new StringBuffer(), new java.text.FieldPosition(0)),
                "format(String,...) -> IAE (not a Number)");
    }

    static void testStructureAndEqualityContract() throws Exception {
        Class<?> c = java.text.CompactNumberFormat.class;
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass().getName().equals("java.text.NumberFormat"), "extends NumberFormat");
        check(c.getConstructor(String.class, java.text.DecimalFormatSymbols.class, String[].class) != null,
              "public 3-arg ctor (pattern, symbols, compactPatterns)");
        check(c.getConstructor(String.class, java.text.DecimalFormatSymbols.class, String[].class, String.class) != null,
              "public 4-arg ctor adds pluralRules");
        expectX(NoSuchMethodException.class, () -> c.getConstructor(String.class,
                java.text.DecimalFormatSymbols.class, String[].class, String[].class),
                "no 4-arg variant with String[] pluralRules");
        expectX(NullPointerException.class, () -> new java.text.CompactNumberFormat(null,
                java.text.DecimalFormatSymbols.getInstance(Locale.US), new String[]{"", "", "0", "0K"}),
                "null decimal pattern -> NPE");
        java.text.CompactNumberFormat custom = new java.text.CompactNumberFormat(
                "#,##0.0#", java.text.DecimalFormatSymbols.getInstance(Locale.US),
                new String[]{"", "", "K", "M"});
        check("M".equals(custom.format(1234)) && "M".equals(custom.format(12345)),
              "custom compact array: index = power of ten, pattern without digits emits literals only");
        check(NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT)
                      .equals(NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT)),
              "equal factory instances");
        java.text.CompactNumberFormat cl = (java.text.CompactNumberFormat)
                NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        check(cl.clone().equals(cl), "clone equals original");
        expectX(NullPointerException.class,
                () -> NumberFormat.getCompactNumberInstance(null, NumberFormat.Style.SHORT),
                "factory null locale -> NPE");
        expectX(NullPointerException.class,
                () -> NumberFormat.getCompactNumberInstance(Locale.US, null),
                "factory null style -> NPE");
        StringBuilder sb = new StringBuilder();
        java.text.AttributedCharacterIterator it = cl.formatToCharacterIterator(1234.5);
        for (char ch = it.first(); ch != java.text.AttributedCharacterIterator.DONE; ch = it.next())
            sb.append(ch);
        check("1K".equals(sb.toString()), "formatToCharacterIterator text matches format(1234.5)");
        check(it.getAllAttributeKeys().contains(java.text.NumberFormat.Field.INTEGER),
              "iterator carries NumberFormat.Field attributes");
    }
    public static void main(String[] args) throws Exception {
        testScaleTableContract();
        testParseMatrixContract();
        testDefaultsAndDigitControlContract();
        testStructureAndEqualityContract();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
