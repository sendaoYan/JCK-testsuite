/*
 * @test
 * @summary sun.print.PrintServiceLookupProvider conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.print:+open
 * @run main PrintServiceLookupProviderTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PrintServiceLookupProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.PrintServiceLookupProvider", false, PrintServiceLookupProviderTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.print.PrintServiceLookupProvider".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("javax.print.PrintServiceLookup".equals(c.getSuperclass().getName()), "superclass");
        check("java.lang.Runnable;sun.print.BackgroundServiceLookup".equals(interfaces(c)), "interfaces");
        check("private static#java.net.URL#newURL(java.lang.String);private#[Ljava.lang.String;#getAllPrinterNamesAIX();private#[Ljava.lang.String;#getAllPrinterNamesBSD();private#[Ljava.lang.String;#getAllPrinterNamesSysV();private#[Ljavax.print.PrintService;#copyOf([Ljavax.print.PrintService;);private#[Ljavax.print.PrintService;#getPrintServices(javax.print.attribute.PrintServiceAttributeSet);private#boolean#checkPrinterName(java.lang.String);private#boolean#matchesAttributes(javax.print.PrintService,javax.print.attribute.PrintServiceAttributeSet);private#int#addPrintServiceToList(java.util.ArrayList,javax.print.PrintService);private#java.lang.String#getDefaultPrinterNameAIX();private#java.lang.String#getDefaultPrinterNameBSD();private#java.lang.String#getPrinterDestName(javax.print.PrintService);private#javax.print.PrintService#getNamedPrinterNameAIX(java.lang.String);private#javax.print.PrintService#getNamedPrinterNameBSD(java.lang.String);private#javax.print.PrintService#getNamedPrinterNameSysV(java.lang.String);private#javax.print.PrintService#getServiceByName(javax.print.attribute.standard.PrinterName);public synchronized#[Ljavax.print.PrintService;#getPrintServices();public synchronized#javax.print.PrintService#getDefaultPrintService();public synchronized#void#getServicesInbackground(sun.print.BackgroundLookupListener);public synchronized#void#refreshServices();public#[Ljavax.print.MultiDocPrintService;#getMultiDocPrintServices([Ljavax.print.DocFlavor;,javax.print.attribute.AttributeSet);public#[Ljavax.print.PrintService;#getPrintServices(javax.print.DocFlavor,javax.print.attribute.AttributeSet);public#void#run();static#[Ljava.lang.String;#execCmd(java.lang.String);static#boolean#isAIX();static#boolean#isBSD();static#boolean#isLinux();static#boolean#isMac();static#int#getBSDCommandIndex();static#java.lang.String#getDefaultPrinterNameSysV()".equals(methods(c)), "methods");
        check("#[Ljava.lang.String;#lpNameComAix;#[Ljava.lang.String;#lpcAllCom;#[Ljava.lang.String;#lpcFirstCom;#[Ljava.lang.String;#lpcNameCom;private static final#int#DEFAULT_MINREFRESH;private static final#int#aix_lpstat_d;private static final#int#aix_lpstat_p;private static final#int#aix_lpstat_v;private static final#int#aix_lsallq;private static#boolean#pollServices;private static#int#aix_defaultPrinterEnumeration;private static#int#minRefreshTime;private static#java.lang.String#debugPrefix;private#[Ljavax.print.PrintService;#printServices;private#java.lang.String#defaultPrinter;private#java.util.Vector#lookupListeners;private#javax.print.PrintService#defaultPrintService;static final#int#BSD_LPD;static final#int#BSD_LPD_NG;static final#int#UNINITIALIZED;static#int#cmdIndex".equals(fields(c)), "fields");
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
