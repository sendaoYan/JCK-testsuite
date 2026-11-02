/*
 * @test
 * @summary javax.print.attribute.standard.CopiesSupported conformance: set-of-integer attribute.
 * @run main CopiesSupportedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class CopiesSupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        CopiesSupported v = new CopiesSupported(1, 5);
        check(v.getCategory() == CopiesSupported.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.contains(1), "contains 1");
        check(v.contains(3), "contains 3");
        check(v.contains(5), "contains 5");
        check(!v.contains(0), "not contains 0");
        check(!v.contains(6), "not contains 6");
        check(v.getMembers() != null, "getMembers");
        check(v.equals(new CopiesSupported(1, 5)), "equals");
        check(v.hashCode() == new CopiesSupported(1, 5).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
