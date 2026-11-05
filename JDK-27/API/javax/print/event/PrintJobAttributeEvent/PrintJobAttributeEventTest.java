/*
 * @test
 * @summary javax.print.event.PrintJobAttributeEvent conformance: white-box structure + black-box behavior.
 * @run main PrintJobAttributeEventTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrintJobAttributeEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static DocPrintJob docJob() {
        return new DocPrintJob() {
            public PrintService getPrintService() { return null; }
            public PrintJobAttributeSet getAttributes() { return null; }
            public void addPrintJobListener(PrintJobListener l) {}
            public void removePrintJobListener(PrintJobListener l) {}
            public void addPrintJobAttributeListener(PrintJobAttributeListener l, PrintJobAttributeSet a) {}
            public void removePrintJobAttributeListener(PrintJobAttributeListener l) {}
            public void print(Doc d, PrintRequestAttributeSet a) throws PrintException {}
        };
    }
    static PrintService printService() {
        return new PrintService() {
            public String getName() { return "svc"; }
            public DocPrintJob createPrintJob() { return null; }
            public void addPrintServiceAttributeListener(PrintServiceAttributeListener l) {}
            public void removePrintServiceAttributeListener(PrintServiceAttributeListener l) {}
            public PrintServiceAttributeSet getAttributes() { return null; }
            public <T extends PrintServiceAttribute> T getAttribute(Class<T> category) { return null; }
            public DocFlavor[] getSupportedDocFlavors() { return new DocFlavor[0]; }
            public boolean isDocFlavorSupported(DocFlavor f) { return false; }
            public Class<?>[] getSupportedAttributeCategories() { return new Class<?>[0]; }
            public boolean isAttributeCategorySupported(Class<? extends Attribute> cat) { return false; }
            public Object getDefaultAttributeValue(Class<? extends Attribute> cat) { return null; }
            public Object getSupportedAttributeValues(Class<? extends Attribute> cat, DocFlavor f, AttributeSet a) { return null; }
            public boolean isAttributeValueSupported(Attribute a, DocFlavor f, AttributeSet s) { return false; }
            public AttributeSet getUnsupportedAttributes(DocFlavor f, AttributeSet a) { return null; }
            public ServiceUIFactory getServiceUIFactory() { return null; }
        };
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.event.PrintJobAttributeEvent");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(PrintEvent.class.isAssignableFrom(c), "extends PrintEvent");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        DocPrintJob j = docJob();
        HashPrintJobAttributeSet a = new HashPrintJobAttributeSet(new Copies(2));
        PrintJobAttributeEvent e = new PrintJobAttributeEvent(j, a);
        check(j == e.getPrintJob(), "getPrintJob");
        check(e.getAttributes().containsValue(new Copies(2)), "getAttributes");
        boolean threw = false; try { e.getAttributes().add(new Copies(3)); } catch (UnmodifiableSetException ex) { threw = true; }
        check(threw, "attributes unmodifiable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: defensive-copied attrs / NPE null attrs / IAE null source ----
    static void testContractDeep() throws Exception {
        javax.print.DocPrintJob job = new AEJob();
        javax.print.attribute.PrintJobAttributeSet attrs = new javax.print.attribute.HashPrintJobAttributeSet();
        attrs.add(new javax.print.attribute.standard.Copies(3));
        javax.print.event.PrintJobAttributeEvent e = new javax.print.event.PrintJobAttributeEvent(job, attrs);
        check(e.getPrintJob() == job, "job identity");
        check(e.getAttributes() != attrs, "attrs defensively copied");
        check(e.getAttributes().containsValue(new javax.print.attribute.standard.Copies(3)), "attrs content");
        expectNPE("null attrs", new Runnable() { public void run() { new javax.print.event.PrintJobAttributeEvent(new AEJob(), null); } }, null);
        expectIAE("null source", new Runnable() { public void run() { new javax.print.event.PrintJobAttributeEvent(null, new javax.print.attribute.HashPrintJobAttributeSet()); } }, "null source");
        check(javax.print.event.PrintEvent.class.isAssignableFrom(javax.print.event.PrintJobAttributeEvent.class), "extends PrintEvent");
    }
    static class AEJob implements javax.print.DocPrintJob {
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
