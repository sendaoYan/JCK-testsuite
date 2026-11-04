/*
 * @test
 * @summary sun.print.UnixPrintJob conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.print:+open
 * @run main UnixPrintJobTest
 */

import java.util.*;
import java.lang.reflect.*;

public class UnixPrintJobTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.UnixPrintJob", false, UnixPrintJobTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.print.UnixPrintJob".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("javax.print.CancelablePrintJob".equals(interfaces(c)), "interfaces");
        check("private synchronized#void#initializeAttributeSets(javax.print.Doc,javax.print.attribute.PrintRequestAttributeSet);private#[Ljava.lang.String;#printExecCmd(java.lang.String,java.lang.String,boolean,java.lang.String,int,java.lang.String);private#void#closeDataStreams();private#void#getAttributeValues(javax.print.DocFlavor);private#void#notifyEvent(int);public#javax.print.PrintService#getPrintService();public#javax.print.attribute.PrintJobAttributeSet#getAttributes();public#void#addPrintJobAttributeListener(javax.print.event.PrintJobAttributeListener,javax.print.attribute.PrintJobAttributeSet);public#void#addPrintJobListener(javax.print.event.PrintJobListener);public#void#cancel();public#void#pageableJob(java.awt.print.Pageable);public#void#print(javax.print.Doc,javax.print.attribute.PrintRequestAttributeSet);public#void#printableJob(java.awt.print.Printable);public#void#removePrintJobAttributeListener(javax.print.event.PrintJobAttributeListener);public#void#removePrintJobListener(javax.print.event.PrintJobListener)".equals(methods(c)), "methods");
        check("private static#int#DESTFILE;private static#int#DESTPRINTER;private static#java.lang.String#debugPrefix;private transient#java.util.ArrayList#attrListeners;private transient#java.util.ArrayList#jobListeners;private transient#java.util.ArrayList#listenedAttributeSets;private#boolean#fidelity;private#boolean#mNoJobSheet;private#boolean#printReturned;private#boolean#printing;private#int#copies;private#int#mDestType;private#java.awt.print.PrinterJob#job;private#java.io.File#spoolFile;private#java.io.InputStream#instream;private#java.io.Reader#reader;private#java.lang.String#jobName;private#java.lang.String#mDestination;private#java.lang.String#mOptions;private#javax.print.Doc#doc;private#javax.print.PrintService#service;private#javax.print.attribute.PrintJobAttributeSet#jobAttrSet;private#javax.print.attribute.PrintRequestAttributeSet#reqAttrSet;private#javax.print.attribute.standard.MediaSize#mediaSize;private#javax.print.attribute.standard.MediaSizeName#mediaName;private#javax.print.attribute.standard.NumberUp#nUp;private#javax.print.attribute.standard.OrientationRequested#orient;private#javax.print.attribute.standard.Sides#sides;private#sun.print.CustomMediaTray#customTray".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 1, "ctors");
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
            s.append(java.lang.reflect.Modifier.toString(m.getModifiers())).append('#');
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
            l.add(java.lang.reflect.Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        if (!c.isEnum()) return "";
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }
}
