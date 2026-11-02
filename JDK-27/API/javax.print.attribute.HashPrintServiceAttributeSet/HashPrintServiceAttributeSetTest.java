/*
 * @test
 * @summary javax.print.attribute.HashPrintServiceAttributeSet conformance: attribute set implementation.
 * @run main HashPrintServiceAttributeSetTest
 */

import java.lang.reflect.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

public class HashPrintServiceAttributeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        HashPrintServiceAttributeSet set = new HashPrintServiceAttributeSet();
        check(set.isEmpty(), "initially empty");
        HashPrintServiceAttributeSet set2 = new HashPrintServiceAttributeSet(new PrinterName("x", java.util.Locale.US));
        check(!set2.isEmpty(), "single-arg ctor");
        HashPrintServiceAttributeSet set3 = new HashPrintServiceAttributeSet(set2);
        check(set3.equals(set2), "copy ctor");
        check(set instanceof javax.print.attribute.PrintServiceAttributeSet, "implements PrintServiceAttributeSet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
