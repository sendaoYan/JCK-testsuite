/*
 * @test
 * @summary javax.print.attribute.standard.PagesPerMinuteColor conformance: integer attribute.
 * @run main PagesPerMinuteColorTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PagesPerMinuteColorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PagesPerMinuteColor v = new PagesPerMinuteColor(20);
        check(v.getValue() == 20, "getValue");
        check(v.getCategory() == PagesPerMinuteColor.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new PagesPerMinuteColor(20)), "equals");
        check(!v.equals(new PagesPerMinuteColor(30)), "not equals");
        check(v.hashCode() == new PagesPerMinuteColor(20).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
