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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: range parsing / members normalization / contains / next / serialization ----
    static void testContractDeep() throws Exception {
        PageRanges pr = new PageRanges("1:3,5,9:11");
        check(java.util.Arrays.deepEquals(pr.getMembers(), new int[][] {{1,3},{5,5},{9,11}}), "members parsed+normalized+sorted");
        check("1-3,5,9-11".equals(pr.toString()), "toString range format [" + pr.toString() + "]");
        check(pr.contains(1) && pr.contains(2) && pr.contains(3) && pr.contains(5) && pr.contains(9) && pr.contains(11), "contains members");
        check(!pr.contains(0) && !pr.contains(4) && !pr.contains(6) && !pr.contains(8) && !pr.contains(12), "non-members false");
        check(pr.next(0) == 1 && pr.next(4) == 5 && pr.next(5) == 9 && pr.next(6) == 9 && pr.next(12) == -1, "next semantics");
        check(java.util.Arrays.deepEquals(new PageRanges(4).getMembers(), new int[][] {{4,4}}), "single-int ctor");
        check(java.util.Arrays.deepEquals(new PageRanges(new int[][] {{9,11},{1,3},{1,2}}).getMembers(), new int[][] {{1,3},{9,11}}), "int[][] ctor merges+sorts");
        check(java.util.Arrays.deepEquals(new PageRanges("2:5").getMembers(), new int[][] {{2,5}}), "String range ctor");
        expectIAE("PageRanges(5,1) string form", new Runnable() { public void run() { new PageRanges("5:1"); } }, "members is zero-length");
        expectIAE("PageRanges(bad-word)", new Runnable() { public void run() { new PageRanges("a"); } }, null);
        expectIAE("PageRanges(empty)", new Runnable() { public void run() { new PageRanges(""); } }, "members is zero-length");
        expectIAE("PageRanges(5,1)", new Runnable() { public void run() { new PageRanges(5, 1); } }, "Null range specified");
        check(new PageRanges(1, 5).contains(new javax.print.attribute.standard.Copies(3)), "contains(IntegerSyntax) uses value regardless of attribute type");
        check("page-ranges".equals(pr.getName()), "getName");
        check(PageRanges.class == pr.getCategory(), "getCategory");
        check(pr.equals(new PageRanges("1:3,5,9:11")), "equals same members");
        check(!pr.equals(new PageRanges("1:3")), "different members unequal");
        check(pr.hashCode() == new PageRanges("9:11,5,1:3").hashCode(), "hashCode member-order independent");
        check(!pr.equals(null) && !pr.equals("1:3"), "null/foreign unequal");
        PageRanges rt = (PageRanges) serRoundTrip(pr);
        check(rt.equals(pr) && java.util.Arrays.deepEquals(rt.getMembers(), pr.getMembers()), "serialization round-trip");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void expectIAE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected IAE: " + what); }
        catch (IllegalArgumentException e) { check(msg == null || msg.equals(e.getMessage()), "IAE msg " + what + " [" + e.getMessage() + "]"); }
    }
    static void expectNPE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected NPE: " + what); }
        catch (NullPointerException e) { check(msg == null || msg.equals(e.getMessage()), "NPE msg " + what + " [" + e.getMessage() + "]"); }
    }

}
