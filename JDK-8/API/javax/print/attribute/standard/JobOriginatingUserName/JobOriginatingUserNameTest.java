/*
 * @test
 * @summary javax.print.attribute.standard.JobOriginatingUserName conformance: text attribute.
 * @run main JobOriginatingUserNameTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobOriginatingUserNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobOriginatingUserName v = new JobOriginatingUserName("sample-text", java.util.Locale.US);
        check("sample-text".equals(v.getValue()), "getValue");
        check(java.util.Locale.US.equals(v.getLocale()), "getLocale");
        check(v.getCategory() == JobOriginatingUserName.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobOriginatingUserName("sample-text", java.util.Locale.US)), "equals");
        check(v.hashCode() == new JobOriginatingUserName("sample-text", java.util.Locale.US).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: null NPE / locale defaulting / equals / serialization ----
    static void testContractDeep() throws Exception {
        JobOriginatingUserName v = new JobOriginatingUserName("contract", java.util.Locale.FRENCH);
        check("contract".equals(v.getValue()), "getValue");
        check(java.util.Locale.FRENCH.equals(v.getLocale()), "getLocale");
        check("contract".equals(v.toString()), "toString == value");
        check("job-originating-user-name".equals(v.getName()), "getName");
        check(JobOriginatingUserName.class == v.getCategory(), "getCategory");
        expectNPE("JobOriginatingUserName(null, locale)", new Runnable() { public void run() { new JobOriginatingUserName(null, java.util.Locale.FRENCH); } }, " value is null");
        JobOriginatingUserName dl = new JobOriginatingUserName("x", null);
        check(java.util.Locale.getDefault().equals(dl.getLocale()), "null locale -> default locale");
        check(v.equals(new JobOriginatingUserName("contract", java.util.Locale.FRENCH)), "equals same value+locale");
        check(!v.equals(new JobOriginatingUserName("contract", java.util.Locale.ENGLISH)), "different locale unequal");
        check(!v.equals(new JobOriginatingUserName("other", java.util.Locale.FRENCH)), "different value unequal");
        check(!v.equals(null) && !v.equals("contract"), "null/foreign unequal");
        check(v.hashCode() == new JobOriginatingUserName("contract", java.util.Locale.FRENCH).hashCode(), "hashCode consistent");
        JobOriginatingUserName rt = (JobOriginatingUserName) serRoundTrip(v);
        check(rt.equals(v) && "contract".equals(rt.getValue()) && java.util.Locale.FRENCH.equals(rt.getLocale()), "serialization round-trip");
        JobOriginatingUserName en = new JobOriginatingUserName("mixed", java.util.Locale.ENGLISH);
        check(!v.equals(en) && !en.equals(v), "locale asymmetry none");
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
