/*
 * @test
 * @summary javax.print.event.PrintServiceAttributeEvent conformance: print service attribute event.
 * @run main PrintServiceAttributeEventTest
 */

import java.lang.reflect.*;
import javax.print.event.*;

public class PrintServiceAttributeEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static class StubService implements javax.print.PrintService {
        public String getName() { return "stub"; }
        public javax.print.DocPrintJob createPrintJob() { return null; }
        public void addPrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener l) { }
        public void removePrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener l) { }
        public javax.print.attribute.PrintServiceAttributeSet getAttributes() { return null; }
        public <T extends javax.print.attribute.PrintServiceAttribute> T getAttribute(Class<T> c) { return null; }
        public javax.print.DocFlavor[] getSupportedDocFlavors() { return null; }
        public boolean isDocFlavorSupported(javax.print.DocFlavor f) { return false; }
        public Class<?>[] getSupportedAttributeCategories() { return null; }
        public boolean isAttributeCategorySupported(Class<? extends javax.print.attribute.Attribute> c) { return false; }
        public Object getDefaultAttributeValue(Class<? extends javax.print.attribute.Attribute> c) { return null; }
        public Object getSupportedAttributeValues(Class<? extends javax.print.attribute.Attribute> c, javax.print.DocFlavor f, javax.print.attribute.AttributeSet s) { return null; }
        public boolean isAttributeValueSupported(javax.print.attribute.Attribute a, javax.print.DocFlavor f, javax.print.attribute.AttributeSet s) { return false; }
        public javax.print.attribute.AttributeSet getUnsupportedAttributes(javax.print.DocFlavor f, javax.print.attribute.AttributeSet s) { return null; }
        public javax.print.ServiceUIFactory getServiceUIFactory() { return null; }
    }

    public static void main(String[] a) throws Exception {
        StubService svc = new StubService();
        javax.print.attribute.PrintServiceAttributeSet attrs = new javax.print.attribute.HashPrintServiceAttributeSet();
        PrintServiceAttributeEvent e = new PrintServiceAttributeEvent(svc, attrs);
        check(e.getPrintService() == svc, "getPrintService");
        check(e.getAttributes() != null, "getAttributes non-null");
        check(e instanceof PrintEvent, "extends PrintEvent");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: service identity / defensive copy / null matrix ----
    static void testContractDeep() throws Exception {
        RecSvc svc = new RecSvc();
        javax.print.attribute.PrintServiceAttributeSet attrs = new javax.print.attribute.HashPrintServiceAttributeSet();
        attrs.add(new javax.print.attribute.standard.PrinterName("deep", null));
        javax.print.event.PrintServiceAttributeEvent e = new javax.print.event.PrintServiceAttributeEvent(svc, attrs);
        check(e.getPrintService() == svc, "service identity");
        check(e.getAttributes() != attrs, "attrs defensively copied");
        check(e.getAttributes().containsValue(new javax.print.attribute.standard.PrinterName("deep", null)), "attrs content");
        expectNPE("null attrs", new Runnable() { public void run() { new javax.print.event.PrintServiceAttributeEvent(new RecSvc(), null); } }, null);
        expectIAE("null service", new Runnable() { public void run() { new javax.print.event.PrintServiceAttributeEvent(null, new javax.print.attribute.HashPrintServiceAttributeSet()); } }, "null source");
        check(javax.print.event.PrintEvent.class.isAssignableFrom(javax.print.event.PrintServiceAttributeEvent.class), "extends PrintEvent");
    }
    static class RecSvc extends BaseSvc { public String getName() { return "EvtSvc"; } }

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
