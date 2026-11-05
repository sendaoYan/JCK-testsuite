/*
 * @test
 * @summary javax.print.attribute.standard.JobPrioritySupported conformance: integer attribute.
 * @run main JobPrioritySupportedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobPrioritySupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobPrioritySupported v = new JobPrioritySupported(5);
        check(v.getValue() == 5, "getValue");
        check(v.getCategory() == JobPrioritySupported.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobPrioritySupported(5)), "equals");
        check(!v.equals(new JobPrioritySupported(7)), "not equals");
        check(v.hashCode() == new JobPrioritySupported(5).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: constructor range matrix / equals / serialization ----
    static void testContractDeep() throws Exception {
        check(new JobPrioritySupported(1).getValue() == 1, "min boundary 1 accepted");
        check(new JobPrioritySupported(100).getValue() == 100, "max boundary 100 accepted");
        expectIAE("JobPrioritySupported(0)", new Runnable() { public void run() { new JobPrioritySupported(0); } }, "Value 0 not in range 1..100");
        expectIAE("JobPrioritySupported(101)", new Runnable() { public void run() { new JobPrioritySupported(101); } }, "Value 101 not in range 1..100");
        expectIAE("JobPrioritySupported(Integer.MIN_VALUE)", new Runnable() { public void run() { new JobPrioritySupported(Integer.MIN_VALUE); } }, null);
        check("job-priority-supported".equals(new JobPrioritySupported(1).getName()), "getName");
        check(JobPrioritySupported.class == new JobPrioritySupported(1).getCategory(), "getCategory");
        check(new JobPrioritySupported(7).equals(new JobPrioritySupported(7)), "equals same value");
        check(!new JobPrioritySupported(7).equals(new JobPrioritySupported(8)), "different value unequal");
        check(!new JobPrioritySupported(7).equals(null) && !new JobPrioritySupported(7).equals("7"), "null/foreign unequal");
        check(new JobPrioritySupported(7).hashCode() == 7, "hashCode == value");
        check(new JobPrioritySupported(7).equals(new javax.print.attribute.standard.Copies(7)) == ("JobPrioritySupported" == "Copies"), "cross IntegerSyntax subclass equals false");
        JobPrioritySupported rt = (JobPrioritySupported) serRoundTrip(new JobPrioritySupported(7));
        check(rt.equals(new JobPrioritySupported(7)) && rt.getValue() == 7, "serialization round-trip");
        check(new JobPrioritySupported(12).toString().equals("12"), "toString == value string");
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
