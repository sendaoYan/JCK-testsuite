/*
 * @test
 * @summary javax.print.attribute.PrintJobAttribute conformance: marker interface.
 * @run main PrintJobAttributeTest
 */

import java.lang.reflect.*;


public class PrintJobAttributeTest {
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
        Class<?> c = Class.forName("javax.print.attribute.PrintJobAttribute");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasIf(c, "javax.print.attribute.Attribute"), "extends javax.print.attribute.Attribute");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: marker interface membership matrix ----
    static void testContractDeep() throws Exception {
        // DocAttribute members
        check(javax.print.attribute.DocAttribute.class.isInstance(javax.print.attribute.standard.Chromaticity.MONOCHROME), "Chromaticity is DocAttribute");
        check(!javax.print.attribute.DocAttribute.class.isInstance(new javax.print.attribute.standard.Copies(1)), "Copies is NOT DocAttribute");
        check(javax.print.attribute.DocAttribute.class.isInstance(new javax.print.attribute.standard.CopiesSupported(1, 5)) == false, "CopiesSupported is NOT DocAttribute");
        check(javax.print.attribute.DocAttribute.class.isInstance(new javax.print.attribute.standard.MediaPrintableArea(1, 1, 10, 10, 25400)), "MediaPrintableArea is DocAttribute");
        // PrintRequestAttribute members
        check(javax.print.attribute.PrintRequestAttribute.class.isInstance(new javax.print.attribute.standard.Copies(1)), "Copies is PrintRequestAttribute");
        check(javax.print.attribute.PrintRequestAttribute.class.isInstance(new javax.print.attribute.standard.JobName("j", null)), "JobName is PrintRequestAttribute");
        check(!javax.print.attribute.PrintRequestAttribute.class.isInstance(javax.print.attribute.standard.PrinterState.IDLE), "PrinterState is NOT PrintRequestAttribute");
        // PrintJobAttribute members
        check(javax.print.attribute.PrintJobAttribute.class.isInstance(new javax.print.attribute.standard.Copies(1)), "Copies is PrintJobAttribute");
        check(javax.print.attribute.PrintJobAttribute.class.isInstance(javax.print.attribute.standard.JobState.PENDING), "JobState is PrintJobAttribute");
        check(!javax.print.attribute.PrintJobAttribute.class.isInstance(new javax.print.attribute.standard.PrinterName("p", null)), "PrinterName is NOT PrintJobAttribute");
        // PrintServiceAttribute members
        check(javax.print.attribute.PrintServiceAttribute.class.isInstance(new javax.print.attribute.standard.PrinterName("p", null)), "PrinterName is PrintServiceAttribute");
        check(javax.print.attribute.PrintServiceAttribute.class.isInstance(javax.print.attribute.standard.PrinterState.IDLE), "PrinterState is PrintServiceAttribute");
        check(!javax.print.attribute.PrintServiceAttribute.class.isInstance(new javax.print.attribute.standard.Copies(1)), "Copies is NOT PrintServiceAttribute");
        // all four extend Attribute
        Class<?>[] ifaces = { javax.print.attribute.DocAttribute.class, javax.print.attribute.PrintRequestAttribute.class,
            javax.print.attribute.PrintJobAttribute.class, javax.print.attribute.PrintServiceAttribute.class };
        for (Class<?> i : ifaces)
            check(javax.print.attribute.Attribute.class.isAssignableFrom(i), i.getSimpleName() + " extends Attribute");
        // typed sets accept only matching attributes
        javax.print.attribute.HashDocAttributeSet docSet = new javax.print.attribute.HashDocAttributeSet();
        docSet.add(javax.print.attribute.standard.Chromaticity.MONOCHROME);
        try { docSet.add((javax.print.attribute.Attribute) new javax.print.attribute.standard.PrinterName("p", null)); throw new AssertionError("expected CCE"); }
        catch (ClassCastException e) { passed++; }
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
