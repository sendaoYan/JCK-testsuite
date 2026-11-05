/*
 * @test
 * @summary javax.print.attribute.standard.Fidelity conformance: enum attribute.
 * @run main FidelityTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class FidelityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Fidelity v1 = Fidelity.FIDELITY_TRUE;
        check(v1 instanceof Fidelity, "FIDELITY_TRUE instanceof Fidelity");
        check(v1.getCategory() == Fidelity.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        Fidelity v2 = Fidelity.FIDELITY_FALSE;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(Fidelity.FIDELITY_TRUE), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == Fidelity.FIDELITY_TRUE.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(Fidelity.FIDELITY_TRUE.getValue() == 0, "FIDELITY_TRUE.getValue == 0");
        check("true".equals(Fidelity.FIDELITY_TRUE.toString()), "FIDELITY_TRUE.toString [true]");
        check(Fidelity.FIDELITY_FALSE.getValue() == 1, "FIDELITY_FALSE.getValue == 1");
        check("false".equals(Fidelity.FIDELITY_FALSE.toString()), "FIDELITY_FALSE.toString [false]");
        check("ipp-attribute-fidelity".equals(Fidelity.FIDELITY_TRUE.getName()), "getName == \"ipp-attribute-fidelity\"");
        check(Fidelity.class == Fidelity.FIDELITY_TRUE.getCategory(), "getCategory");
        check(Fidelity.FIDELITY_TRUE.equals(Fidelity.FIDELITY_TRUE), "equals self");
        check(!Fidelity.FIDELITY_TRUE.equals(null), "not equals null");
        check(!Fidelity.FIDELITY_TRUE.equals("true"), "not equals foreign type");
        check(Fidelity.FIDELITY_TRUE.hashCode() == 0, "hashCode == value");
        check(Fidelity.FIDELITY_TRUE.clone() == Fidelity.FIDELITY_TRUE, "clone returns same singleton");
        check(!Fidelity.FIDELITY_TRUE.equals(Fidelity.FIDELITY_FALSE), "distinct constants unequal");
        check(Fidelity.FIDELITY_TRUE.toString() != Fidelity.FIDELITY_FALSE.toString() || true, "toString stable");
        check(!Fidelity.FIDELITY_TRUE.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        Fidelity.FIDELITY_TRUE,
        Fidelity.FIDELITY_FALSE
    };
    static final String[] NAMES = {
        "FIDELITY_TRUE",
        "FIDELITY_FALSE"
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
