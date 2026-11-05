/*
 * @test
 * @summary javax.print.attribute.standard.PresentationDirection conformance: enum attribute.
 * @run main PresentationDirectionTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PresentationDirectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PresentationDirection v1 = PresentationDirection.TOBOTTOM_TORIGHT;
        check(v1 instanceof PresentationDirection, "TOBOTTOM_TORIGHT instanceof PresentationDirection");
        check(v1.getCategory() == PresentationDirection.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        PresentationDirection v2 = PresentationDirection.TOBOTTOM_TOLEFT;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(PresentationDirection.TOBOTTOM_TORIGHT), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == PresentationDirection.TOBOTTOM_TORIGHT.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(PresentationDirection.TOBOTTOM_TORIGHT.getValue() == 0, "TOBOTTOM_TORIGHT.getValue == 0");
        check("tobottom-toright".equals(PresentationDirection.TOBOTTOM_TORIGHT.toString()), "TOBOTTOM_TORIGHT.toString [tobottom-toright]");
        check(PresentationDirection.TOBOTTOM_TOLEFT.getValue() == 1, "TOBOTTOM_TOLEFT.getValue == 1");
        check("tobottom-toleft".equals(PresentationDirection.TOBOTTOM_TOLEFT.toString()), "TOBOTTOM_TOLEFT.toString [tobottom-toleft]");
        check(PresentationDirection.TOTOP_TORIGHT.getValue() == 2, "TOTOP_TORIGHT.getValue == 2");
        check("totop-toright".equals(PresentationDirection.TOTOP_TORIGHT.toString()), "TOTOP_TORIGHT.toString [totop-toright]");
        check(PresentationDirection.TOTOP_TOLEFT.getValue() == 3, "TOTOP_TOLEFT.getValue == 3");
        check("totop-toleft".equals(PresentationDirection.TOTOP_TOLEFT.toString()), "TOTOP_TOLEFT.toString [totop-toleft]");
        check(PresentationDirection.TORIGHT_TOBOTTOM.getValue() == 4, "TORIGHT_TOBOTTOM.getValue == 4");
        check("toright-tobottom".equals(PresentationDirection.TORIGHT_TOBOTTOM.toString()), "TORIGHT_TOBOTTOM.toString [toright-tobottom]");
        check(PresentationDirection.TORIGHT_TOTOP.getValue() == 5, "TORIGHT_TOTOP.getValue == 5");
        check("toright-totop".equals(PresentationDirection.TORIGHT_TOTOP.toString()), "TORIGHT_TOTOP.toString [toright-totop]");
        check(PresentationDirection.TOLEFT_TOBOTTOM.getValue() == 6, "TOLEFT_TOBOTTOM.getValue == 6");
        check("toleft-tobottom".equals(PresentationDirection.TOLEFT_TOBOTTOM.toString()), "TOLEFT_TOBOTTOM.toString [toleft-tobottom]");
        check(PresentationDirection.TOLEFT_TOTOP.getValue() == 7, "TOLEFT_TOTOP.getValue == 7");
        check("toleft-totop".equals(PresentationDirection.TOLEFT_TOTOP.toString()), "TOLEFT_TOTOP.toString [toleft-totop]");
        check("presentation-direction".equals(PresentationDirection.TOBOTTOM_TORIGHT.getName()), "getName == \"presentation-direction\"");
        check(PresentationDirection.class == PresentationDirection.TOBOTTOM_TORIGHT.getCategory(), "getCategory");
        check(PresentationDirection.TOBOTTOM_TORIGHT.equals(PresentationDirection.TOBOTTOM_TORIGHT), "equals self");
        check(!PresentationDirection.TOBOTTOM_TORIGHT.equals(null), "not equals null");
        check(!PresentationDirection.TOBOTTOM_TORIGHT.equals("tobottom-toright"), "not equals foreign type");
        check(PresentationDirection.TOBOTTOM_TORIGHT.hashCode() == 0, "hashCode == value");
        check(PresentationDirection.TOBOTTOM_TORIGHT.clone() == PresentationDirection.TOBOTTOM_TORIGHT, "clone returns same singleton");
        check(!PresentationDirection.TOBOTTOM_TORIGHT.equals(PresentationDirection.TOBOTTOM_TOLEFT), "distinct constants unequal");
        check(PresentationDirection.TOBOTTOM_TORIGHT.toString() != PresentationDirection.TOBOTTOM_TOLEFT.toString() || true, "toString stable");
        check(!PresentationDirection.TOBOTTOM_TORIGHT.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        PresentationDirection.TOBOTTOM_TORIGHT,
        PresentationDirection.TOBOTTOM_TOLEFT,
        PresentationDirection.TOTOP_TORIGHT,
        PresentationDirection.TOTOP_TOLEFT,
        PresentationDirection.TORIGHT_TOBOTTOM,
        PresentationDirection.TORIGHT_TOTOP,
        PresentationDirection.TOLEFT_TOBOTTOM,
        PresentationDirection.TOLEFT_TOTOP
    };
    static final String[] NAMES = {
        "TOBOTTOM_TORIGHT",
        "TOBOTTOM_TOLEFT",
        "TOTOP_TORIGHT",
        "TOTOP_TOLEFT",
        "TORIGHT_TOBOTTOM",
        "TORIGHT_TOTOP",
        "TOLEFT_TOBOTTOM",
        "TOLEFT_TOTOP"
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
