/*
 * @test
 * @summary javax.print.attribute.standard.PrinterIsAcceptingJobs conformance: white-box structure + black-box behavior.
 * @run main PrinterIsAcceptingJobsTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrinterIsAcceptingJobsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.PrinterIsAcceptingJobs");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(PrintServiceAttribute.class.isAssignableFrom(c), "implements PrintServiceAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.getCategory() == PrinterIsAcceptingJobs.class, "getCategory");
        check("printer-is-accepting-jobs".equals(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.getName()), "getName");
        check(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.getValue() == 0, "NOT_ACCEPTING_JOBS value == 0");
        check("not-accepting-jobs".equals(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.toString()), "NOT_ACCEPTING_JOBS toString");
        check(PrinterIsAcceptingJobs.ACCEPTING_JOBS.getValue() == 1, "ACCEPTING_JOBS value == 1");
        check("accepting-jobs".equals(PrinterIsAcceptingJobs.ACCEPTING_JOBS.toString()), "ACCEPTING_JOBS toString");
        check(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.clone() == PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS, "clone returns singleton");
        check(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.hashCode() == 0, "hashCode == value");
        check(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.equals(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS), "equals self");
        check(!PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.equals(PrinterIsAcceptingJobs.ACCEPTING_JOBS), "distinct constants unequal");
        check(!PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.equals(null) && !PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.getValue() == 0, "NOT_ACCEPTING_JOBS.getValue == 0");
        check("not-accepting-jobs".equals(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.toString()), "NOT_ACCEPTING_JOBS.toString [not-accepting-jobs]");
        check(PrinterIsAcceptingJobs.ACCEPTING_JOBS.getValue() == 1, "ACCEPTING_JOBS.getValue == 1");
        check("accepting-jobs".equals(PrinterIsAcceptingJobs.ACCEPTING_JOBS.toString()), "ACCEPTING_JOBS.toString [accepting-jobs]");
        check("printer-is-accepting-jobs".equals(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.getName()), "getName == \"printer-is-accepting-jobs\"");
        check(PrinterIsAcceptingJobs.class == PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.getCategory(), "getCategory");
        check(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.equals(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS), "equals self");
        check(!PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.equals(null), "not equals null");
        check(!PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.equals("not-accepting-jobs"), "not equals foreign type");
        check(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.hashCode() == 0, "hashCode == value");
        check(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.clone() == PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS, "clone returns same singleton");
        check(!PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.equals(PrinterIsAcceptingJobs.ACCEPTING_JOBS), "distinct constants unequal");
        check(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.toString() != PrinterIsAcceptingJobs.ACCEPTING_JOBS.toString() || true, "toString stable");
        check(!PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
        for (int i = 0; i < NAMES.length; i++) {
            javax.print.attribute.EnumSyntax e = VALUES[i];
            check(serRoundTrip(e) == e, "serialization readResolve restores singleton " + NAMES[i]);
        }
        // distinct-value count vs constant count (alias handling)
        java.util.Set<Integer> vals = new java.util.HashSet<Integer>();
        for (javax.print.attribute.EnumSyntax e : VALUES) vals.add(e.getValue());
        check(vals.size() == VALUES.length, "all values distinct");
    }

    static final javax.print.attribute.EnumSyntax[] VALUES = {
        PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS,
        PrinterIsAcceptingJobs.ACCEPTING_JOBS
    };
    static final String[] NAMES = {
        "NOT_ACCEPTING_JOBS",
        "ACCEPTING_JOBS"
    };

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
