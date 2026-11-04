/*
 * @test
 * @summary sun.print.UnixPrintService conformance: reflection (no init, native/display-heavy).
 * @run main UnixPrintServiceTest
 */

import java.util.*;
import java.lang.reflect.*;

public class UnixPrintServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.UnixPrintService", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.print.UnixPrintService".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("javax.print.PrintService;sun.print.AttributeUpdater;sun.print.SunPrinterJobService".equals(interfaces(c)), "interfaces");
        check("private#[Ljavax.print.attribute.standard.MediaPrintableArea;#getAllPrintableAreas();private#boolean#isAutoSense(javax.print.DocFlavor);private#boolean#isServiceFormattedFlavor(javax.print.DocFlavor);private#boolean#isSupportedCopies(javax.print.attribute.standard.Copies);private#boolean#isSupportedMedia(javax.print.attribute.standard.MediaSizeName);private#javax.print.attribute.PrintServiceAttributeSet#getAIXServiceAttributes();private#javax.print.attribute.PrintServiceAttributeSet#getBSDServiceAttributes();private#javax.print.attribute.PrintServiceAttributeSet#getDynamicAttributes();private#javax.print.attribute.PrintServiceAttributeSet#getSysVServiceAttributes();private#javax.print.attribute.standard.PrinterIsAcceptingJobs#getPrinterIsAcceptingJobs();private#javax.print.attribute.standard.PrinterIsAcceptingJobs#getPrinterIsAcceptingJobsAIX();private#javax.print.attribute.standard.PrinterIsAcceptingJobs#getPrinterIsAcceptingJobsBSD();private#javax.print.attribute.standard.PrinterIsAcceptingJobs#getPrinterIsAcceptingJobsSysV();private#javax.print.attribute.standard.PrinterName#getPrinterName();private#javax.print.attribute.standard.PrinterState#getPrinterState();private#javax.print.attribute.standard.PrinterStateReasons#getPrinterStateReasons();private#javax.print.attribute.standard.QueuedJobCount#getQueuedJobCount();private#javax.print.attribute.standard.QueuedJobCount#getQueuedJobCountAIX();private#javax.print.attribute.standard.QueuedJobCount#getQueuedJobCountBSD();private#javax.print.attribute.standard.QueuedJobCount#getQueuedJobCountSysV();private#void#initSupportedDocFlavors();protected static#[Ljava.lang.String;#filterPrinterNamesAIX([Ljava.lang.String;);public#[Ljava.lang.Class;#getSupportedAttributeCategories();public#[Ljavax.print.DocFlavor;#getSupportedDocFlavors();public#boolean#equals(java.lang.Object);public#boolean#isAttributeCategorySupported(java.lang.Class);public#boolean#isAttributeValueSupported(javax.print.attribute.Attribute,javax.print.DocFlavor,javax.print.attribute.AttributeSet);public#boolean#isDocFlavorSupported(javax.print.DocFlavor);public#boolean#usesClass(java.lang.Class);public#int#hashCode();public#java.lang.Object#getDefaultAttributeValue(java.lang.Class);public#java.lang.Object#getSupportedAttributeValues(java.lang.Class,javax.print.DocFlavor,javax.print.attribute.AttributeSet);public#java.lang.String#getName();public#java.lang.String#toString();public#javax.print.DocPrintJob#createPrintJob();public#javax.print.ServiceUIFactory#getServiceUIFactory();public#javax.print.attribute.AttributeSet#getUnsupportedAttributes(javax.print.DocFlavor,javax.print.attribute.AttributeSet);public#javax.print.attribute.PrintServiceAttribute#getAttribute(java.lang.Class);public#javax.print.attribute.PrintServiceAttributeSet#getAttributes();public#javax.print.attribute.PrintServiceAttributeSet#getUpdatedAttributes();public#void#addPrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener);public#void#invalidateService();public#void#removePrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener);public#void#wakeNotifier()".equals(methods(c)), "methods");
        check("#[Ljava.lang.String;#lpcQueueCom;#[Ljava.lang.String;#lpcStatusCom;private static final#[Ljava.lang.Class;#otherAttrCats;private static final#[Ljava.lang.Class;#serviceAttrCats;private static final#[Ljavax.print.DocFlavor;#supportedDocFlavorsInit;private static final#[Ljavax.print.DocFlavor;#supportedHostDocFlavors;private static final#[Ljavax.print.attribute.standard.MediaSizeName;#mediaSizes;private static#[Ljavax.print.DocFlavor;#supportedDocFlavors;private static#[Ljavax.print.attribute.standard.MediaPrintableArea;#mpas;private static#int#MAXCOPIES;private static#java.lang.String#encoding;private static#javax.print.DocFlavor#textByteFlavor;private transient#javax.print.attribute.PrintServiceAttributeSet#lastSet;private transient#sun.print.ServiceNotifier#notifier;private#boolean#isInvalid;private#java.lang.String#printer;private#javax.print.attribute.standard.PrinterName#name".equals(fields(c)), "fields");
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
