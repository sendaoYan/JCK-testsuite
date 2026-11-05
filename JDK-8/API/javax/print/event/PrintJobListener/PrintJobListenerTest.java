/*
 * @test
 * @summary javax.print.event.PrintJobListener conformance: listener interface.
 * @run main PrintJobListenerTest
 */

import java.lang.reflect.*;


public class PrintJobListenerTest {
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
        Class<?> c = Class.forName("javax.print.event.PrintJobListener");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "printDataTransferCompleted"), "declares printDataTransferCompleted");
        check(hasMethod(c, "printJobCompleted"), "declares printJobCompleted");
        check(hasMethod(c, "printJobFailed"), "declares printJobFailed");
        check(hasMethod(c, "printJobCanceled"), "declares printJobCanceled");
        check(hasMethod(c, "printJobNoMoreEvents"), "declares printJobNoMoreEvents");
        check(hasMethod(c, "printJobRequiresAttention"), "declares printJobRequiresAttention");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording listener receives every callback kind ----
    static void testContractDeep() throws Exception {
        RecListener l = new RecListener();
        javax.print.event.PrintJobEvent ev = new javax.print.event.PrintJobEvent(new JL(), javax.print.event.PrintJobEvent.JOB_FAILED);
        l.printDataTransferCompleted(ev); l.printJobCanceled(ev); l.printJobCompleted(ev);
        l.printJobFailed(ev); l.printJobNoMoreEvents(ev); l.printJobRequiresAttention(ev);
        check(l.count == 6, "all six callbacks recorded [" + l.count + "]");
        check(l.last == ev, "event identity delivered");
        check(l.types.contains(Integer.valueOf(javax.print.event.PrintJobEvent.JOB_FAILED)), "type observable via event");
    }
    static class RecListener implements javax.print.event.PrintJobListener {
        int count; javax.print.event.PrintJobEvent last; final java.util.List<Integer> types = new java.util.ArrayList<Integer>();
        void rec(javax.print.event.PrintJobEvent e) { count++; last = e; types.add(Integer.valueOf(e.getPrintEventType())); }
        public void printDataTransferCompleted(javax.print.event.PrintJobEvent e) { rec(e); }
        public void printJobCanceled(javax.print.event.PrintJobEvent e) { rec(e); }
        public void printJobCompleted(javax.print.event.PrintJobEvent e) { rec(e); }
        public void printJobFailed(javax.print.event.PrintJobEvent e) { rec(e); }
        public void printJobNoMoreEvents(javax.print.event.PrintJobEvent e) { rec(e); }
        public void printJobRequiresAttention(javax.print.event.PrintJobEvent e) { rec(e); }
    }
    static class JL implements javax.print.DocPrintJob {
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
