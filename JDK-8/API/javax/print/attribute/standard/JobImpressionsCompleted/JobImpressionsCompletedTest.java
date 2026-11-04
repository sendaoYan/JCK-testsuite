/*
 * @test
 * @summary javax.print.attribute.standard.JobImpressionsCompleted conformance: integer attribute.
 * @run main JobImpressionsCompletedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobImpressionsCompletedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobImpressionsCompleted v = new JobImpressionsCompleted(10);
        check(v.getValue() == 10, "getValue");
        check(v.getCategory() == JobImpressionsCompleted.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobImpressionsCompleted(10)), "equals");
        check(!v.equals(new JobImpressionsCompleted(20)), "not equals");
        check(v.hashCode() == new JobImpressionsCompleted(10).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
