/*
 * @test
 * @summary javax.print.attribute.standard.JobMediaSheetsSupported conformance: set-of-integer attribute.
 * @run main JobMediaSheetsSupportedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobMediaSheetsSupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobMediaSheetsSupported v = new JobMediaSheetsSupported(0, 5);
        check(v.getCategory() == JobMediaSheetsSupported.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.contains(0), "contains 0");
        check(v.contains(2), "contains 2");
        check(v.contains(5), "contains 5");
        check(!v.contains(6), "not contains 6");
        check(v.getMembers() != null, "getMembers");
        check(v.equals(new JobMediaSheetsSupported(0, 5)), "equals");
        check(v.hashCode() == new JobMediaSheetsSupported(0, 5).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
