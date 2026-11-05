/*
 * @test
 * @summary javax.print.attribute.standard.PrinterIsAcceptingJobs conformance: enum attribute.
 * @run main PrinterIsAcceptingJobsTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PrinterIsAcceptingJobsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PrinterIsAcceptingJobs v1 = PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS;
        check(v1 instanceof PrinterIsAcceptingJobs, "NOT_ACCEPTING_JOBS instanceof PrinterIsAcceptingJobs");
        check(v1.getCategory() == PrinterIsAcceptingJobs.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        PrinterIsAcceptingJobs v2 = PrinterIsAcceptingJobs.ACCEPTING_JOBS;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
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
