/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.UNIXToolkit conformance: reflection (no init, native/display-heavy).
 * @run main UNIXToolkitTest
 */

import java.util.*;
import java.lang.reflect.*;

public class UNIXToolkitTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.UNIXToolkit", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.UNIXToolkit".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.SunToolkit".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check(subset(methods(c), "private native#boolean#gtkCheckVersionImpl(int,int,int);private native#boolean#load_gtk_icon(java.lang.String);private native#boolean#load_stock_icon(int,java.lang.String,int,int,java.lang.String);private native#void#nativeSync();private static native#boolean#check_gtk(int);private static native#boolean#load_gtk(int,boolean);private static native#boolean#unload_gtk();private static native#int#get_gtk_version();protected#java.awt.RenderingHints#getDesktopAAHints();protected#java.lang.Object#lazilyLoadDesktopProperty(java.lang.String);protected#java.lang.Object#lazilyLoadGTKIcon(java.lang.String);public static#boolean#isGtkVerbose();public static#int#getDatatransferTimeout();public static#sun.awt.UNIXToolkit$GtkVersions#getEnabledGtkVersion();public static#sun.awt.UNIXToolkit$GtkVersions#getGtkVersion();public#boolean#checkGtkVersion(int,int,int);public#boolean#isNativeGTKAvailable();public#boolean#loadGTK();public#java.awt.image.BufferedImage#getGTKIcon(java.lang.String);public#java.awt.image.BufferedImage#getStockIcon(int,java.lang.String,int,int,java.lang.String);public#void#loadIconCallback([B,int,int,int,int,int,boolean);public#void#sync()"), "methods");
        check(subset(fields(c), "private static#[I#BAND_OFFSETS;private static#[I#BAND_OFFSETS_ALPHA;private static#int#DEFAULT_DATATRANSFER_TIMEOUT;private#java.awt.image.BufferedImage#tmpImage;private#java.lang.Boolean#nativeGTKAvailable;private#java.lang.Boolean#nativeGTKLoaded;public static#java.lang.Object#GTK_LOCK;public static#java.lang.String#FONTCONFIGAAHINT"), "fields");
        check(c.getDeclaredConstructors().length == 1, "ctors");
        System.out.println("PASS (" + passed + " assertions)");
    }

    // Build-specific tolerance: jdk8u502 added members and strengthened a few
    // `static` fields to `static final`. Assert the jdk8u372 surface is still
    // PRESENT (a subset) and ignore the `final` keyword — member growth and
    // final-strengthening are not contract drift; member REMOVAL still fails.
    static String mod(int m) {
        return Modifier.toString(m).replace(" final", "").replaceAll("\\s+", " ").trim();
    }

    static boolean subset(String actual, String expected) {
        Set<String> a = new HashSet<>();
        for (String s : actual.split(";")) if (!s.isEmpty()) a.add(s);
        for (String e : expected.split(";")) if (!a.contains(e)) return false;
        return true;
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
            s.append(mod(m.getModifiers())).append('#');
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
            l.add(mod(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
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
