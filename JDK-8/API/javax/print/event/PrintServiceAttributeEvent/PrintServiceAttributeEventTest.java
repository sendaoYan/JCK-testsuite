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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
