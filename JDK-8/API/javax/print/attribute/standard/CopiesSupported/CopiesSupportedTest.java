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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: range parsing / members normalization / contains / next / serialization ----
    static void testContractDeep() throws Exception {
        CopiesSupported pr = new CopiesSupported(1, 11);
        check(java.util.Arrays.deepEquals(pr.getMembers(), new int[][] {{1,11}}), "members from (lo,hi)");
        check(pr.contains(1) && pr.contains(11) && !pr.contains(0) && !pr.contains(12), "contains boundaries");
        check(pr.next(0) == 1 && pr.next(11) == -1, "next boundaries");
        check(java.util.Arrays.deepEquals(new CopiesSupported(4).getMembers(), new int[][] {{4,4}}), "single-int ctor");
        expectIAE("CopiesSupported(5,1)", new Runnable() { public void run() { new CopiesSupported(5, 1); } }, "Null range specified");
        expectIAE("CopiesSupported(0,0)", new Runnable() { public void run() { new CopiesSupported(0, 0); } }, "Copies value < 1 specified");
        expectIAE("CopiesSupported(0)", new Runnable() { public void run() { new CopiesSupported(0); } }, "Copies value < 1 specified");
        check(new CopiesSupported(1, 5).contains(new javax.print.attribute.standard.Copies(3)), "contains(IntegerSyntax) inside");
        check(!new CopiesSupported(1, 5).contains(new javax.print.attribute.standard.Copies(9)), "contains(IntegerSyntax) outside");
        check("copies-supported".equals(pr.getName()), "getName");
        check(CopiesSupported.class == pr.getCategory(), "getCategory");
        check(pr.equals(new CopiesSupported(1, 11)), "equals same range");
        check(!pr.equals(new CopiesSupported(2, 11)), "different range unequal");
        check(pr.hashCode() == new CopiesSupported(1, 11).hashCode(), "hashCode consistent");
        check(!pr.equals(null) && !pr.equals("1:3"), "null/foreign unequal");
        CopiesSupported rt = (CopiesSupported) serRoundTrip(pr);
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
