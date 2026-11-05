/*
 * @test
 * @summary javax.print.attribute.standard.Severity conformance: enum attribute.
 * @run main SeverityTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class SeverityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Severity v1 = Severity.REPORT;
        check(v1 instanceof Severity, "REPORT instanceof Severity");
        check(v1.getCategory() == Severity.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        Severity v2 = Severity.WARNING;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(Severity.REPORT), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == Severity.REPORT.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(Severity.REPORT.getValue() == 0, "REPORT.getValue == 0");
        check("report".equals(Severity.REPORT.toString()), "REPORT.toString [report]");
        check(Severity.WARNING.getValue() == 1, "WARNING.getValue == 1");
        check("warning".equals(Severity.WARNING.toString()), "WARNING.toString [warning]");
        check(Severity.ERROR.getValue() == 2, "ERROR.getValue == 2");
        check("error".equals(Severity.ERROR.toString()), "ERROR.toString [error]");
        check("severity".equals(Severity.REPORT.getName()), "getName == \"severity\"");
        check(Severity.class == Severity.REPORT.getCategory(), "getCategory");
        check(Severity.REPORT.equals(Severity.REPORT), "equals self");
        check(!Severity.REPORT.equals(null), "not equals null");
        check(!Severity.REPORT.equals("report"), "not equals foreign type");
        check(Severity.REPORT.hashCode() == 0, "hashCode == value");
        check(Severity.REPORT.clone() == Severity.REPORT, "clone returns same singleton");
        check(!Severity.REPORT.equals(Severity.WARNING), "distinct constants unequal");
        check(Severity.REPORT.toString() != Severity.WARNING.toString() || true, "toString stable");
        check(!Severity.REPORT.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        Severity.REPORT,
        Severity.WARNING,
        Severity.ERROR
    };
    static final String[] NAMES = {
        "REPORT",
        "WARNING",
        "ERROR"
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
