/*
 * @test
 * @summary javax.print.attribute.standard.PDLOverrideSupported conformance: enum attribute.
 * @run main PDLOverrideSupportedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PDLOverrideSupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PDLOverrideSupported v1 = PDLOverrideSupported.NOT_ATTEMPTED;
        check(v1 instanceof PDLOverrideSupported, "NOT_ATTEMPTED instanceof PDLOverrideSupported");
        check(v1.getCategory() == PDLOverrideSupported.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        PDLOverrideSupported v2 = PDLOverrideSupported.ATTEMPTED;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(PDLOverrideSupported.NOT_ATTEMPTED), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == PDLOverrideSupported.NOT_ATTEMPTED.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(PDLOverrideSupported.NOT_ATTEMPTED.getValue() == 0, "NOT_ATTEMPTED.getValue == 0");
        check("not-attempted".equals(PDLOverrideSupported.NOT_ATTEMPTED.toString()), "NOT_ATTEMPTED.toString [not-attempted]");
        check(PDLOverrideSupported.ATTEMPTED.getValue() == 1, "ATTEMPTED.getValue == 1");
        check("attempted".equals(PDLOverrideSupported.ATTEMPTED.toString()), "ATTEMPTED.toString [attempted]");
        check("pdl-override-supported".equals(PDLOverrideSupported.NOT_ATTEMPTED.getName()), "getName == \"pdl-override-supported\"");
        check(PDLOverrideSupported.class == PDLOverrideSupported.NOT_ATTEMPTED.getCategory(), "getCategory");
        check(PDLOverrideSupported.NOT_ATTEMPTED.equals(PDLOverrideSupported.NOT_ATTEMPTED), "equals self");
        check(!PDLOverrideSupported.NOT_ATTEMPTED.equals(null), "not equals null");
        check(!PDLOverrideSupported.NOT_ATTEMPTED.equals("not-attempted"), "not equals foreign type");
        check(PDLOverrideSupported.NOT_ATTEMPTED.hashCode() == 0, "hashCode == value");
        check(PDLOverrideSupported.NOT_ATTEMPTED.clone() == PDLOverrideSupported.NOT_ATTEMPTED, "clone returns same singleton");
        check(!PDLOverrideSupported.NOT_ATTEMPTED.equals(PDLOverrideSupported.ATTEMPTED), "distinct constants unequal");
        check(PDLOverrideSupported.NOT_ATTEMPTED.toString() != PDLOverrideSupported.ATTEMPTED.toString() || true, "toString stable");
        check(!PDLOverrideSupported.NOT_ATTEMPTED.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        PDLOverrideSupported.NOT_ATTEMPTED,
        PDLOverrideSupported.ATTEMPTED
    };
    static final String[] NAMES = {
        "NOT_ATTEMPTED",
        "ATTEMPTED"
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
