/*
 * @test
 * @summary javax.print.MultiDocPrintJob conformance: interface.
 * @run main MultiDocPrintJobTest
 */

import java.lang.reflect.*;


public class MultiDocPrintJobTest {
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
        Class<?> c = Class.forName("javax.print.MultiDocPrintJob");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasIf(c, "javax.print.DocPrintJob"), "extends javax.print.DocPrintJob");
        check(hasMethod(c, "print"), "declares print");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: MultiDoc overload is distinct from Doc overload ----
    static void testContractDeep() throws Exception {
        check(javax.print.DocPrintJob.class.isAssignableFrom(javax.print.MultiDocPrintJob.class), "extends DocPrintJob");
        javax.print.MultiDocPrintJob.class.getMethod("print", javax.print.MultiDoc.class, javax.print.attribute.PrintRequestAttributeSet.class);
        passed++;
        javax.print.DocPrintJob.class.getMethod("print", javax.print.Doc.class, javax.print.attribute.PrintRequestAttributeSet.class);
        passed++;
        RecMultiJob job = new RecMultiJob();
        javax.print.SimpleDoc d1 = new javax.print.SimpleDoc("one", javax.print.DocFlavor.STRING.TEXT_PLAIN, null);
        javax.print.SimpleDoc d2 = new javax.print.SimpleDoc("two", javax.print.DocFlavor.STRING.TEXT_PLAIN, null);
        javax.print.MultiDoc chain = new Chain(d1, new Chain(d2, null));
        job.print(chain, null);
        check(job.count == 2, "walked whole chain [" + job.count + "]");
        check(job.first == d1, "first doc identity");
    }
    static class Chain implements javax.print.MultiDoc {
        final javax.print.Doc doc; final javax.print.MultiDoc next;
        Chain(javax.print.Doc d, javax.print.MultiDoc n) { doc = d; next = n; }
        public javax.print.Doc getDoc() { return doc; }
        public javax.print.MultiDoc next() { return next; }
    }
    static class RecMultiJob implements javax.print.MultiDocPrintJob {
        int count; javax.print.Doc first;
        public void print(javax.print.MultiDoc md, javax.print.attribute.PrintRequestAttributeSet a) throws javax.print.PrintException {
            try {
            javax.print.MultiDoc cur = md;
            while (cur != null) { if (first == null) first = cur.getDoc(); count++; cur = cur.next(); }
        } catch (java.io.IOException e) { throw new javax.print.PrintException(e);
        }
        }
        public javax.print.PrintService getPrintService() { return null; }
        public javax.print.attribute.PrintJobAttributeSet getAttributes() { return new javax.print.attribute.HashPrintJobAttributeSet(); }
        public void print(javax.print.Doc d, javax.print.attribute.PrintRequestAttributeSet a) {}
        public void addPrintJobListener(javax.print.event.PrintJobListener l) {}
        public void removePrintJobListener(javax.print.event.PrintJobListener l) {}
        public void addPrintJobAttributeListener(javax.print.event.PrintJobAttributeListener l, javax.print.attribute.PrintJobAttributeSet a) {}
        public void removePrintJobAttributeListener(javax.print.event.PrintJobAttributeListener l) {}
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
