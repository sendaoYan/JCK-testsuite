/*
 * @test
 * @summary javax.swing.text.NumberFormatter conformance: white-box structure + black-box behavior.
 * @run main NumberFormatterTest
 */

import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.text.NumberFormatter;

public class NumberFormatterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.NumberFormatter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.InternationalFormatter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance(Locale.ROOT);
        DecimalFormat df = new DecimalFormat("0.00", sym);
        NumberFormatter f = new NumberFormatter(df);

        check(f.getFormat() == df, "constructor keeps format");
        check(f.getAllowsInvalid() == true, "allowsInvalid default true");
        check(f.getOverwriteMode() == false, "overwriteMode default false");
        check(f.getCommitsOnValidEdit() == false, "commitsOnValidEdit default false");

        check("1234.50".equals(f.valueToString(1234.5)), "valueToString decimal");
        check("-12.50".equals(f.valueToString(-12.5)), "valueToString negative");

        Object parsed = f.stringToValue("1234.50");
        check(parsed instanceof Double && ((Double) parsed).doubleValue() == 1234.5, "stringToValue Double");

        Object neg = f.stringToValue("-12.50");
        check(neg instanceof Double && ((Double) neg).doubleValue() == -12.5, "stringToValue negative Double");

        Object roundTrip = f.stringToValue(f.valueToString(1234.5));
        check(roundTrip instanceof Double && ((Double) roundTrip).doubleValue() == 1234.5, "round trip");

        boolean threw = false;
        try {
            f.stringToValue("not-a-number");
        } catch (ParseException pe) {
            threw = true;
        }
        check(threw, "illegal number -> ParseException");

        f.setValueClass(Integer.class);
        Object iv = f.stringToValue("42");
        check(iv instanceof Integer && ((Integer) iv).intValue() == 42, "stringToValue to Integer valueClass");

        NumberFormatter g = new NumberFormatter(new DecimalFormat("0", sym));
        g.setMinimum(0);
        g.setMaximum(100);
        Object inRange = g.stringToValue("50");
        check(inRange instanceof Integer && ((Integer) inRange).intValue() == 50, "in-range value");
        boolean outHigh = false;
        try { g.stringToValue("150"); } catch (ParseException pe) { outHigh = true; }
        check(outHigh, "above max -> ParseException");

        DecimalFormat df2 = new DecimalFormat("0.###", sym);
        f.setFormat(df2);
        check(f.getFormat() == df2, "setFormat/getFormat");
        check("3.14".equals(f.valueToString(3.14)), "valueToString after setFormat");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
