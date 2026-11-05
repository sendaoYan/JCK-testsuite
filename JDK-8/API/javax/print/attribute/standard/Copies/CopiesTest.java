/*
 * @test
 * @summary javax.print.attribute.standard.Copies conformance: integer attribute.
 * @run main CopiesTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class CopiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Copies v = new Copies(3);
        check(v.getValue() == 3, "getValue");
        check(v.getCategory() == Copies.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new Copies(3)), "equals");
        check(!v.equals(new Copies(5)), "not equals");
        check(v.hashCode() == new Copies(3).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: constructor range matrix / equals / serialization ----
    static void testContractDeep() throws Exception {
        check(new Copies(1).getValue() == 1, "min boundary 1 accepted");
        check(new Copies(2147483647).getValue() == 2147483647, "max boundary 2147483647 accepted");
        expectIAE("Copies(0)", new Runnable() { public void run() { new Copies(0); } }, "Value 0 not in range 1..2147483647");
        check(new Copies(Integer.MAX_VALUE).getValue() == Integer.MAX_VALUE, "Integer.MAX_VALUE accepted (no int overflow possible above)");
        expectIAE("Copies(Integer.MIN_VALUE)", new Runnable() { public void run() { new Copies(Integer.MIN_VALUE); } }, null);
        check("copies".equals(new Copies(1).getName()), "getName");
        check(Copies.class == new Copies(1).getCategory(), "getCategory");
        check(new Copies(7).equals(new Copies(7)), "equals same value");
        check(!new Copies(7).equals(new Copies(8)), "different value unequal");
        check(!new Copies(7).equals(null) && !new Copies(7).equals("7"), "null/foreign unequal");
        check(new Copies(7).hashCode() == 7, "hashCode == value");
        check(new Copies(7).equals(new javax.print.attribute.standard.Copies(7)) == ("Copies" == "Copies"), "cross IntegerSyntax subclass equals false");
        Copies rt = (Copies) serRoundTrip(new Copies(7));
        check(rt.equals(new Copies(7)) && rt.getValue() == 7, "serialization round-trip");
        check(new Copies(12).toString().equals("12"), "toString == value string");
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
