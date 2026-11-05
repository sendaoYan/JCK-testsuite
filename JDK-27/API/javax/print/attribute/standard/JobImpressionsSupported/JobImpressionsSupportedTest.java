/*
 * @test
 * @summary javax.print.attribute.standard.JobImpressionsSupported conformance: white-box structure + black-box behavior.
 * @run main JobImpressionsSupportedTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class JobImpressionsSupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.JobImpressionsSupported");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(SetOfIntegerSyntax.class.isAssignableFrom(c), "extends SetOfIntegerSyntax");
        check(SupportedValuesAttribute.class.isAssignableFrom(c), "implements SupportedValuesAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JobImpressionsSupported p = new JobImpressionsSupported(1, 3);
        check("job-impressions-supported".equals(p.getName()), "getName");
        check(p.getCategory() == JobImpressionsSupported.class, "getCategory");
        int[][] m = p.getMembers();
        check(m.length == 1 && m[0][0] == 1 && m[0][1] == 3, "getMembers");
        check("1-3".equals(p.toString()), "toString");
        check(p.contains(1) && p.contains(2) && p.contains(3), "contains in range");
        check(!p.contains(0) && !p.contains(4), "contains out of range");
        check(p.contains(new Copies(2)), "contains(IntegerSyntax)");
        check(p.next(-1) == 1 && p.next(1) == 2 && p.next(3) == -1, "next");
        check(p.equals(new JobImpressionsSupported(1, 3)), "equals same");
        check(!p.equals(new JobImpressionsSupported(1, 4)), "different unequal");
        check(!p.equals(null) && !p.equals("x"), "not equals null/foreign");
        check(p.hashCode() == new JobImpressionsSupported(1, 3).hashCode(), "hashCode consistent");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: range parsing / members normalization / contains / next / serialization ----
    static void testContractDeep() throws Exception {
        JobImpressionsSupported pr = new JobImpressionsSupported(1, 11);
        check(java.util.Arrays.deepEquals(pr.getMembers(), new int[][] {{1,11}}), "members from (lo,hi)");
        check(pr.contains(1) && pr.contains(11) && !pr.contains(0) && !pr.contains(12), "contains boundaries");
        check(pr.next(0) == 1 && pr.next(11) == -1, "next boundaries");
        expectIAE("JobImpressionsSupported(5,1)", new Runnable() { public void run() { new JobImpressionsSupported(5, 1); } }, "Null range specified");
        check("job-impressions-supported".equals(pr.getName()), "getName");
        check(JobImpressionsSupported.class == pr.getCategory(), "getCategory");
        check(pr.equals(new JobImpressionsSupported(1, 11)), "equals same range");
        check(!pr.equals(new JobImpressionsSupported(2, 11)), "different range unequal");
        check(pr.hashCode() == new JobImpressionsSupported(1, 11).hashCode(), "hashCode consistent");
        check(!pr.equals(null) && !pr.equals("1:3"), "null/foreign unequal");
        JobImpressionsSupported rt = (JobImpressionsSupported) serRoundTrip(pr);
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
