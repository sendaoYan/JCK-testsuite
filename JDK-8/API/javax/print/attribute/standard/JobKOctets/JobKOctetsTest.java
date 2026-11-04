/*
 * @test
 * @summary javax.print.attribute.standard.JobKOctets conformance: integer attribute.
 * @run main JobKOctetsTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobKOctetsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobKOctets v = new JobKOctets(10);
        check(v.getValue() == 10, "getValue");
        check(v.getCategory() == JobKOctets.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobKOctets(10)), "equals");
        check(!v.equals(new JobKOctets(20)), "not equals");
        check(v.hashCode() == new JobKOctets(10).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
