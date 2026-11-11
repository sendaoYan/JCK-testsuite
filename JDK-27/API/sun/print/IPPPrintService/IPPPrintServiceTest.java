/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.print.IPPPrintService conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.print:+open
 * @run main IPPPrintServiceTest
 */

import java.util.*;
import java.lang.reflect.*;

public class IPPPrintServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.IPPPrintService", false, IPPPrintServiceTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.print.IPPPrintService".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("javax.print.PrintService;sun.print.SunPrinterJobService".equals(interfaces(c)), "interfaces");
        check("#java.lang.String#getDest();private static#java.lang.String#toHex(byte);private static#java.net.URL#newURL(java.lang.String);private synchronized#boolean#isSupportedMedia(javax.print.attribute.standard.MediaSizeName);private synchronized#boolean#isSupportedMediaTray(javax.print.attribute.standard.MediaTray);private#[Ljavax.print.attribute.standard.Media;#getSupportedMedia();private#[Ljavax.print.attribute.standard.OutputBin;#getSupportedOutputBins();private#[Ljavax.print.attribute.standard.PrinterResolution;#getPrintResolutions();private#boolean#isAutoSense(javax.print.DocFlavor);private#boolean#isDestinationSupported(javax.print.DocFlavor,javax.print.attribute.AttributeSet);private#boolean#isSupportedCopies(javax.print.attribute.standard.Copies);private#boolean#isSupportedResolution(javax.print.attribute.standard.PrinterResolution);private#javax.print.attribute.standard.Media#getIPPMedia(java.lang.String);private#void#initAttributes();private#void#opGetAttributes();protected static#void#debug_println(java.lang.String);public static#[Ljava.util.HashMap;#readIPPResponse(java.io.InputStream);public static#boolean#writeIPPRequest(java.io.OutputStream,java.lang.String,[Lsun.print.AttributeClass;);public static#java.net.HttpURLConnection#getIPPConnection(java.net.URL);public synchronized#[Ljava.lang.Class;#getSupportedAttributeCategories();public synchronized#[Ljavax.print.DocFlavor;#getSupportedDocFlavors();public synchronized#boolean#isPostscript();public synchronized#java.lang.Object#getDefaultAttributeValue(java.lang.Class);public synchronized#java.lang.Object#getSupportedAttributeValues(java.lang.Class,javax.print.DocFlavor,javax.print.attribute.AttributeSet);public synchronized#javax.print.attribute.PrintServiceAttribute#getAttribute(java.lang.Class);public synchronized#javax.print.attribute.PrintServiceAttributeSet#getAttributes();public#boolean#equals(java.lang.Object);public#boolean#isAttributeCategorySupported(java.lang.Class);public#boolean#isAttributeValueSupported(javax.print.attribute.Attribute,javax.print.DocFlavor,javax.print.attribute.AttributeSet);public#boolean#isDocFlavorSupported(javax.print.DocFlavor);public#boolean#isIPPSupportedImages(java.lang.String);public#boolean#usesClass(java.lang.Class);public#int#hashCode();public#java.lang.String#getName();public#java.lang.String#toString();public#javax.print.DocPrintJob#createPrintJob();public#javax.print.ServiceUIFactory#getServiceUIFactory();public#javax.print.attribute.AttributeSet#getUnsupportedAttributes(javax.print.DocFlavor,javax.print.attribute.AttributeSet);public#sun.print.CustomMediaSizeName#findCustomMedia(javax.print.attribute.standard.MediaSizeName);public#void#addPrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener);public#void#removePrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener);public#void#wakeNotifier()".equals(methods(c)), "methods");
        check("private static final#byte#GRPTAG_END_ATTRIBUTES;private static final#byte#GRPTAG_JOB_ATTRIBUTES;private static final#byte#GRPTAG_OP_ATTRIBUTES;private static final#byte#GRPTAG_PRINTER_ATTRIBUTES;private static final#byte#STATUSCODE_SUCCESS;private static final#java.lang.String#FORCE_PIPE_PROP;private static final#java.lang.String#debugPrefix;private static#[Ljava.lang.Object;#allDocFlavors;private static#[Ljava.lang.Object;#printReqAttribDefault;private static#[Ljavax.print.DocFlavor;#appOctetStream;private static#[Ljavax.print.DocFlavor;#appPCL;private static#[Ljavax.print.DocFlavor;#appPDF;private static#[Ljavax.print.DocFlavor;#appPostScript;private static#[Ljavax.print.DocFlavor;#imageGIF;private static#[Ljavax.print.DocFlavor;#imageJPG;private static#[Ljavax.print.DocFlavor;#imagePNG;private static#[Ljavax.print.DocFlavor;#textHtml;private static#[Ljavax.print.DocFlavor;#textHtmlHost;private static#[Ljavax.print.DocFlavor;#textPlain;private static#[Ljavax.print.DocFlavor;#textPlainHost;private static#[[Ljava.lang.Object;#serviceAttributes;private static#int#MAXCOPIES;private static#short#MAX_ATTRIBUTE_LENGTH;private transient#sun.print.ServiceNotifier#notifier;private#[I#ppdResolutions;private#[Ljava.lang.Class;#supportedCats;private#[Ljavax.print.DocFlavor;#supportedDocFlavors;private#[Ljavax.print.attribute.standard.MediaSizeName;#mediaSizeNames;private#[Ljavax.print.attribute.standard.MediaTray;#mediaTrays;private#[Ljavax.print.attribute.standard.OutputBin;#outputBins;private#[Ljavax.print.attribute.standard.PrinterResolution;#printerResolutions;private#[Lsun.print.CustomMediaSizeName;#customMediaSizeNames;private#boolean#gifImagesAdded;private#boolean#init;private#boolean#isCupsPrinter;private#boolean#jpgImagesAdded;private#boolean#pngImagesAdded;private#int#defaultMediaIndex;private#java.lang.Boolean#isPS;private#java.lang.String#printer;private#java.net.HttpURLConnection#urlConnection;private#java.net.URI#myURI;private#java.net.URL#myURL;private#java.util.HashMap#getAttMap;private#sun.print.CUPSPrinter#cps;public static final#boolean#debugPrint;public static final#java.lang.String#OP_CUPS_GET_DEFAULT;public static final#java.lang.String#OP_CUPS_GET_PRINTERS;public static final#java.lang.String#OP_GET_ATTRIBUTES".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
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
