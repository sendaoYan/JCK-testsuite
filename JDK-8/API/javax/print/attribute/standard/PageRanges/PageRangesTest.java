/*
 * @test
 * @summary javax.print.attribute.standard.PageRanges conformance: set-of-integer attribute.
 * @run main PageRangesTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PageRangesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PageRanges v = new PageRanges(1, 5);
        check(v.getCategory() == PageRanges.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.contains(1) && v.contains(3) && v.contains(5), "contains range");
        check(!v.contains(0) && !v.contains(6), "not contains outside");
        check(v.getMembers() != null, "getMembers");
        check(v.equals(new PageRanges(1, 5)), "equals int,int");
        check(v.hashCode() == new PageRanges(1, 5).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        PageRanges str = new PageRanges("1-3");
        check(str.contains(1) && str.contains(2) && str.contains(3), "string ctor");
        PageRanges single = new PageRanges(2);
        check(single.contains(2), "single int ctor");
        PageRanges arr = new PageRanges(new int[][] {{1,2},{4,4}});
        check(arr.contains(1) && arr.contains(2) && arr.contains(4), "array ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
