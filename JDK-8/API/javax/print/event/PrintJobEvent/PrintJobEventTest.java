/*
 * @test
 * @summary javax.print.event.PrintJobEvent conformance: print job event.
 * @run main PrintJobEventTest
 */

import java.lang.reflect.*;
import javax.print.event.*;

public class PrintJobEventTest {
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

    public static void main(String[] a) throws Exception {
        StubJob job = new StubJob();
        PrintJobEvent e = new PrintJobEvent(job, PrintJobEvent.JOB_COMPLETE);
        check(e.getPrintEventType() == PrintJobEvent.JOB_COMPLETE, "getPrintEventType");
        check(e.getPrintJob() == job, "getPrintJob");
        check(PrintJobEvent.JOB_CANCELED != PrintJobEvent.JOB_COMPLETE, "distinct constants");
        check(PrintJobEvent.JOB_FAILED != PrintJobEvent.REQUIRES_ATTENTION, "distinct constants 2");
        check(PrintJobEvent.NO_MORE_EVENTS != PrintJobEvent.DATA_TRANSFER_COMPLETE, "distinct constants 3");
        check(e instanceof PrintEvent, "extends PrintEvent");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: all 6 type constants / getPrintJob / arbitrary types accepted ----
    static void testContractDeep() throws Exception {
        check(javax.print.event.PrintJobEvent.JOB_CANCELED == 101, "JOB_CANCELED 101");
        check(javax.print.event.PrintJobEvent.JOB_COMPLETE == 102, "JOB_COMPLETE 102");
        check(javax.print.event.PrintJobEvent.JOB_FAILED == 103, "JOB_FAILED 103");
        check(javax.print.event.PrintJobEvent.REQUIRES_ATTENTION == 104, "REQUIRES_ATTENTION 104");
        check(javax.print.event.PrintJobEvent.NO_MORE_EVENTS == 105, "NO_MORE_EVENTS 105");
        check(javax.print.event.PrintJobEvent.DATA_TRANSFER_COMPLETE == 106, "DATA_TRANSFER_COMPLETE 106");
        javax.print.DocPrintJob job = new EvJob();
        for (int t = 101; t <= 106; t++) {
            javax.print.event.PrintJobEvent ev = new javax.print.event.PrintJobEvent(job, t);
            check(ev.getPrintEventType() == t, "type " + t);
            check(ev.getPrintJob() == job, "job identity " + t);
            check(ev.toString().startsWith("PrintEvent on "), "toString inherits");
        }
        javax.print.event.PrintJobEvent odd = new javax.print.event.PrintJobEvent(job, 999);
        check(odd.getPrintEventType() == 999, "arbitrary type value accepted");
        expectIAE("null source", new Runnable() { public void run() { new javax.print.event.PrintJobEvent(null, 101); } }, "null source");
    }
    static class EvJob implements javax.print.DocPrintJob {
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
