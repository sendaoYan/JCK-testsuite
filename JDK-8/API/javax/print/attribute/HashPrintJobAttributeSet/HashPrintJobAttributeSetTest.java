/*
 * @test
 * @summary javax.print.attribute.HashPrintJobAttributeSet conformance: attribute set implementation.
 * @run main HashPrintJobAttributeSetTest
 */

import java.lang.reflect.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

public class HashPrintJobAttributeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        HashPrintJobAttributeSet set = new HashPrintJobAttributeSet();
        check(set.isEmpty(), "initially empty");
        HashPrintJobAttributeSet set2 = new HashPrintJobAttributeSet(new JobName("x", java.util.Locale.US));
        check(!set2.isEmpty(), "single-arg ctor");
        HashPrintJobAttributeSet set3 = new HashPrintJobAttributeSet(set2);
        check(set3.equals(set2), "copy ctor");
        check(set instanceof javax.print.attribute.PrintJobAttributeSet, "implements PrintJobAttributeSet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
