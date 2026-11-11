/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.print.CUPSPrinter conformance: reflection (no init, native/display-heavy).
 * @run main CUPSPrinterTest
 */

import java.util.*;
import java.lang.reflect.*;

public class CUPSPrinterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.CUPSPrinter", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.print.CUPSPrinter".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#[Ljavax.print.attribute.standard.MediaPrintableArea;#getMediaPrintableArea();#[Ljavax.print.attribute.standard.MediaSizeName;#getMediaSizeNames();#[Ljavax.print.attribute.standard.MediaTray;#getMediaTrays();#[Lsun.print.CustomMediaSizeName;#getCustomMediaSizeNames();private static native#boolean#canConnect(java.lang.String,int);private static native#boolean#initIDs();private static native#int#getCupsPort();private static native#java.lang.String#getCupsDefaultPrinter();private static native#java.lang.String#getCupsServer();private static synchronized native#[F#getPageSizes(java.lang.String);private static synchronized native#[Ljava.lang.String;#getMedia(java.lang.String);private synchronized#void#initMedia();public static#boolean#isCupsRunning();public static#int#getPort();public static#java.lang.String#getServer();public#int#getDefaultMediaIndex();static#[Ljava.lang.String;#getAllPrinters();static#[Ljava.lang.String;#getDefaultPrinter()".equals(methods(c)), "methods");
        check("private static final#double#PRINTER_DPI;private static final#java.lang.String#debugPrefix;private static#boolean#libFound;private static#int#cupsPort;private static#java.lang.String#cupsServer;private#[F#pageSizes;private#[Ljava.lang.String;#media;private#[Ljavax.print.attribute.standard.MediaPrintableArea;#cupsMediaPrintables;private#[Ljavax.print.attribute.standard.MediaSizeName;#cupsMediaSNames;private#[Ljavax.print.attribute.standard.MediaTray;#cupsMediaTrays;private#[Lsun.print.CustomMediaSizeName;#cupsCustomMediaSNames;private#boolean#initialized;private#java.lang.String#printer;public#int#nPageSizes;public#int#nTrays".equals(fields(c)), "fields");
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
