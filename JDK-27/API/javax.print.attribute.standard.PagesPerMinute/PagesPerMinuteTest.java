/*
 * @test
 * @summary javax.print.attribute.standard.PagesPerMinute conformance: integer attribute.
 * @run main PagesPerMinuteTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PagesPerMinuteTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PagesPerMinute v = new PagesPerMinute(20);
        check(v.getValue() == 20, "getValue");
        check(v.getCategory() == PagesPerMinute.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new PagesPerMinute(20)), "equals");
        check(!v.equals(new PagesPerMinute(30)), "not equals");
        check(v.hashCode() == new PagesPerMinute(20).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
