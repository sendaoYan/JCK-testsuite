/*
 * @test
 * @summary javax.print.attribute.HashDocAttributeSet conformance: attribute set implementation.
 * @run main HashDocAttributeSetTest
 */

import java.lang.reflect.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

public class HashDocAttributeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        HashDocAttributeSet set = new HashDocAttributeSet();
        check(set.isEmpty(), "initially empty");
        HashDocAttributeSet set2 = new HashDocAttributeSet(new DocumentName("x", java.util.Locale.US));
        check(!set2.isEmpty(), "single-arg ctor");
        HashDocAttributeSet set3 = new HashDocAttributeSet(set2);
        check(set3.equals(set2), "copy ctor");
        check(set instanceof javax.print.attribute.DocAttributeSet, "implements DocAttributeSet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
