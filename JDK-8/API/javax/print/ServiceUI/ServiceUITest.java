/*
 * @test
 * @summary javax.print.ServiceUI conformance: service UI.
 * @run main ServiceUITest
 */

import java.lang.reflect.*;
import javax.print.ServiceUI;

public class ServiceUITest {
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
        ServiceUI ui = new ServiceUI();
        check(ui != null, "ctor");
        Class<?> c = Class.forName("javax.print.ServiceUI");
        check(hasMethod(c, "printDialog"), "declares printDialog");
        check(java.lang.reflect.Modifier.isStatic(c.getDeclaredMethod("printDialog", java.awt.GraphicsConfiguration.class, int.class, int.class, javax.print.PrintService[].class, javax.print.PrintService.class, javax.print.DocFlavor.class, javax.print.attribute.PrintRequestAttributeSet.class).getModifiers()), "printDialog static");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: headless contract + sole method signature ----
    static void testContractDeep() throws Exception {
        boolean headless = java.awt.GraphicsEnvironment.isHeadless();
        if (headless) {
            try {
                javax.print.ServiceUI.printDialog(null, 10, 10, new javax.print.PrintService[0], null, null, null);
                throw new AssertionError("expected HeadlessException");
            } catch (java.awt.HeadlessException e) { passed++; }
        } else { passed++; }
        java.lang.reflect.Method[] ms = javax.print.ServiceUI.class.getMethods();
        int dialogMethods = 0;
        for (java.lang.reflect.Method m : ms) if (m.getName().equals("printDialog")) dialogMethods++;
        check(dialogMethods == 1, "exactly one public printDialog");
        java.lang.reflect.Method m = javax.print.ServiceUI.class.getMethod("printDialog",
            java.awt.GraphicsConfiguration.class, int.class, int.class,
            javax.print.PrintService[].class, javax.print.PrintService.class, javax.print.DocFlavor.class,
            javax.print.attribute.PrintRequestAttributeSet.class);
        check(m.getReturnType() == javax.print.PrintService.class, "returns PrintService");
        check(java.awt.HeadlessException.class == m.getExceptionTypes()[0], "declares HeadlessException");
        check(!java.lang.reflect.Modifier.isAbstract(javax.print.ServiceUI.class.getModifiers()), "concrete class");
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
    // minimal PrintService adapter used by several probes
    static class BaseSvc implements javax.print.PrintService {
        public String getName() { return "RecSvc"; }
        public javax.print.DocPrintJob createPrintJob() { return null; }
        public void addPrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener l) {}
        public void removePrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener l) {}
        public javax.print.attribute.PrintServiceAttributeSet getAttributes() { return new javax.print.attribute.HashPrintServiceAttributeSet(); }
        public <T extends javax.print.attribute.PrintServiceAttribute> T getAttribute(Class<T> c) { return null; }
        public javax.print.DocFlavor[] getSupportedDocFlavors() { return new javax.print.DocFlavor[] { javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE }; }
        public boolean isDocFlavorSupported(javax.print.DocFlavor f) { return javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE.equals(f); }
        public Class<?>[] getSupportedAttributeCategories() { return new Class<?>[] { javax.print.attribute.standard.Copies.class }; }
        public boolean isAttributeCategorySupported(Class<? extends javax.print.attribute.Attribute> c) { return c == javax.print.attribute.standard.Copies.class; }
        public Object getDefaultAttributeValue(Class<? extends javax.print.attribute.Attribute> c) { return c == javax.print.attribute.standard.Copies.class ? new javax.print.attribute.standard.Copies(7) : null; }
        public Object getSupportedAttributeValues(Class<? extends javax.print.attribute.Attribute> c, javax.print.DocFlavor f, javax.print.attribute.AttributeSet as) { return c == javax.print.attribute.standard.Copies.class ? new javax.print.attribute.standard.CopiesSupported(1, 9) : null; }
        public boolean isAttributeValueSupported(javax.print.attribute.Attribute v, javax.print.DocFlavor f, javax.print.attribute.AttributeSet as) { return v instanceof javax.print.attribute.standard.Copies; }
        public javax.print.attribute.AttributeSet getUnsupportedAttributes(javax.print.DocFlavor f, javax.print.attribute.AttributeSet as) { return null; }
        public javax.print.ServiceUIFactory getServiceUIFactory() { return null; }
    }

}
