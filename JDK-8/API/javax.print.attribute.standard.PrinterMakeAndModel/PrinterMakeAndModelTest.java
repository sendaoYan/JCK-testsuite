/*
 * @test
 * @summary javax.print.attribute.standard.PrinterMakeAndModel conformance: text attribute.
 * @run main PrinterMakeAndModelTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PrinterMakeAndModelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PrinterMakeAndModel v = new PrinterMakeAndModel("sample-text", java.util.Locale.US);
        check("sample-text".equals(v.getValue()), "getValue");
        check(java.util.Locale.US.equals(v.getLocale()), "getLocale");
        check(v.getCategory() == PrinterMakeAndModel.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new PrinterMakeAndModel("sample-text", java.util.Locale.US)), "equals");
        check(v.hashCode() == new PrinterMakeAndModel("sample-text", java.util.Locale.US).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
