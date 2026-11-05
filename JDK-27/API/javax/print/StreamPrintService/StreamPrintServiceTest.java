/*
 * @test
 * @summary javax.print.StreamPrintService conformance: white-box structure + black-box behavior.
 * @run main StreamPrintServiceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class StreamPrintServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.StreamPrintService");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(PrintService.class.isAssignableFrom(c), "implements PrintService");
        check(hasMethod(c, "getOutputStream") && hasMethod(c, "getOutputFormat") && hasMethod(c, "dispose") && hasMethod(c, "isDisposed"), "methods");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamPrintService s = new StreamPrintService(out) {
            public String getOutputFormat() { return "application/postscript"; }
            public String getName() { return "stream"; }
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
        check(out == s.getOutputStream(), "getOutputStream");
        check("application/postscript".equals(s.getOutputFormat()), "getOutputFormat");
        check(!s.isDisposed(), "not disposed initially");
        s.dispose();
        check(s.isDisposed(), "disposed after dispose");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: protected ctor subclass + dispose/isDisposed/output stream identity ----
    static void testContractDeep() throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(javax.print.StreamPrintService.class.getModifiers()), "StreamPrintService abstract");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        RecStream svc = new RecStream(bos);
        check(svc.getOutputStream() == bos, "output stream same reference");
        check(!svc.isDisposed(), "initially not disposed");
        svc.dispose();
        check(svc.isDisposed(), "disposed after dispose()");
        check(svc.getOutputFormat().equals("application/x-deep"), "subclass output format");
        check(javax.print.PrintService.class.isAssignableFrom(javax.print.StreamPrintService.class), "implements PrintService");
        java.lang.reflect.Constructor<?>[] cts = javax.print.StreamPrintService.class.getDeclaredConstructors();
        int protOs = 0;
        for (java.lang.reflect.Constructor<?> ct : cts)
            if (java.lang.reflect.Modifier.isProtected(ct.getModifiers())
                && ct.getParameterTypes().length == 1 && ct.getParameterTypes()[0] == java.io.OutputStream.class) protOs++;
        check(protOs == 1, "exactly one protected OutputStream ctor (of " + cts.length + " declared)");
    }
    static class RecStream extends javax.print.StreamPrintService {
        RecStream(java.io.OutputStream os) { super(os); }
        public String getOutputFormat() { return "application/x-deep"; }
        public javax.print.DocFlavor[] getSupportedDocFlavors() { return new javax.print.DocFlavor[0]; }
        public javax.print.DocPrintJob createPrintJob() { return null; }
        public String getName() { return "RecStream"; }
        public void addPrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener l) {}
        public void removePrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener l) {}
        public javax.print.attribute.PrintServiceAttributeSet getAttributes() { return new javax.print.attribute.HashPrintServiceAttributeSet(); }
        public <T extends javax.print.attribute.PrintServiceAttribute> T getAttribute(Class<T> c) { return null; }
        public boolean isDocFlavorSupported(javax.print.DocFlavor f) { return false; }
        public Class<?>[] getSupportedAttributeCategories() { return new Class[0]; }
        public boolean isAttributeCategorySupported(Class<? extends javax.print.attribute.Attribute> c) { return false; }
        public Object getDefaultAttributeValue(Class<? extends javax.print.attribute.Attribute> c) { return null; }
        public Object getSupportedAttributeValues(Class<? extends javax.print.attribute.Attribute> c, javax.print.DocFlavor f, javax.print.attribute.AttributeSet as) { return null; }
        public boolean isAttributeValueSupported(javax.print.attribute.Attribute v, javax.print.DocFlavor f, javax.print.attribute.AttributeSet as) { return false; }
        public javax.print.attribute.AttributeSet getUnsupportedAttributes(javax.print.DocFlavor f, javax.print.attribute.AttributeSet as) { return null; }
        public javax.print.ServiceUIFactory getServiceUIFactory() { return null; }
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
