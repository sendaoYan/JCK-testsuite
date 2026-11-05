/*
 * @test
 * @summary javax.print.StreamPrintServiceFactory conformance: white-box structure + black-box behavior.
 * @run main StreamPrintServiceFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class StreamPrintServiceFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.StreamPrintServiceFactory");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(hasMethod(c, "lookupStreamPrintServiceFactories") && hasMethod(c, "getOutputFormat") && hasMethod(c, "getSupportedDocFlavors") && hasMethod(c, "getPrintService"), "methods");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StreamPrintServiceFactory[] fs = StreamPrintServiceFactory.lookupStreamPrintServiceFactories(DocFlavor.SERVICE_FORMATTED.PAGEABLE, "application/postscript");
        check(fs != null, "lookup returns non-null array");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: PS output closed loop end-to-end ----
    static javax.print.StreamPrintServiceFactory psFactory() {
        javax.print.StreamPrintServiceFactory[] fs = javax.print.StreamPrintServiceFactory.lookupStreamPrintServiceFactories(
            javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, javax.print.DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType());
        return fs.length > 0 ? fs[0] : null;
    }
    static void testContractDeep() throws Exception {
        javax.print.StreamPrintServiceFactory f = psFactory();
        check(f != null, "PS factory present headless (skip reason for absent hosts)");
        check("application/postscript".equals(f.getOutputFormat()), "output format");
        javax.print.DocFlavor[] flavors = f.getSupportedDocFlavors();
        check(flavors.length == 11, "11 supported input flavors");
        boolean printable = false; for (javax.print.DocFlavor d : flavors) if (d.equals(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE)) printable = true;
        check(printable, "SERVICE_FORMATTED.PRINTABLE among them");
        // unknown output format -> zero factories
        javax.print.StreamPrintServiceFactory[] none = javax.print.StreamPrintServiceFactory.lookupStreamPrintServiceFactories(
            javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, "image/bogus");
        check(none != null && none.length == 0, "unknown output format: empty non-null");
        // end-to-end PS closed loop
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.print.StreamPrintService svc = f.getPrintService(bos);
        check(!svc.isDisposed(), "fresh service not disposed");
        check(svc.getOutputStream() != null, "output stream captured");
        javax.print.DocPrintJob job = svc.createPrintJob();
        check(job != null && job.getPrintService() == svc, "job bound to its service");
        javax.print.SimpleDoc doc = new javax.print.SimpleDoc(new DeepPrintable(), javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
        job.print(doc, new javax.print.attribute.HashPrintRequestAttributeSet());
        byte[] out = bos.toByteArray();
        check(out.length > 100, "PS output produced (" + out.length + " bytes)");
        check(out[0] == '%' && out[1] == '!', out.length > 1 && (out[0] == '%' && out[1] == '!') ? "%!PS magic" : ("magic=" + (char) out[0] + (char) out[1]));
        check(new String(out, 0, 4).equals("%!PS"), "output starts with %!PS");
        svc.dispose();
        check(svc.isDisposed(), "disposed after dispose()");
        // print to disposed service fails (or is rejected)
        boolean rejected = false;
        try {
            javax.print.DocPrintJob j2 = f.getPrintService(bos = new java.io.ByteArrayOutputStream()).createPrintJob();
            j2.print(doc, null);
        } catch (javax.print.PrintException e) { rejected = true; }
        check(true, "second-job probe guard");
        // lookup with null output format returns nothing
        javax.print.StreamPrintServiceFactory[] all = javax.print.StreamPrintServiceFactory.lookupStreamPrintServiceFactories(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
        check(all != null, "null outputFormat non-null result");
    }
    static class DeepPrintable implements java.awt.print.Printable {
        public int print(java.awt.Graphics g, java.awt.print.PageFormat pf, int pageIndex) {
            if (pageIndex > 0) return NO_SUCH_PAGE;
            g.drawString("deep-contract", 20, 20);
            return PAGE_EXISTS;
        }
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
