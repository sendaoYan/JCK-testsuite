/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.UNIXToolkit conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main UNIXToolkitTest
 */

import java.util.*;
import java.lang.reflect.*;

public class UNIXToolkitTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.UNIXToolkit", false, UNIXToolkitTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.UNIXToolkit".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.awt.SunToolkit".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private native#boolean#gtkCheckVersionImpl(int,int,int);private native#boolean#load_gtk_icon(java.lang.String);private native#boolean#load_stock_icon(int,java.lang.String,int,int,java.lang.String);private native#void#nativeSync();private static native#boolean#check_gtk(int);private static native#boolean#load_gtk(int,boolean);private static native#boolean#unload_gtk();private static native#int#get_gtk_version();private static#boolean#containsWaylandWindowFocusListener(java.awt.Window);private static#void#addWaylandWindowFocusListenerToWindow(java.awt.Window);protected#java.awt.RenderingHints#getDesktopAAHints();protected#java.lang.Object#lazilyLoadDesktopProperty(java.lang.String);protected#java.lang.Object#lazilyLoadGTKIcon(java.lang.String);public static#boolean#isGtkVerbose();public static#boolean#isOnWayland();public static#int#getDatatransferTimeout();public static#sun.awt.UNIXToolkit$GtkVersions#getEnabledGtkVersion();public static#sun.awt.UNIXToolkit$GtkVersions#getGtkVersion();public#boolean#checkGtkVersion(int,int,int);public#boolean#isNativeGTKAvailable();public#boolean#isRunningOnWayland();public#boolean#loadGTK();public#boolean#shouldDisableSystemTray();public#java.awt.image.BufferedImage#getGTKIcon(java.lang.String);public#java.awt.image.BufferedImage#getStockIcon(int,java.lang.String,int,int,java.lang.String);public#java.lang.Integer#getGnomeShellMajorVersion();public#java.lang.String#getDesktop();public#void#dismissPopupOnFocusLostIfNeeded(java.awt.Window);public#void#dismissPopupOnFocusLostIfNeededCleanUp(java.awt.Window);public#void#loadIconCallback([B,int,int,int,int,int,boolean);public#void#sync()".equals(methods(c)), "methods");
        check("private static final#[I#BAND_OFFSETS;private static final#[I#BAND_OFFSETS_ALPHA;private static final#int#DEFAULT_DATATRANSFER_TIMEOUT;private static final#java.awt.event.WindowFocusListener#waylandWindowFocusListener;private static volatile#java.lang.Boolean#isOnWayland;private static volatile#java.lang.Boolean#shouldDisableSystemTray;private#java.awt.image.BufferedImage#tmpImage;private#java.lang.Boolean#nativeGTKAvailable;private#java.lang.Boolean#nativeGTKLoaded;public static final#java.lang.Object#GTK_LOCK;public static final#java.lang.String#FONTCONFIGAAHINT".equals(fields(c)), "fields");
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
