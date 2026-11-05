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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: range parsing / members normalization / contains / next / serialization ----
    static void testContractDeep() throws Exception {
        NumberUpSupported pr = new NumberUpSupported(1, 11);
        check(java.util.Arrays.deepEquals(pr.getMembers(), new int[][] {{1,11}}), "members from (lo,hi)");
        check(pr.contains(1) && pr.contains(11) && !pr.contains(0) && !pr.contains(12), "contains boundaries");
        check(pr.next(0) == 1 && pr.next(11) == -1, "next boundaries");
        check(java.util.Arrays.deepEquals(new NumberUpSupported(4).getMembers(), new int[][] {{4,4}}), "single-int ctor");
        check(java.util.Arrays.deepEquals(new NumberUpSupported(new int[][] {{9,11},{1,3},{1,2}}).getMembers(), new int[][] {{1,3},{9,11}}), "int[][] ctor merges+sorts");
        expectIAE("NumberUpSupported(5,1)", new Runnable() { public void run() { new NumberUpSupported(5, 1); } }, "Null range specified");
        expectIAE("NumberUpSupported(0)", new Runnable() { public void run() { new NumberUpSupported(0); } }, "Number up value must be > 0");
        expectIAE("NumberUpSupported(0,3)", new Runnable() { public void run() { new NumberUpSupported(0, 3); } }, "Number up value must be > 0");
        check("number-up-supported".equals(pr.getName()), "getName");
        check(NumberUpSupported.class == pr.getCategory(), "getCategory");
        check(pr.equals(new NumberUpSupported(1, 11)), "equals same range");
        check(!pr.equals(new NumberUpSupported(2, 11)), "different range unequal");
        check(pr.hashCode() == new NumberUpSupported(1, 11).hashCode(), "hashCode consistent");
        check(!pr.equals(null) && !pr.equals("1:3"), "null/foreign unequal");
        NumberUpSupported rt = (NumberUpSupported) serRoundTrip(pr);
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
