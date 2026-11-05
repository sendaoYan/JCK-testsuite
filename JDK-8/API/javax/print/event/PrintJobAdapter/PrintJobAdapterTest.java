/*
 * @test
 * @summary javax.print.event.PrintJobAdapter conformance: listener adapter.
 * @run main PrintJobAdapterTest
 */

import java.lang.reflect.*;
import javax.print.event.*;

public class PrintJobAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static class StubJob implements javax.print.DocPrintJob {
        public javax.print.PrintService getPrintService() { return null; }
        public javax.print.attribute.PrintJobAttributeSet getAttributes() { return null; }
        public void addPrintJobListener(javax.print.event.PrintJobListener l) { }
        public void removePrintJobListener(javax.print.event.PrintJobListener l) { }
        public void addPrintJobAttributeListener(javax.print.event.PrintJobAttributeListener l, javax.print.attribute.PrintJobAttributeSet s) { }
        public void removePrintJobAttributeListener(javax.print.event.PrintJobAttributeListener l) { }
        public void print(javax.print.Doc d, javax.print.attribute.PrintRequestAttributeSet s) throws javax.print.PrintException { }
    }

    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.event.PrintJobAdapter");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(hasIf(c, "javax.print.event.PrintJobListener"), "implements PrintJobListener");
        PrintJobAdapter adapter = new PrintJobAdapter() { };
        StubJob job = new StubJob();
        PrintJobEvent e = new PrintJobEvent(job, PrintJobEvent.JOB_COMPLETE);
        adapter.printDataTransferCompleted(e);
        adapter.printJobCompleted(e);
        adapter.printJobFailed(e);
        adapter.printJobCanceled(e);
        adapter.printJobNoMoreEvents(e);
        adapter.printJobRequiresAttention(e);
        check(true, "no-op adapter methods");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: all six no-op default methods callable on anonymous subclass ----
    static void testContractDeep() throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(javax.print.event.PrintJobAdapter.class.getModifiers()), "adapter abstract");
        javax.print.event.PrintJobAdapter ad = new javax.print.event.PrintJobAdapter() {};
        javax.print.event.PrintJobEvent ev = new javax.print.event.PrintJobEvent(new AJ(), javax.print.event.PrintJobEvent.JOB_FAILED);
        ad.printDataTransferCompleted(ev);
        ad.printJobCanceled(ev);
        ad.printJobCompleted(ev);
        ad.printJobFailed(ev);
        ad.printJobNoMoreEvents(ev);
        ad.printJobRequiresAttention(ev);
        passed += 6; // all six no-op callbacks complete silently
        String[] names = { "printDataTransferCompleted", "printJobCanceled", "printJobCompleted", "printJobFailed", "printJobNoMoreEvents", "printJobRequiresAttention" };
        for (String n : names) {
            boolean found = false;
            for (java.lang.reflect.Method m : javax.print.event.PrintJobAdapter.class.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
        check(javax.print.event.PrintJobListener.class.isAssignableFrom(javax.print.event.PrintJobAdapter.class), "implements PrintJobListener");
    }
    static class AJ implements javax.print.DocPrintJob {
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
