/*
 * @test
 * @summary sun.awt.X11.XUtilConstants conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XUtilConstantsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XUtilConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XUtilConstants", false, XUtilConstantsTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XUtilConstants".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("".equals(methods(c)), "methods");
        check("public static final#int#AllValues;public static final#int#BitmapFileInvalid;public static final#int#BitmapNoMemory;public static final#int#BitmapOpenFailed;public static final#int#BitmapSuccess;public static final#int#DontCareState;public static final#int#HeightValue;public static final#int#IconicState;public static final#int#InactiveState;public static final#int#NoValue;public static final#int#NormalState;public static final#int#RectangleIn;public static final#int#RectangleOut;public static final#int#RectanglePart;public static final#int#VisualAllMask;public static final#int#VisualBitsPerRGBMask;public static final#int#VisualBlueMaskMask;public static final#int#VisualClassMask;public static final#int#VisualColormapSizeMask;public static final#int#VisualDepthMask;public static final#int#VisualGreenMaskMask;public static final#int#VisualIDMask;public static final#int#VisualNoMask;public static final#int#VisualRedMaskMask;public static final#int#VisualScreenMask;public static final#int#WidthValue;public static final#int#WithdrawnState;public static final#int#XCNOENT;public static final#int#XCNOMEM;public static final#int#XCSUCCESS;public static final#int#XConverterNotFound;public static final#int#XLocaleNotSupported;public static final#int#XNegative;public static final#int#XNoMemory;public static final#int#XValue;public static final#int#YNegative;public static final#int#YValue;public static final#int#ZoomState;public static final#long#AllHints;public static final#long#IconMaskHint;public static final#long#IconPixmapHint;public static final#long#IconPositionHint;public static final#long#IconWindowHint;public static final#long#InputHint;public static final#long#PAllHints;public static final#long#PAspect;public static final#long#PBaseSize;public static final#long#PMaxSize;public static final#long#PMinSize;public static final#long#PPosition;public static final#long#PResizeInc;public static final#long#PSize;public static final#long#PWinGravity;public static final#long#StateHint;public static final#long#USPosition;public static final#long#USSize;public static final#long#WindowGroupHint;public static final#long#XUrgencyHint".equals(fields(c)), "fields");
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
