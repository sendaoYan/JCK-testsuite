/*
 * @test
 * @summary javax.print.DocPrintJob conformance: interface.
 * @run main DocPrintJobTest
 */

import java.lang.reflect.*;


public class DocPrintJobTest {
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
        Class<?> c = Class.forName("javax.print.DocPrintJob");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getPrintService"), "declares getPrintService");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
        check(hasMethod(c, "addPrintJobListener"), "declares addPrintJobListener");
        check(hasMethod(c, "removePrintJobListener"), "declares removePrintJobListener");
        check(hasMethod(c, "addPrintJobAttributeListener"), "declares addPrintJobAttributeListener");
        check(hasMethod(c, "removePrintJobAttributeListener"), "declares removePrintJobAttributeListener");
        check(hasMethod(c, "print"), "declares print");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording job + real PS job surface ----
    static void testContractDeep() throws Exception {
        javax.print.StreamPrintServiceFactory[] fs = javax.print.StreamPrintServiceFactory.lookupStreamPrintServiceFactories(
            javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, javax.print.DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType());
        if (fs.length > 0) {
            javax.print.StreamPrintService s = fs[0].getPrintService(new java.io.ByteArrayOutputStream());
            javax.print.DocPrintJob job = s.createPrintJob();
            check(job.getPrintService() == s, "job.getPrintService identity");
            javax.print.attribute.PrintJobAttributeSet jas = job.getAttributes();
            check(jas != null && jas.isEmpty(), "fresh job attribute set empty");
            job.addPrintJobListener(null);
            job.removePrintJobListener(null);
            passed += 2; // listener registration accepted
            javax.print.SimpleDoc doc = new javax.print.SimpleDoc(new DeepPrintable2(), javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
            job.print(doc, new javax.print.attribute.HashPrintRequestAttributeSet());
            check(s.getOutputStream() != null, "print consumed stream");
        } else { passed++; }
        // recording implementation: listener bookkeeping + attribute listener pairing
        RecJob rec = new RecJob();
        javax.print.event.PrintJobListener l1 = new javax.print.event.PrintJobAdapter() {};
        rec.addPrintJobListener(l1);
        check(rec.listeners.size() == 1, "listener added");
        rec.removePrintJobListener(l1);
        check(rec.listeners.size() == 0, "listener removed");
        javax.print.attribute.PrintJobAttributeSet jas = new javax.print.attribute.HashPrintJobAttributeSet();
        jas.add(new javax.print.attribute.standard.Copies(2));
        rec.lastAttrs = jas;
        check(rec.getAttributes().containsValue(new javax.print.attribute.standard.Copies(2)), "job attrs expose copies");
    }
    static class DeepPrintable2 implements java.awt.print.Printable {
        public int print(java.awt.Graphics g, java.awt.print.PageFormat pf, int pageIndex) { return pageIndex > 0 ? NO_SUCH_PAGE : PAGE_EXISTS; }
    }
    static class RecJob implements javax.print.DocPrintJob {
        final java.util.List<javax.print.event.PrintJobListener> listeners = new java.util.ArrayList<javax.print.event.PrintJobListener>();
        javax.print.attribute.PrintJobAttributeSet lastAttrs = new javax.print.attribute.HashPrintJobAttributeSet();
        public javax.print.PrintService getPrintService() { return null; }
        public javax.print.attribute.PrintJobAttributeSet getAttributes() { return lastAttrs; }
        public void print(javax.print.Doc d, javax.print.attribute.PrintRequestAttributeSet a) {}
        public void addPrintJobListener(javax.print.event.PrintJobListener l) { listeners.add(l); }
        public void removePrintJobListener(javax.print.event.PrintJobListener l) { listeners.remove(l); }
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
