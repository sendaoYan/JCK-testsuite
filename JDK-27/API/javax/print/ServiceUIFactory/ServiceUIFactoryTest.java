/*
 * @test
 * @summary javax.print.ServiceUIFactory conformance: white-box structure + black-box behavior.
 * @run main ServiceUIFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class ServiceUIFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.ServiceUIFactory");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(hasMethod(c, "getUI") && hasMethod(c, "getUIClassNamesForRole"), "abstract methods");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check("javax.swing.JComponent".equals(ServiceUIFactory.JCOMPONENT_UI), "JCOMPONENT_UI");
        check("java.awt.Panel".equals(ServiceUIFactory.PANEL_UI), "PANEL_UI");
        check("java.awt.Dialog".equals(ServiceUIFactory.DIALOG_UI), "DIALOG_UI");
        check("javax.swing.JDialog".equals(ServiceUIFactory.JDIALOG_UI), "JDIALOG_UI");
        check(ServiceUIFactory.ABOUT_UIROLE == 1 && ServiceUIFactory.ADMIN_UIROLE == 2 && ServiceUIFactory.MAIN_UIROLE == 3 && ServiceUIFactory.RESERVED_UIROLE == 99, "UI role constants");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: role/gadget constants + recording implementation ----
    static void testContractDeep() throws Exception {
        check(javax.print.ServiceUIFactory.ABOUT_UIROLE == 1, "ABOUT_UIROLE 1");
        check(javax.print.ServiceUIFactory.ADMIN_UIROLE == 2, "ADMIN_UIROLE 2");
        check(javax.print.ServiceUIFactory.MAIN_UIROLE == 3, "MAIN_UIROLE 3");
        check(javax.print.ServiceUIFactory.RESERVED_UIROLE == 99, "RESERVED_UIROLE 99");
        check("javax.swing.JComponent".equals(javax.print.ServiceUIFactory.JCOMPONENT_UI), "JCOMPONENT_UI");
        check("java.awt.Panel".equals(javax.print.ServiceUIFactory.PANEL_UI), "PANEL_UI");
        check("java.awt.Dialog".equals(javax.print.ServiceUIFactory.DIALOG_UI), "DIALOG_UI");
        check("javax.swing.JDialog".equals(javax.print.ServiceUIFactory.JDIALOG_UI), "JDIALOG_UI");
        check(java.lang.reflect.Modifier.isAbstract(javax.print.ServiceUIFactory.class.getModifiers()), "abstract");
        RecFactory f = new RecFactory();
        check("DEEP".equals(f.getUI(javax.print.ServiceUIFactory.ABOUT_UIROLE, javax.print.ServiceUIFactory.JDIALOG_UI)), "recording getUI");
        check(f.getUI(-1, "x") == null, "unknown role null");
        check(f.getUIClassNamesForRole(javax.print.ServiceUIFactory.MAIN_UIROLE).length == 1, "class names for role");
        check(f.getUIClassNamesForRole(-1).length == 0, "unknown role empty names");
    }
    static class RecFactory extends javax.print.ServiceUIFactory {
        public Object getUI(int role, String ui) { return role == ABOUT_UIROLE ? "DEEP" : null; }
        public String[] getUIClassNamesForRole(int role) { return role == MAIN_UIROLE ? new String[] { "javax.swing.JLabel" } : new String[0]; }
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
