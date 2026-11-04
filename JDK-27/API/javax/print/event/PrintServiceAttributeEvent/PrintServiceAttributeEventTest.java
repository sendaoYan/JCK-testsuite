/*
 * @test
 * @summary javax.print.event.PrintServiceAttributeEvent conformance: white-box structure + black-box behavior.
 * @run main PrintServiceAttributeEventTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrintServiceAttributeEventTest {
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
        Class<?> c = load("javax.print.event.PrintServiceAttributeEvent");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(PrintEvent.class.isAssignableFrom(c), "extends PrintEvent");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        PrintService svc = printService();
        HashPrintServiceAttributeSet a = new HashPrintServiceAttributeSet(new PagesPerMinute(5));
        PrintServiceAttributeEvent e = new PrintServiceAttributeEvent(svc, a);
        check(svc == e.getPrintService(), "getPrintService");
        check(e.getAttributes().containsValue(new PagesPerMinute(5)), "getAttributes");
        boolean threw = false; try { e.getAttributes().add(new PagesPerMinute(6)); } catch (UnmodifiableSetException ex) { threw = true; }
        check(threw, "attributes unmodifiable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
