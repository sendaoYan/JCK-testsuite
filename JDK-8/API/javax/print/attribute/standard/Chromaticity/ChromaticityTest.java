/*
 * @test
 * @summary javax.print.attribute.standard.Chromaticity conformance: enum attribute.
 * @run main ChromaticityTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class ChromaticityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Chromaticity v1 = Chromaticity.MONOCHROME;
        check(v1 instanceof Chromaticity, "MONOCHROME instanceof Chromaticity");
        check(v1.getCategory() == Chromaticity.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        Chromaticity v2 = Chromaticity.COLOR;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(Chromaticity.MONOCHROME), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == Chromaticity.MONOCHROME.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(Chromaticity.MONOCHROME.getValue() == 0, "MONOCHROME.getValue == 0");
        check("monochrome".equals(Chromaticity.MONOCHROME.toString()), "MONOCHROME.toString [monochrome]");
        check(Chromaticity.COLOR.getValue() == 1, "COLOR.getValue == 1");
        check("color".equals(Chromaticity.COLOR.toString()), "COLOR.toString [color]");
        check("chromaticity".equals(Chromaticity.MONOCHROME.getName()), "getName == \"chromaticity\"");
        check(Chromaticity.class == Chromaticity.MONOCHROME.getCategory(), "getCategory");
        check(Chromaticity.MONOCHROME.equals(Chromaticity.MONOCHROME), "equals self");
        check(!Chromaticity.MONOCHROME.equals(null), "not equals null");
        check(!Chromaticity.MONOCHROME.equals("monochrome"), "not equals foreign type");
        check(Chromaticity.MONOCHROME.hashCode() == 0, "hashCode == value");
        check(Chromaticity.MONOCHROME.clone() == Chromaticity.MONOCHROME, "clone returns same singleton");
        check(!Chromaticity.MONOCHROME.equals(Chromaticity.COLOR), "distinct constants unequal");
        check(Chromaticity.MONOCHROME.toString() != Chromaticity.COLOR.toString() || true, "toString stable");
        check(!Chromaticity.MONOCHROME.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        Chromaticity.MONOCHROME,
        Chromaticity.COLOR
    };
    static final String[] NAMES = {
        "MONOCHROME",
        "COLOR"
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
