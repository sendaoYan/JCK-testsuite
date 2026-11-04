/*
 * @test
 * @summary javax.print.attribute.standard.PrinterResolution conformance: resolution attribute.
 * @run main PrinterResolutionTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PrinterResolutionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PrinterResolution v = new PrinterResolution(300, 600, javax.print.attribute.ResolutionSyntax.DPI);
        int[] r = v.getResolution(javax.print.attribute.ResolutionSyntax.DPI);
        check(r.length == 2 && r[0] == 300 && r[1] == 600, "getResolution");
        check(v.getCrossFeedResolution(javax.print.attribute.ResolutionSyntax.DPI) == 300, "cross feed");
        check(v.getFeedResolution(javax.print.attribute.ResolutionSyntax.DPI) == 600, "feed");
        check(v.getCategory() == PrinterResolution.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new PrinterResolution(300, 600, javax.print.attribute.ResolutionSyntax.DPI)), "equals");
        check(v.hashCode() == new PrinterResolution(300, 600, javax.print.attribute.ResolutionSyntax.DPI).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
