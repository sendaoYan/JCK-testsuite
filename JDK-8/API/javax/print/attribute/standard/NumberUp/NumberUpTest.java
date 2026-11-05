/*
 * @test
 * @summary javax.print.attribute.standard.NumberUp conformance: integer attribute.
 * @run main NumberUpTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class NumberUpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        NumberUp v = new NumberUp(2);
        check(v.getValue() == 2, "getValue");
        check(v.getCategory() == NumberUp.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new NumberUp(2)), "equals");
        check(!v.equals(new NumberUp(4)), "not equals");
        check(v.hashCode() == new NumberUp(2).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: constructor range matrix / equals / serialization ----
    static void testContractDeep() throws Exception {
        check(new NumberUp(1).getValue() == 1, "min boundary 1 accepted");
        check(new NumberUp(2147483647).getValue() == 2147483647, "max boundary 2147483647 accepted");
        expectIAE("NumberUp(0)", new Runnable() { public void run() { new NumberUp(0); } }, "Value 0 not in range 1..2147483647");
        check(new NumberUp(Integer.MAX_VALUE).getValue() == Integer.MAX_VALUE, "Integer.MAX_VALUE accepted (no int overflow possible above)");
        expectIAE("NumberUp(Integer.MIN_VALUE)", new Runnable() { public void run() { new NumberUp(Integer.MIN_VALUE); } }, null);
        check("number-up".equals(new NumberUp(1).getName()), "getName");
        check(NumberUp.class == new NumberUp(1).getCategory(), "getCategory");
        check(new NumberUp(7).equals(new NumberUp(7)), "equals same value");
        check(!new NumberUp(7).equals(new NumberUp(8)), "different value unequal");
        check(!new NumberUp(7).equals(null) && !new NumberUp(7).equals("7"), "null/foreign unequal");
        check(new NumberUp(7).hashCode() == 7, "hashCode == value");
        check(new NumberUp(7).equals(new javax.print.attribute.standard.Copies(7)) == ("NumberUp" == "Copies"), "cross IntegerSyntax subclass equals false");
        NumberUp rt = (NumberUp) serRoundTrip(new NumberUp(7));
        check(rt.equals(new NumberUp(7)) && rt.getValue() == 7, "serialization round-trip");
        check(new NumberUp(12).toString().equals("12"), "toString == value string");
    }

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
