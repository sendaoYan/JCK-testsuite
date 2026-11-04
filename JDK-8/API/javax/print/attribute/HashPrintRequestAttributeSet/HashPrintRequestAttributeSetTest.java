/*
 * @test
 * @summary javax.print.attribute.HashPrintRequestAttributeSet conformance: attribute set implementation.
 * @run main HashPrintRequestAttributeSetTest
 */

import java.lang.reflect.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

public class HashPrintRequestAttributeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        HashPrintRequestAttributeSet set = new HashPrintRequestAttributeSet();
        check(set.isEmpty(), "initially empty");
        HashPrintRequestAttributeSet set2 = new HashPrintRequestAttributeSet(new Copies(2));
        check(!set2.isEmpty(), "single-arg ctor");
        HashPrintRequestAttributeSet set3 = new HashPrintRequestAttributeSet(set2);
        check(set3.equals(set2), "copy ctor");
        check(set instanceof javax.print.attribute.PrintRequestAttributeSet, "implements PrintRequestAttributeSet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
