/*
 * @test
 * @summary java.text.CompactNumberFormat conformance: getCompactNumberInstance/format/parse.
 * @run main CompactNumberFormatTest
 */
import java.text.CompactNumberFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CompactNumberFormatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        NumberFormat nf = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        check(nf instanceof CompactNumberFormat, "instanceof CompactNumberFormat");
        CompactNumberFormat cnf = (CompactNumberFormat) nf;
        check("1K".equals(cnf.format(1000)), "format 1000");
        check("2K".equals(cnf.format(1500)), "format 1500 rounds");
        check("1M".equals(cnf.format(1000000)), "format 1000000");
        check(cnf.parse("1K").longValue() == 1000, "parse 1K");
        check(cnf.getRoundingMode() != null, "getRoundingMode non-null");
        check(cnf.equals(NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT)), "equals");
        check(cnf.hashCode() == cnf.hashCode(), "hashCode stable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
