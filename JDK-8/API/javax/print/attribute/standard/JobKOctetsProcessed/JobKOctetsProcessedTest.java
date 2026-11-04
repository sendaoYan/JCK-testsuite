/*
 * @test
 * @summary javax.print.attribute.standard.JobKOctetsProcessed conformance: integer attribute.
 * @run main JobKOctetsProcessedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobKOctetsProcessedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobKOctetsProcessed v = new JobKOctetsProcessed(10);
        check(v.getValue() == 10, "getValue");
        check(v.getCategory() == JobKOctetsProcessed.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobKOctetsProcessed(10)), "equals");
        check(!v.equals(new JobKOctetsProcessed(20)), "not equals");
        check(v.hashCode() == new JobKOctetsProcessed(10).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
