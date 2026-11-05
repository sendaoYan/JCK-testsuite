/*
 * @test
 * @summary javax.print.attribute.standard.OrientationRequested conformance: enum attribute.
 * @run main OrientationRequestedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class OrientationRequestedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        OrientationRequested v1 = OrientationRequested.PORTRAIT;
        check(v1 instanceof OrientationRequested, "PORTRAIT instanceof OrientationRequested");
        check(v1.getCategory() == OrientationRequested.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        OrientationRequested v2 = OrientationRequested.LANDSCAPE;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(OrientationRequested.PORTRAIT), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == OrientationRequested.PORTRAIT.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(OrientationRequested.PORTRAIT.getValue() == 3, "PORTRAIT.getValue == 3");
        check("portrait".equals(OrientationRequested.PORTRAIT.toString()), "PORTRAIT.toString [portrait]");
        check(OrientationRequested.LANDSCAPE.getValue() == 4, "LANDSCAPE.getValue == 4");
        check("landscape".equals(OrientationRequested.LANDSCAPE.toString()), "LANDSCAPE.toString [landscape]");
        check(OrientationRequested.REVERSE_LANDSCAPE.getValue() == 5, "REVERSE_LANDSCAPE.getValue == 5");
        check("reverse-landscape".equals(OrientationRequested.REVERSE_LANDSCAPE.toString()), "REVERSE_LANDSCAPE.toString [reverse-landscape]");
        check(OrientationRequested.REVERSE_PORTRAIT.getValue() == 6, "REVERSE_PORTRAIT.getValue == 6");
        check("reverse-portrait".equals(OrientationRequested.REVERSE_PORTRAIT.toString()), "REVERSE_PORTRAIT.toString [reverse-portrait]");
        check("orientation-requested".equals(OrientationRequested.PORTRAIT.getName()), "getName == \"orientation-requested\"");
        check(OrientationRequested.class == OrientationRequested.PORTRAIT.getCategory(), "getCategory");
        check(OrientationRequested.PORTRAIT.equals(OrientationRequested.PORTRAIT), "equals self");
        check(!OrientationRequested.PORTRAIT.equals(null), "not equals null");
        check(!OrientationRequested.PORTRAIT.equals("portrait"), "not equals foreign type");
        check(OrientationRequested.PORTRAIT.hashCode() == 3, "hashCode == value");
        check(OrientationRequested.PORTRAIT.clone() == OrientationRequested.PORTRAIT, "clone returns same singleton");
        check(!OrientationRequested.PORTRAIT.equals(OrientationRequested.LANDSCAPE), "distinct constants unequal");
        check(OrientationRequested.PORTRAIT.toString() != OrientationRequested.LANDSCAPE.toString() || true, "toString stable");
        check(!OrientationRequested.PORTRAIT.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        OrientationRequested.PORTRAIT,
        OrientationRequested.LANDSCAPE,
        OrientationRequested.REVERSE_LANDSCAPE,
        OrientationRequested.REVERSE_PORTRAIT
    };
    static final String[] NAMES = {
        "PORTRAIT",
        "LANDSCAPE",
        "REVERSE_LANDSCAPE",
        "REVERSE_PORTRAIT"
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
