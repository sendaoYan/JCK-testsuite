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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
