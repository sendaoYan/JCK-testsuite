/*
 * @test
 * @summary javax.print.attribute.standard.JobKOctets conformance: integer attribute.
 * @run main JobKOctetsTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobKOctetsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobKOctets v = new JobKOctets(10);
        check(v.getValue() == 10, "getValue");
        check(v.getCategory() == JobKOctets.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobKOctets(10)), "equals");
        check(!v.equals(new JobKOctets(20)), "not equals");
        check(v.hashCode() == new JobKOctets(10).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: constructor range matrix / equals / serialization ----
    static void testContractDeep() throws Exception {
        check(new JobKOctets(0).getValue() == 0, "min boundary 0 accepted");
        check(new JobKOctets(2147483647).getValue() == 2147483647, "max boundary 2147483647 accepted");
        expectIAE("JobKOctets(-1)", new Runnable() { public void run() { new JobKOctets(-1); } }, "Value -1 not in range 0..2147483647");
        check(new JobKOctets(Integer.MAX_VALUE).getValue() == Integer.MAX_VALUE, "Integer.MAX_VALUE accepted (no int overflow possible above)");
        expectIAE("JobKOctets(Integer.MIN_VALUE)", new Runnable() { public void run() { new JobKOctets(Integer.MIN_VALUE); } }, null);
        check("job-k-octets".equals(new JobKOctets(0).getName()), "getName");
        check(JobKOctets.class == new JobKOctets(0).getCategory(), "getCategory");
        check(new JobKOctets(7).equals(new JobKOctets(7)), "equals same value");
        check(!new JobKOctets(7).equals(new JobKOctets(8)), "different value unequal");
        check(!new JobKOctets(7).equals(null) && !new JobKOctets(7).equals("7"), "null/foreign unequal");
        check(new JobKOctets(7).hashCode() == 7, "hashCode == value");
        check(new JobKOctets(7).equals(new javax.print.attribute.standard.Copies(7)) == ("JobKOctets" == "Copies"), "cross IntegerSyntax subclass equals false");
        JobKOctets rt = (JobKOctets) serRoundTrip(new JobKOctets(7));
        check(rt.equals(new JobKOctets(7)) && rt.getValue() == 7, "serialization round-trip");
        check(new JobKOctets(12).toString().equals("12"), "toString == value string");
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
