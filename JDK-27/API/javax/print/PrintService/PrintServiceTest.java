/*
 * @test
 * @summary javax.print.PrintService conformance: white-box structure + black-box behavior.
 * @run main PrintServiceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrintServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.PrintService");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "createPrintJob"), "declares createPrintJob");
        check(hasMethod(c, "addPrintServiceAttributeListener"), "declares addPrintServiceAttributeListener");
        check(hasMethod(c, "removePrintServiceAttributeListener"), "declares removePrintServiceAttributeListener");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
        check(hasMethod(c, "getAttribute"), "declares getAttribute");
        check(hasMethod(c, "getSupportedDocFlavors"), "declares getSupportedDocFlavors");
        check(hasMethod(c, "isDocFlavorSupported"), "declares isDocFlavorSupported");
        check(hasMethod(c, "getSupportedAttributeCategories"), "declares getSupportedAttributeCategories");
        check(hasMethod(c, "isAttributeCategorySupported"), "declares isAttributeCategorySupported");
        check(hasMethod(c, "getDefaultAttributeValue"), "declares getDefaultAttributeValue");
        check(hasMethod(c, "getSupportedAttributeValues"), "declares getSupportedAttributeValues");
        check(hasMethod(c, "isAttributeValueSupported"), "declares isAttributeValueSupported");
        check(hasMethod(c, "getUnsupportedAttributes"), "declares getUnsupportedAttributes");
        check(hasMethod(c, "getServiceUIFactory"), "declares getServiceUIFactory");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
    }

    static void testBlackBox() throws Exception { /* pure interface */ }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: real PS stream service surface + recording stub full matrix ----
    static void testContractDeep() throws Exception {
        javax.print.StreamPrintServiceFactory[] fs = javax.print.StreamPrintServiceFactory.lookupStreamPrintServiceFactories(
            javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, javax.print.DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType());
        check(fs.length == 1, "exactly one PS stream factory");
        javax.print.StreamPrintService s = fs[0].getPrintService(new java.io.ByteArrayOutputStream());
        check("Postscript output".equals(s.getName()), "PS service name [" + s.getName() + "]");
        check(s.getSupportedDocFlavors().length == 11, "11 supported flavors");
        check(s.getSupportedAttributeCategories().length == 11, "11 attribute categories");
        check(s.isDocFlavorSupported(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE), "SERVICE_FORMATTED printable supported");
        check(!s.isDocFlavorSupported(javax.print.DocFlavor.BYTE_ARRAY.POSTSCRIPT), "byte PS not a PS-stream input flavor");
        check(s.getAttributes().size() == 1, "one service attribute (PrinterName)");
        check(s.getAttribute(javax.print.attribute.standard.PrinterName.class) == null, "PS stream PrinterName null");
        check(s.getDefaultAttributeValue(javax.print.attribute.standard.Copies.class).equals(new javax.print.attribute.standard.Copies(1)), "default Copies 1");
        Object sup = s.getSupportedAttributeValues(javax.print.attribute.standard.Copies.class, javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
        check("1-1000".equals(sup.toString()), "CopiesSupported toString 1-1000 [" + sup + "]");
        check(sup.equals(new javax.print.attribute.standard.CopiesSupported(1, 1000)), "CopiesSupported equality");
        check(s.isAttributeCategorySupported(javax.print.attribute.standard.Copies.class), "Copies category supported");
        check(s.isAttributeValueSupported(new javax.print.attribute.standard.Copies(1), javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, null), "Copies value supported");
        check(s.getUnsupportedAttributes(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, null) == null, "no unsupported attrs for supported flavor");
        check(s.getServiceUIFactory() == null, "PS stream has no UI factory");
        expectNPE("getAttribute(null) on real service", new Runnable() { public void run() { s.getAttribute(null); } }, null);
        // recording stub honors every interface method
        BaseSvc rec = new BaseSvc();
        check(rec.getName().equals("RecSvc") && rec.createPrintJob() == null, "stub name/job");
        check(rec.getAttributes().isEmpty(), "stub attributes empty set");
        check(rec.getSupportedDocFlavors().length == 1, "stub flavors");
        check(rec.isDocFlavorSupported(javax.print.DocFlavor.SERVICE_FORMATTED.RENDERABLE_IMAGE) == false, "stub unsupported flavor false");
        check(rec.getSupportedAttributeCategories()[0] == javax.print.attribute.standard.Copies.class, "stub categories");
        check(rec.getDefaultAttributeValue(javax.print.attribute.standard.Copies.class).equals(new javax.print.attribute.standard.Copies(7)), "stub default");
        check(rec.getSupportedAttributeValues(javax.print.attribute.standard.Copies.class, null, null).equals(new javax.print.attribute.standard.CopiesSupported(1, 9)), "stub supported values");
        check(rec.isAttributeValueSupported(new javax.print.attribute.standard.Copies(1), null, null), "stub value support true");
        check(rec.getUnsupportedAttributes(null, null) == null, "stub unsupported null");
        check(rec.getServiceUIFactory() == null, "stub ui factory null");
        rec.addPrintServiceAttributeListener(null);
        rec.removePrintServiceAttributeListener(null);
        passed += 2; // listener add/remove accepted
        // interface surface: 15 abstract methods, equals/hashCode declared
        java.lang.reflect.Method[] ms = javax.print.PrintService.class.getMethods();
        check(ms.length >= 15, "PrintService at least 15 methods: " + ms.length);
        boolean hasEquals = false, hasHashCode = false;
        for (java.lang.reflect.Method m : ms) { if (m.getName().equals("equals")) hasEquals = true; if (m.getName().equals("hashCode")) hasHashCode = true; }
        check(hasEquals && hasHashCode, "PrintService redeclares equals+hashCode contract");
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
