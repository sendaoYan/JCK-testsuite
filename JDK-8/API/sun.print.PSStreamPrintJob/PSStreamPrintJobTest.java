/*
 * @test
 * @summary sun.print.PSStreamPrintJob conformance: reflection (no init, native/display-heavy).
 * @run main PSStreamPrintJobTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PSStreamPrintJobTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.PSStreamPrintJob", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.print.PSStreamPrintJob".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("javax.print.CancelablePrintJob".equals(interfaces(c)), "interfaces");
        check("private synchronized#void#initializeAttributeSets(javax.print.Doc,javax.print.attribute.PrintRequestAttributeSet);private#void#closeDataStreams();private#void#getAttributeValues(javax.print.DocFlavor);private#void#notifyEvent(int);public#javax.print.PrintService#getPrintService();public#javax.print.attribute.PrintJobAttributeSet#getAttributes();public#void#addPrintJobAttributeListener(javax.print.event.PrintJobAttributeListener,javax.print.attribute.PrintJobAttributeSet);public#void#addPrintJobListener(javax.print.event.PrintJobListener);public#void#cancel();public#void#pageableJob(java.awt.print.Pageable,javax.print.attribute.PrintRequestAttributeSet);public#void#print(javax.print.Doc,javax.print.attribute.PrintRequestAttributeSet);public#void#printableJob(java.awt.print.Printable,javax.print.attribute.PrintRequestAttributeSet);public#void#removePrintJobAttributeListener(javax.print.event.PrintJobAttributeListener);public#void#removePrintJobListener(javax.print.event.PrintJobListener)".equals(methods(c)), "methods");
        check("private transient#java.util.Vector#attrListeners;private transient#java.util.Vector#jobListeners;private transient#java.util.Vector#listenedAttributeSets;private#boolean#fidelity;private#boolean#printReturned;private#boolean#printing;private#int#copies;private#java.awt.print.PrinterJob#job;private#java.io.InputStream#instream;private#java.io.Reader#reader;private#java.lang.String#jobName;private#javax.print.Doc#doc;private#javax.print.attribute.PrintJobAttributeSet#jobAttrSet;private#javax.print.attribute.PrintRequestAttributeSet#reqAttrSet;private#javax.print.attribute.standard.MediaSize#mediaSize;private#javax.print.attribute.standard.OrientationRequested#orient;private#sun.print.PSStreamPrintService#service".equals(fields(c)), "fields");
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
