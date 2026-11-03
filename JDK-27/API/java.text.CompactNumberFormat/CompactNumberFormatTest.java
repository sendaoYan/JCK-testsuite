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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
