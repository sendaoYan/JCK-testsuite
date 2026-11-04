/*
 * @test
 * @summary javax.print.attribute.standard.JobKOctetsSupported conformance: set-of-integer attribute.
 * @run main JobKOctetsSupportedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobKOctetsSupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobKOctetsSupported v = new JobKOctetsSupported(0, 5);
        check(v.getCategory() == JobKOctetsSupported.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.contains(0), "contains 0");
        check(v.contains(2), "contains 2");
        check(v.contains(5), "contains 5");
        check(!v.contains(6), "not contains 6");
        check(v.getMembers() != null, "getMembers");
        check(v.equals(new JobKOctetsSupported(0, 5)), "equals");
        check(v.hashCode() == new JobKOctetsSupported(0, 5).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
