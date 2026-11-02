/*
 * @test
 * @summary javax.print.attribute.standard.JobHoldUntil conformance: date-time attribute.
 * @run main JobHoldUntilTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobHoldUntilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobHoldUntil v = new JobHoldUntil(new java.util.Date(0L));
        check(new java.util.Date(0L).equals(v.getValue()), "getValue");
        check(v.getCategory() == JobHoldUntil.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobHoldUntil(new java.util.Date(0L))), "equals");
        check(v.hashCode() == new JobHoldUntil(new java.util.Date(0L)).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
