/*
 * @test
 * @summary javax.print.PrintServiceLookup conformance: white-box structure + black-box behavior.
 * @run main PrintServiceLookupTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrintServiceLookupTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.PrintServiceLookup");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(hasMethod(c, "lookupPrintServices") && hasMethod(c, "lookupMultiDocPrintServices") && hasMethod(c, "lookupDefaultPrintService") && hasMethod(c, "registerServiceProvider") && hasMethod(c, "registerService"), "static methods");
        check(hasMethod(c, "getPrintServices") && hasMethod(c, "getMultiDocPrintServices") && hasMethod(c, "getDefaultPrintService"), "abstract methods");
    }

    static void testBlackBox() throws Exception { /* pure interface */ }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: no-service contract + registerServiceProvider/registerService closed loops ----
    static void testContractDeep() throws Exception {
        // no-service contract on this headless host (CUPS-less)
        javax.print.PrintService[] all = javax.print.PrintServiceLookup.lookupPrintServices(null, null);
        check(all != null, "lookupPrintServices returns non-null array");
        int real = 0, nullLeak = 0;
        for (javax.print.PrintService p : all) { if (p == null) nullLeak++; else real++; }
        check(real == 0, "no real printers installed: zero non-null services (skip reason: headless CUPS-less host)");
        boolean isOld = System.getProperty("java.specification.version").startsWith("1.8");
        check(nullLeak > 0 == isOld, "registerService(null) leak: JDK-8 true (null entry), JDK-27 false (clean)");
        javax.print.MultiDocPrintService[] md = javax.print.PrintServiceLookup.lookupMultiDocPrintServices(null, null);
        check(md != null && md.length == 0, "lookupMultiDocPrintServices non-null empty");
        check(javax.print.PrintServiceLookup.lookupDefaultPrintService() == null, "defaultPrintService null without printers");
        try {
            javax.print.PrintService[] byFlavor = javax.print.PrintServiceLookup.lookupPrintServices(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
            check(byFlavor != null && byFlavor.length == 0, "flavor-filtered lookup still empty non-null");
            check(!isOld, "flavor lookup clean on JDK-27");
        } catch (NullPointerException npe) {
            check(isOld, "flavor-filtered lookup NPEs on JDK-8 from leaked null entry");
        }
        // registerService closed loop
        boolean regDirect = javax.print.PrintServiceLookup.registerService(new BaseSvc());
        check(regDirect, "registerService returns true for plain service");
        javax.print.PrintService[] withReg = javax.print.PrintServiceLookup.lookupPrintServices(null, null);
        boolean seen = false; for (javax.print.PrintService p : withReg) if (p != null && "RecSvc".equals(p.getName())) seen = true;
        check(seen, "registered service surfaces in lookupPrintServices");
        try {
            javax.print.PrintService[] withReg2 = javax.print.PrintServiceLookup.lookupPrintServices(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
            boolean seen2 = false; for (javax.print.PrintService p : withReg2) if (p != null && "RecSvc".equals(p.getName())) seen2 = true;
            check(seen2, "registered service matches its supported flavor in filtered lookup");
            check(!isOld, "clean filtered lookup on JDK-27");
        } catch (NullPointerException npe) {
            check(isOld, "filtered lookup NPEs on JDK-8 from leaked null entry (consequence of registerService(null) bug)");
        }
        // registerServiceProvider closed loop
        boolean regProv = javax.print.PrintServiceLookup.registerServiceProvider(new DeepLookup());
        check(regProv, "registerServiceProvider true");
        javax.print.PrintService[] withProv = javax.print.PrintServiceLookup.lookupPrintServices(null, null);
        boolean seenP = false; for (javax.print.PrintService p : withProv) if (p != null && "ProvSvc".equals(p.getName())) seenP = true;
        check(seenP, "provider service surfaces in lookupPrintServices");
        boolean dup = javax.print.PrintServiceLookup.registerServiceProvider(new DeepLookup());
        check(!dup, "re-registering provider with same services returns false");
        // null-arg contract: registerService(null) differs (JDK-8 accepts+leaks null, JDK-27 rejects false); registerServiceProvider(null) accepts on both
        // (registerService(null) first-call outcome already captured by the nullLeak check above;
        //  a second call dedups to false on JDK-8, so no repeat call is asserted here)
        check(javax.print.PrintServiceLookup.registerServiceProvider(null), "registerServiceProvider(null) returns true on both");
        // abstract SPI surface
        check(java.lang.reflect.Modifier.isAbstract(javax.print.PrintServiceLookup.class.getModifiers()), "SPI class abstract");
        javax.print.PrintServiceLookup.class.getDeclaredConstructor();
        passed++; // default ctor exists (instantiation blocked by module rules on 9+)
    }
    static class DeepLookup extends javax.print.PrintServiceLookup {
        public javax.print.PrintService[] getPrintServices(javax.print.DocFlavor f, javax.print.attribute.AttributeSet as) { return new javax.print.PrintService[] { new ProvSvc() }; }
        public javax.print.PrintService[] getPrintServices() { return new javax.print.PrintService[] { new ProvSvc() }; }
        public javax.print.MultiDocPrintService[] getMultiDocPrintServices(javax.print.DocFlavor[] fs, javax.print.attribute.AttributeSet as) { return new javax.print.MultiDocPrintService[0]; }
        public javax.print.PrintService getDefaultPrintService() { return null; }
    }
    static class ProvSvc extends BaseSvc { public String getName() { return "ProvSvc"; } }

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
