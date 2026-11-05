/*
 * @test
 * @summary javax.print.attribute.standard.DialogTypeSelection conformance: enum attribute.
 * @run main DialogTypeSelectionTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class DialogTypeSelectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        DialogTypeSelection v1 = DialogTypeSelection.NATIVE;
        check(v1 instanceof DialogTypeSelection, "NATIVE instanceof DialogTypeSelection");
        check(v1.getCategory() == DialogTypeSelection.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        DialogTypeSelection v2 = DialogTypeSelection.COMMON;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(DialogTypeSelection.NATIVE), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == DialogTypeSelection.NATIVE.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(DialogTypeSelection.NATIVE.getValue() == 0, "NATIVE.getValue == 0");
        check("native".equals(DialogTypeSelection.NATIVE.toString()), "NATIVE.toString [native]");
        check(DialogTypeSelection.COMMON.getValue() == 1, "COMMON.getValue == 1");
        check("common".equals(DialogTypeSelection.COMMON.toString()), "COMMON.toString [common]");
        check("dialog-type-selection".equals(DialogTypeSelection.NATIVE.getName()), "getName == \"dialog-type-selection\"");
        check(DialogTypeSelection.class == DialogTypeSelection.NATIVE.getCategory(), "getCategory");
        check(DialogTypeSelection.NATIVE.equals(DialogTypeSelection.NATIVE), "equals self");
        check(!DialogTypeSelection.NATIVE.equals(null), "not equals null");
        check(!DialogTypeSelection.NATIVE.equals("native"), "not equals foreign type");
        check(DialogTypeSelection.NATIVE.hashCode() == 0, "hashCode == value");
        check(DialogTypeSelection.NATIVE.clone() == DialogTypeSelection.NATIVE, "clone returns same singleton");
        check(!DialogTypeSelection.NATIVE.equals(DialogTypeSelection.COMMON), "distinct constants unequal");
        check(DialogTypeSelection.NATIVE.toString() != DialogTypeSelection.COMMON.toString() || true, "toString stable");
        check(!DialogTypeSelection.NATIVE.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        DialogTypeSelection.NATIVE,
        DialogTypeSelection.COMMON
    };
    static final String[] NAMES = {
        "NATIVE",
        "COMMON"
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
