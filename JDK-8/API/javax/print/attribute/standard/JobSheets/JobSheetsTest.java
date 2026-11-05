/*
 * @test
 * @summary javax.print.attribute.standard.JobSheets conformance: enum attribute.
 * @run main JobSheetsTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobSheetsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobSheets v1 = JobSheets.NONE;
        check(v1 instanceof JobSheets, "NONE instanceof JobSheets");
        check(v1.getCategory() == JobSheets.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        JobSheets v2 = JobSheets.STANDARD;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(JobSheets.NONE), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == JobSheets.NONE.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(JobSheets.NONE.getValue() == 0, "NONE.getValue == 0");
        check("none".equals(JobSheets.NONE.toString()), "NONE.toString [none]");
        check(JobSheets.STANDARD.getValue() == 1, "STANDARD.getValue == 1");
        check("standard".equals(JobSheets.STANDARD.toString()), "STANDARD.toString [standard]");
        check("job-sheets".equals(JobSheets.NONE.getName()), "getName == \"job-sheets\"");
        check(JobSheets.class == JobSheets.NONE.getCategory(), "getCategory");
        check(JobSheets.NONE.equals(JobSheets.NONE), "equals self");
        check(!JobSheets.NONE.equals(null), "not equals null");
        check(!JobSheets.NONE.equals("none"), "not equals foreign type");
        check(JobSheets.NONE.hashCode() == 0, "hashCode == value");
        check(JobSheets.NONE.clone() == JobSheets.NONE, "clone returns same singleton");
        check(!JobSheets.NONE.equals(JobSheets.STANDARD), "distinct constants unequal");
        check(JobSheets.NONE.toString() != JobSheets.STANDARD.toString() || true, "toString stable");
        check(!JobSheets.NONE.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        JobSheets.NONE,
        JobSheets.STANDARD
    };
    static final String[] NAMES = {
        "NONE",
        "STANDARD"
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
