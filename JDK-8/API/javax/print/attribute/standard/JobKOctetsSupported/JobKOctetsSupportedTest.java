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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: range parsing / members normalization / contains / next / serialization ----
    static void testContractDeep() throws Exception {
        JobKOctetsSupported pr = new JobKOctetsSupported(1, 11);
        check(java.util.Arrays.deepEquals(pr.getMembers(), new int[][] {{1,11}}), "members from (lo,hi)");
        check(pr.contains(1) && pr.contains(11) && !pr.contains(0) && !pr.contains(12), "contains boundaries");
        check(pr.next(0) == 1 && pr.next(11) == -1, "next boundaries");
        expectIAE("JobKOctetsSupported(5,1)", new Runnable() { public void run() { new JobKOctetsSupported(5, 1); } }, "Null range specified");
        check("job-k-octets-supported".equals(pr.getName()), "getName");
        check(JobKOctetsSupported.class == pr.getCategory(), "getCategory");
        check(pr.equals(new JobKOctetsSupported(1, 11)), "equals same range");
        check(!pr.equals(new JobKOctetsSupported(2, 11)), "different range unequal");
        check(pr.hashCode() == new JobKOctetsSupported(1, 11).hashCode(), "hashCode consistent");
        check(!pr.equals(null) && !pr.equals("1:3"), "null/foreign unequal");
        JobKOctetsSupported rt = (JobKOctetsSupported) serRoundTrip(pr);
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
