/*
 * @test
 * @summary javax.print.attribute.standard.JobImpressionsCompleted conformance: integer attribute.
 * @run main JobImpressionsCompletedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobImpressionsCompletedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobImpressionsCompleted v = new JobImpressionsCompleted(10);
        check(v.getValue() == 10, "getValue");
        check(v.getCategory() == JobImpressionsCompleted.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobImpressionsCompleted(10)), "equals");
        check(!v.equals(new JobImpressionsCompleted(20)), "not equals");
        check(v.hashCode() == new JobImpressionsCompleted(10).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: constructor range matrix / equals / serialization ----
    static void testContractDeep() throws Exception {
        check(new JobImpressionsCompleted(0).getValue() == 0, "min boundary 0 accepted");
        check(new JobImpressionsCompleted(2147483647).getValue() == 2147483647, "max boundary 2147483647 accepted");
        expectIAE("JobImpressionsCompleted(-1)", new Runnable() { public void run() { new JobImpressionsCompleted(-1); } }, "Value -1 not in range 0..2147483647");
        check(new JobImpressionsCompleted(Integer.MAX_VALUE).getValue() == Integer.MAX_VALUE, "Integer.MAX_VALUE accepted (no int overflow possible above)");
        expectIAE("JobImpressionsCompleted(Integer.MIN_VALUE)", new Runnable() { public void run() { new JobImpressionsCompleted(Integer.MIN_VALUE); } }, null);
        check("job-impressions-completed".equals(new JobImpressionsCompleted(0).getName()), "getName");
        check(JobImpressionsCompleted.class == new JobImpressionsCompleted(0).getCategory(), "getCategory");
        check(new JobImpressionsCompleted(7).equals(new JobImpressionsCompleted(7)), "equals same value");
        check(!new JobImpressionsCompleted(7).equals(new JobImpressionsCompleted(8)), "different value unequal");
        check(!new JobImpressionsCompleted(7).equals(null) && !new JobImpressionsCompleted(7).equals("7"), "null/foreign unequal");
        check(new JobImpressionsCompleted(7).hashCode() == 7, "hashCode == value");
        check(new JobImpressionsCompleted(7).equals(new javax.print.attribute.standard.Copies(7)) == ("JobImpressionsCompleted" == "Copies"), "cross IntegerSyntax subclass equals false");
        JobImpressionsCompleted rt = (JobImpressionsCompleted) serRoundTrip(new JobImpressionsCompleted(7));
        check(rt.equals(new JobImpressionsCompleted(7)) && rt.getValue() == 7, "serialization round-trip");
        check(new JobImpressionsCompleted(12).toString().equals("12"), "toString == value string");
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
