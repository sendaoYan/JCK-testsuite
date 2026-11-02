/*
 * @test
 * @summary sun.print.PrintJob2D conformance: reflection (no init, native/display-heavy).
 * @run main PrintJob2DTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PrintJob2DTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.PrintJob2D", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.print.PrintJob2D".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.awt.PrintJob".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.print.Printable;java.lang.Runnable".equals(interfaces(c)), "interfaces");
        check("private static#[I#getSize(java.awt.PageAttributes$MediaType);private#double#getPageResolutionInternal();private#void#copyAttributes(javax.print.PrintService);private#void#debugPrintAttributes(boolean,boolean);private#void#initPrintJob2D(java.awt.Frame,java.lang.String,java.awt.JobAttributes,java.awt.PageAttributes);private#void#startPrinterJobThread();private#void#throwPrintToFile();private#void#translateInputProps();private#void#translateOutputProps();private#void#updateAttributes();public static#java.awt.PageAttributes$MediaType#unMapMedia(javax.print.attribute.standard.MediaSizeName);public static#javax.print.attribute.standard.MediaSizeName#mapMedia(java.awt.PageAttributes$MediaType);public synchronized#void#end();public#boolean#lastPageFirst();public#boolean#printDialog();public#int#getPageResolution();public#int#print(java.awt.Graphics,java.awt.print.PageFormat,int);public#java.awt.Dimension#getPageDimension();public#java.awt.Graphics#getGraphics();public#void#finalize();public#void#run()".equals(methods(c)), "methods");
        check("private static final#[I#LENGTHS;private static final#[I#WIDTHS;private static final#[Ljava.awt.PageAttributes$MediaType;#SIZES;private static final#[Ljavax.print.attribute.standard.MediaSizeName;#JAVAXSIZES;private static final#java.lang.String#A4;private static final#java.lang.String#DEST_PROP;private static final#java.lang.String#EXECUTIVE;private static final#java.lang.String#FILE;private static final#java.lang.String#FILENAME_PROP;private static final#java.lang.String#LANDSCAPE;private static final#java.lang.String#LEGAL;private static final#java.lang.String#LETTER;private static final#java.lang.String#NUMCOPIES_PROP;private static final#java.lang.String#OPTIONS_PROP;private static final#java.lang.String#ORIENT_PROP;private static final#java.lang.String#PAPERSIZE_PROP;private static final#java.lang.String#PORTRAIT;private static final#java.lang.String#PRINTER;private static final#java.lang.String#PRINTER_PROP;private#int#pageIndex;private#java.awt.Frame#frame;private#java.awt.Graphics2D#currentGraphics;private#java.awt.JobAttributes#jobAttributes;private#java.awt.PageAttributes#pageAttributes;private#java.awt.print.PageFormat#pageFormat;private#java.awt.print.PrinterJob#printerJob;private#java.lang.String#docTitle;private#java.lang.String#options;private#java.lang.Thread#printerJobThread;private#java.util.Properties#props;private#javax.print.attribute.PrintRequestAttributeSet#attributes;private#sun.print.PrintJob2D$MessageQ#graphicsDrawn;private#sun.print.PrintJob2D$MessageQ#graphicsToBeDrawn".equals(fields(c)), "fields");
        check(c.getDeclaredConstructors().length == 2, "ctors");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        Collections.sort(l);
        return String.join(";", l);
    }

    static String methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            StringBuilder s = new StringBuilder();
            s.append(Modifier.toString(m.getModifiers())).append('#');
            s.append(m.getReturnType().getName()).append('#');
            s.append(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) { if (i > 0) s.append(','); s.append(ps[i].getName()); }
            s.append(')');
            l.add(s.toString());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            l.add(Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        try {
            Object[] ec = c.getEnumConstants();
            if (ec == null) return "";
            List<String> l = new ArrayList<>();
            for (Object o : ec) l.add(((Enum<?>) o).name());
            Collections.sort(l);
            return String.join(";", l);
        } catch (Throwable t) {
            return "";
        }
    }
}
