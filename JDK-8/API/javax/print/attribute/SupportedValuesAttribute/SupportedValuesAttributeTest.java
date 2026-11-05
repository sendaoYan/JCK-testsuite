/*
 * @test
 * @summary javax.print.attribute.SupportedValuesAttribute conformance: marker interface.
 * @run main SupportedValuesAttributeTest
 */

import java.lang.reflect.*;


public class SupportedValuesAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.attribute.SupportedValuesAttribute");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasIf(c, "javax.print.attribute.Attribute"), "extends javax.print.attribute.Attribute");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: SupportedValuesAttribute membership ----
    static void testContractDeep() throws Exception {
        javax.print.attribute.SupportedValuesAttribute v = new javax.print.attribute.standard.CopiesSupported(1, 99);
        check(v instanceof javax.print.attribute.Attribute, "extends Attribute");
        check(javax.print.attribute.standard.CopiesSupported.class == v.getCategory(), "getCategory");
        check("copies-supported".equals(v.getName()), "getName");
        javax.print.attribute.SupportedValuesAttribute[] others = {
            new javax.print.attribute.standard.NumberUpSupported(1, 4),
            new javax.print.attribute.standard.JobImpressionsSupported(0, 1000),
            new javax.print.attribute.standard.JobKOctetsSupported(0, 1000),
            new javax.print.attribute.standard.JobMediaSheetsSupported(0, 100) };
        String[] names = { "number-up-supported", "job-impressions-supported", "job-k-octets-supported", "job-media-sheets-supported" };
        for (int i = 0; i < others.length; i++) check(names[i].equals(others[i].getName()), names[i]);
        check(!javax.print.attribute.SupportedValuesAttribute.class.isInstance(new javax.print.attribute.standard.Copies(1)), "Copies is not SupportedValues");
        Object rt = serRoundTrip(v);
        check(rt.equals(v), "serialization round-trip");
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
