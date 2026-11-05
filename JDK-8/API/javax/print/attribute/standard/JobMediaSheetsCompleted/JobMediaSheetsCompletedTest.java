/*
 * @test
 * @summary javax.print.attribute.standard.JobMediaSheetsCompleted conformance: integer attribute.
 * @run main JobMediaSheetsCompletedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobMediaSheetsCompletedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobMediaSheetsCompleted v = new JobMediaSheetsCompleted(10);
        check(v.getValue() == 10, "getValue");
        check(v.getCategory() == JobMediaSheetsCompleted.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobMediaSheetsCompleted(10)), "equals");
        check(!v.equals(new JobMediaSheetsCompleted(20)), "not equals");
        check(v.hashCode() == new JobMediaSheetsCompleted(10).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: constructor range matrix / equals / serialization ----
    static void testContractDeep() throws Exception {
        check(new JobMediaSheetsCompleted(0).getValue() == 0, "min boundary 0 accepted");
        check(new JobMediaSheetsCompleted(2147483647).getValue() == 2147483647, "max boundary 2147483647 accepted");
        expectIAE("JobMediaSheetsCompleted(-1)", new Runnable() { public void run() { new JobMediaSheetsCompleted(-1); } }, "Value -1 not in range 0..2147483647");
        check(new JobMediaSheetsCompleted(Integer.MAX_VALUE).getValue() == Integer.MAX_VALUE, "Integer.MAX_VALUE accepted (no int overflow possible above)");
        expectIAE("JobMediaSheetsCompleted(Integer.MIN_VALUE)", new Runnable() { public void run() { new JobMediaSheetsCompleted(Integer.MIN_VALUE); } }, null);
        check("job-media-sheets-completed".equals(new JobMediaSheetsCompleted(0).getName()), "getName");
        check(JobMediaSheetsCompleted.class == new JobMediaSheetsCompleted(0).getCategory(), "getCategory");
        check(new JobMediaSheetsCompleted(7).equals(new JobMediaSheetsCompleted(7)), "equals same value");
        check(!new JobMediaSheetsCompleted(7).equals(new JobMediaSheetsCompleted(8)), "different value unequal");
        check(!new JobMediaSheetsCompleted(7).equals(null) && !new JobMediaSheetsCompleted(7).equals("7"), "null/foreign unequal");
        check(new JobMediaSheetsCompleted(7).hashCode() == 7, "hashCode == value");
        check(new JobMediaSheetsCompleted(7).equals(new javax.print.attribute.standard.Copies(7)) == ("JobMediaSheetsCompleted" == "Copies"), "cross IntegerSyntax subclass equals false");
        JobMediaSheetsCompleted rt = (JobMediaSheetsCompleted) serRoundTrip(new JobMediaSheetsCompleted(7));
        check(rt.equals(new JobMediaSheetsCompleted(7)) && rt.getValue() == 7, "serialization round-trip");
        check(new JobMediaSheetsCompleted(12).toString().equals("12"), "toString == value string");
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
