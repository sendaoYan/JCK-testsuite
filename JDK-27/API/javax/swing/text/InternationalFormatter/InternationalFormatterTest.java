/*
 * @test
 * @summary javax.swing.text.InternationalFormatter conformance: white-box structure + black-box behavior.
 * @run main InternationalFormatterTest
 */

import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.text.InternationalFormatter;

public class InternationalFormatterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.InternationalFormatter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.DefaultFormatter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance(Locale.ROOT);
        DecimalFormat df = new DecimalFormat("0.00", sym);

        InternationalFormatter f = new InternationalFormatter();
        check(f.getFormat() == null, "no-arg format null");
        check(f.getMinimum() == null, "no-arg minimum null");
        check(f.getMaximum() == null, "no-arg maximum null");
        check(f.getOverwriteMode() == false, "no-arg overwriteMode false");

        InternationalFormatter f2 = new InternationalFormatter(df);
        check(f2.getFormat() == df, "constructor keeps format");

        f2.setFormat(df);
        check(f2.getFormat() == df, "setFormat/getFormat");

        check("".equals(f2.valueToString(null)), "valueToString(null) empty");
        check("1234.50".equals(f2.valueToString(1234.5)), "valueToString decimal");

        Object parsed = f2.stringToValue("1234.50");
        check(parsed instanceof Double && ((Double) parsed).doubleValue() == 1234.5, "stringToValue Double");

        Object roundTrip = f2.stringToValue(f2.valueToString(1234.5));
        check(roundTrip instanceof Double && ((Double) roundTrip).doubleValue() == 1234.5, "valueToString/stringToValue round trip");

        boolean threw = false;
        try {
            f2.stringToValue("abc");
        } catch (ParseException pe) {
            threw = true;
        }
        check(threw, "illegal number -> ParseException");

        DecimalFormat intFmt = new DecimalFormat("0", sym);
        InternationalFormatter f3 = new InternationalFormatter(intFmt);
        f3.setMinimum(0);
        check(f3.getValueClass() == Integer.class, "setMinimum sets valueClass");
        check(Integer.valueOf(0).equals(f3.getMinimum()), "getMinimum round trip");
        f3.setMaximum(100);
        check(Integer.valueOf(100).equals(f3.getMaximum()), "getMaximum round trip");

        Object v = f3.stringToValue("50");
        check(v instanceof Integer && ((Integer) v).intValue() == 50, "stringToValue in range");

        boolean outHigh = false, outLow = false;
        try { f3.stringToValue("150"); } catch (ParseException pe) { outHigh = true; }
        try { f3.stringToValue("-5"); } catch (ParseException pe) { outLow = true; }
        check(outHigh, "above max -> ParseException");
        check(outLow, "below min -> ParseException");

        InternationalFormatter clone = (InternationalFormatter) f2.clone();
        check(clone != f2 && clone.getClass() == InternationalFormatter.class, "clone distinct same class");
        check(clone.getFormat() == df, "clone copies format");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
