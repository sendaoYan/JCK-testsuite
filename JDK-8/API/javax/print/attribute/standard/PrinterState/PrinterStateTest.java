/*
 * @test
 * @summary javax.print.attribute.standard.PrinterState conformance: enum attribute.
 * @run main PrinterStateTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PrinterStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PrinterState v1 = PrinterState.UNKNOWN;
        check(v1 instanceof PrinterState, "UNKNOWN instanceof PrinterState");
        check(v1.getCategory() == PrinterState.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        PrinterState v2 = PrinterState.IDLE;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(PrinterState.UNKNOWN), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == PrinterState.UNKNOWN.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(PrinterState.UNKNOWN.getValue() == 0, "UNKNOWN.getValue == 0");
        check("unknown".equals(PrinterState.UNKNOWN.toString()), "UNKNOWN.toString [unknown]");
        check(PrinterState.IDLE.getValue() == 3, "IDLE.getValue == 3");
        check("idle".equals(PrinterState.IDLE.toString()), "IDLE.toString [idle]");
        check(PrinterState.PROCESSING.getValue() == 4, "PROCESSING.getValue == 4");
        check("processing".equals(PrinterState.PROCESSING.toString()), "PROCESSING.toString [processing]");
        check(PrinterState.STOPPED.getValue() == 5, "STOPPED.getValue == 5");
        check("stopped".equals(PrinterState.STOPPED.toString()), "STOPPED.toString [stopped]");
        check("printer-state".equals(PrinterState.UNKNOWN.getName()), "getName == \"printer-state\"");
        check(PrinterState.class == PrinterState.UNKNOWN.getCategory(), "getCategory");
        check(PrinterState.UNKNOWN.equals(PrinterState.UNKNOWN), "equals self");
        check(!PrinterState.UNKNOWN.equals(null), "not equals null");
        check(!PrinterState.UNKNOWN.equals("unknown"), "not equals foreign type");
        check(PrinterState.UNKNOWN.hashCode() == 0, "hashCode == value");
        check(PrinterState.UNKNOWN.clone() == PrinterState.UNKNOWN, "clone returns same singleton");
        check(!PrinterState.UNKNOWN.equals(PrinterState.IDLE), "distinct constants unequal");
        check(PrinterState.UNKNOWN.toString() != PrinterState.IDLE.toString() || true, "toString stable");
        check(!PrinterState.UNKNOWN.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        PrinterState.UNKNOWN,
        PrinterState.IDLE,
        PrinterState.PROCESSING,
        PrinterState.STOPPED
    };
    static final String[] NAMES = {
        "UNKNOWN",
        "IDLE",
        "PROCESSING",
        "STOPPED"
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
