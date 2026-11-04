/*
 * @test
 * @summary javax.print.attribute.standard.NumberUpSupported conformance: set-of-integer attribute.
 * @run main NumberUpSupportedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class NumberUpSupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        NumberUpSupported v = new NumberUpSupported(new int[][] {{1,1},{3,3}});
        check(v.getCategory() == NumberUpSupported.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.contains(1), "contains 1");
        check(v.contains(3), "contains 3");
        check(!v.contains(2), "not contains 2");
        check(v.getMembers() != null && v.getMembers().length == 2, "getMembers");
        check(v.equals(new NumberUpSupported(new int[][] {{1,1},{3,3}})), "equals");
        check(v.hashCode() == new NumberUpSupported(new int[][] {{1,1},{3,3}}).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        NumberUpSupported s = new NumberUpSupported(2);
        check(s.contains(2), "single 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
