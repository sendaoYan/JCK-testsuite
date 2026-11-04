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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
