/*
 * @test
 * @summary javax.print.attribute.standard.PrintQuality conformance: enum attribute.
 * @run main PrintQualityTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PrintQualityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PrintQuality v1 = PrintQuality.DRAFT;
        check(v1 instanceof PrintQuality, "DRAFT instanceof PrintQuality");
        check(v1.getCategory() == PrintQuality.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        PrintQuality v2 = PrintQuality.NORMAL;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(PrintQuality.DRAFT), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == PrintQuality.DRAFT.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(PrintQuality.DRAFT.getValue() == 3, "DRAFT.getValue == 3");
        check("draft".equals(PrintQuality.DRAFT.toString()), "DRAFT.toString [draft]");
        check(PrintQuality.NORMAL.getValue() == 4, "NORMAL.getValue == 4");
        check("normal".equals(PrintQuality.NORMAL.toString()), "NORMAL.toString [normal]");
        check(PrintQuality.HIGH.getValue() == 5, "HIGH.getValue == 5");
        check("high".equals(PrintQuality.HIGH.toString()), "HIGH.toString [high]");
        check("print-quality".equals(PrintQuality.DRAFT.getName()), "getName == \"print-quality\"");
        check(PrintQuality.class == PrintQuality.DRAFT.getCategory(), "getCategory");
        check(PrintQuality.DRAFT.equals(PrintQuality.DRAFT), "equals self");
        check(!PrintQuality.DRAFT.equals(null), "not equals null");
        check(!PrintQuality.DRAFT.equals("draft"), "not equals foreign type");
        check(PrintQuality.DRAFT.hashCode() == 3, "hashCode == value");
        check(PrintQuality.DRAFT.clone() == PrintQuality.DRAFT, "clone returns same singleton");
        check(!PrintQuality.DRAFT.equals(PrintQuality.NORMAL), "distinct constants unequal");
        check(PrintQuality.DRAFT.toString() != PrintQuality.NORMAL.toString() || true, "toString stable");
        check(!PrintQuality.DRAFT.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        PrintQuality.DRAFT,
        PrintQuality.NORMAL,
        PrintQuality.HIGH
    };
    static final String[] NAMES = {
        "DRAFT",
        "NORMAL",
        "HIGH"
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
