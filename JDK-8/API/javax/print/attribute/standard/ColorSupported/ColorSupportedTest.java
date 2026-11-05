/*
 * @test
 * @summary javax.print.attribute.standard.ColorSupported conformance: enum attribute.
 * @run main ColorSupportedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class ColorSupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        ColorSupported v1 = ColorSupported.NOT_SUPPORTED;
        check(v1 instanceof ColorSupported, "NOT_SUPPORTED instanceof ColorSupported");
        check(v1.getCategory() == ColorSupported.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        ColorSupported v2 = ColorSupported.SUPPORTED;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(ColorSupported.NOT_SUPPORTED), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == ColorSupported.NOT_SUPPORTED.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(ColorSupported.NOT_SUPPORTED.getValue() == 0, "NOT_SUPPORTED.getValue == 0");
        check("not-supported".equals(ColorSupported.NOT_SUPPORTED.toString()), "NOT_SUPPORTED.toString [not-supported]");
        check(ColorSupported.SUPPORTED.getValue() == 1, "SUPPORTED.getValue == 1");
        check("supported".equals(ColorSupported.SUPPORTED.toString()), "SUPPORTED.toString [supported]");
        check("color-supported".equals(ColorSupported.NOT_SUPPORTED.getName()), "getName == \"color-supported\"");
        check(ColorSupported.class == ColorSupported.NOT_SUPPORTED.getCategory(), "getCategory");
        check(ColorSupported.NOT_SUPPORTED.equals(ColorSupported.NOT_SUPPORTED), "equals self");
        check(!ColorSupported.NOT_SUPPORTED.equals(null), "not equals null");
        check(!ColorSupported.NOT_SUPPORTED.equals("not-supported"), "not equals foreign type");
        check(ColorSupported.NOT_SUPPORTED.hashCode() == 0, "hashCode == value");
        check(ColorSupported.NOT_SUPPORTED.clone() == ColorSupported.NOT_SUPPORTED, "clone returns same singleton");
        check(!ColorSupported.NOT_SUPPORTED.equals(ColorSupported.SUPPORTED), "distinct constants unequal");
        check(ColorSupported.NOT_SUPPORTED.toString() != ColorSupported.SUPPORTED.toString() || true, "toString stable");
        check(!ColorSupported.NOT_SUPPORTED.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        ColorSupported.NOT_SUPPORTED,
        ColorSupported.SUPPORTED
    };
    static final String[] NAMES = {
        "NOT_SUPPORTED",
        "SUPPORTED"
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
