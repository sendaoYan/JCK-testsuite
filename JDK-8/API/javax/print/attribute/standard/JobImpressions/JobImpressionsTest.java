/*
 * @test
 * @summary javax.print.attribute.standard.JobImpressions conformance: integer attribute.
 * @run main JobImpressionsTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobImpressionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobImpressions v = new JobImpressions(10);
        check(v.getValue() == 10, "getValue");
        check(v.getCategory() == JobImpressions.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobImpressions(10)), "equals");
        check(!v.equals(new JobImpressions(20)), "not equals");
        check(v.hashCode() == new JobImpressions(10).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
