/*
 * @test
 * @summary javax.print.attribute.standard.NumberOfDocuments conformance: integer attribute.
 * @run main NumberOfDocumentsTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class NumberOfDocumentsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        NumberOfDocuments v = new NumberOfDocuments(10);
        check(v.getValue() == 10, "getValue");
        check(v.getCategory() == NumberOfDocuments.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new NumberOfDocuments(10)), "equals");
        check(!v.equals(new NumberOfDocuments(20)), "not equals");
        check(v.hashCode() == new NumberOfDocuments(10).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: constructor range matrix / equals / serialization ----
    static void testContractDeep() throws Exception {
        check(new NumberOfDocuments(0).getValue() == 0, "min boundary 0 accepted");
        check(new NumberOfDocuments(2147483647).getValue() == 2147483647, "max boundary 2147483647 accepted");
        expectIAE("NumberOfDocuments(-1)", new Runnable() { public void run() { new NumberOfDocuments(-1); } }, "Value -1 not in range 0..2147483647");
        check(new NumberOfDocuments(Integer.MAX_VALUE).getValue() == Integer.MAX_VALUE, "Integer.MAX_VALUE accepted (no int overflow possible above)");
        expectIAE("NumberOfDocuments(Integer.MIN_VALUE)", new Runnable() { public void run() { new NumberOfDocuments(Integer.MIN_VALUE); } }, null);
        check("number-of-documents".equals(new NumberOfDocuments(0).getName()), "getName");
        check(NumberOfDocuments.class == new NumberOfDocuments(0).getCategory(), "getCategory");
        check(new NumberOfDocuments(7).equals(new NumberOfDocuments(7)), "equals same value");
        check(!new NumberOfDocuments(7).equals(new NumberOfDocuments(8)), "different value unequal");
        check(!new NumberOfDocuments(7).equals(null) && !new NumberOfDocuments(7).equals("7"), "null/foreign unequal");
        check(new NumberOfDocuments(7).hashCode() == 7, "hashCode == value");
        check(new NumberOfDocuments(7).equals(new javax.print.attribute.standard.Copies(7)) == ("NumberOfDocuments" == "Copies"), "cross IntegerSyntax subclass equals false");
        NumberOfDocuments rt = (NumberOfDocuments) serRoundTrip(new NumberOfDocuments(7));
        check(rt.equals(new NumberOfDocuments(7)) && rt.getValue() == 7, "serialization round-trip");
        check(new NumberOfDocuments(12).toString().equals("12"), "toString == value string");
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
