/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XAtom conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XAtomTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XAtomTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XAtom", false, XAtomTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XAtom".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#[Lsun.awt.X11.XAtom;#getAtomListProperty(long);#[Lsun.awt.X11.XAtom;#getAtomListProperty(sun.awt.X11.XBaseWindow);#long#getAtom();#sun.awt.X11.XAtomList#getAtomListPropertyList(long);#sun.awt.X11.XAtomList#getAtomListPropertyList(sun.awt.X11.XBaseWindow);#void#checkWindow(long);#void#putAtom(long);#void#register();#void#setAtomListProperty(long,[Lsun.awt.X11.XAtom;);#void#setAtomListProperty(long,sun.awt.X11.XAtomList);public static#sun.awt.X11.XAtom#get(java.lang.String);public#[B#getByteArrayProperty(long,long);public#boolean#equals(java.lang.Object);public#boolean#getAtomData(long,long,int);public#boolean#getAtomData(long,long,long,int);public#boolean#isInterned();public#int#hashCode();public#java.lang.String#getName();public#java.lang.String#getProperty(long);public#java.lang.String#toString();public#long#get32Property(long,long);public#long#getCard32Property(sun.awt.X11.XBaseWindow);public#long#getWindowProperty(long);public#void#DeleteProperty(long);public#void#DeleteProperty(sun.awt.X11.XBaseWindow);public#void#intern(boolean);public#void#setAtomData(long,long,[B);public#void#setAtomData(long,long,int);public#void#setAtomData(long,long,long,int);public#void#setAtomData8(long,long,long,int);public#void#setAtomListProperty(sun.awt.X11.XBaseWindow,[Lsun.awt.X11.XAtom;);public#void#setAtomListProperty(sun.awt.X11.XBaseWindow,sun.awt.X11.XAtomList);public#void#setCard32Property(long,long);public#void#setCard32Property(sun.awt.X11.XBaseWindow,long);public#void#setProperty(long,java.lang.String);public#void#setProperty8(long,java.lang.String);public#void#setPropertyUTF8(long,java.lang.String);public#void#setValues(long,java.lang.String,long);public#void#setWindowProperty(long,long);public#void#setWindowProperty(sun.awt.X11.XBaseWindow,sun.awt.X11.XBaseWindow);static#int#getAtomSize();static#java.lang.String#asString(long);static#long#getAtom(long);static#long#toData([Lsun.awt.X11.XAtom;);static#sun.awt.X11.XAtom#get(long);static#sun.awt.X11.XAtom#lookup(java.lang.String);static#sun.awt.X11.XAtom#lookup(long);static#void#register(sun.awt.X11.XAtom)".equals(methods(c)), "methods");
        check("#java.lang.String#name;#long#atom;#long#display;private static#[Lsun.awt.X11.XAtom;#emptyList;private static#jdk.internal.misc.Unsafe#unsafe;public static final#long#XA_ARC;public static final#long#XA_ATOM;public static final#long#XA_BITMAP;public static final#long#XA_CAP_HEIGHT;public static final#long#XA_CARDINAL;public static final#long#XA_COLORMAP;public static final#long#XA_COPYRIGHT;public static final#long#XA_CURSOR;public static final#long#XA_CUT_BUFFER0;public static final#long#XA_CUT_BUFFER1;public static final#long#XA_CUT_BUFFER2;public static final#long#XA_CUT_BUFFER3;public static final#long#XA_CUT_BUFFER4;public static final#long#XA_CUT_BUFFER5;public static final#long#XA_CUT_BUFFER6;public static final#long#XA_CUT_BUFFER7;public static final#long#XA_DRAWABLE;public static final#long#XA_END_SPACE;public static final#long#XA_FAMILY_NAME;public static final#long#XA_FONT;public static final#long#XA_FONT_NAME;public static final#long#XA_FULL_NAME;public static final#long#XA_INTEGER;public static final#long#XA_ITALIC_ANGLE;public static final#long#XA_LAST_PREDEFINED;public static final#long#XA_MAX_SPACE;public static final#long#XA_MIN_SPACE;public static final#long#XA_NORM_SPACE;public static final#long#XA_NOTICE;public static final#long#XA_PIXMAP;public static final#long#XA_POINT;public static final#long#XA_POINT_SIZE;public static final#long#XA_PRIMARY;public static final#long#XA_QUAD_WIDTH;public static final#long#XA_RECTANGLE;public static final#long#XA_RESOLUTION;public static final#long#XA_RESOURCE_MANAGER;public static final#long#XA_RGB_BEST_MAP;public static final#long#XA_RGB_BLUE_MAP;public static final#long#XA_RGB_COLOR_MAP;public static final#long#XA_RGB_DEFAULT_MAP;public static final#long#XA_RGB_GRAY_MAP;public static final#long#XA_RGB_GREEN_MAP;public static final#long#XA_RGB_RED_MAP;public static final#long#XA_SECONDARY;public static final#long#XA_STRIKEOUT_ASCENT;public static final#long#XA_STRIKEOUT_DESCENT;public static final#long#XA_STRING;public static final#long#XA_SUBSCRIPT_X;public static final#long#XA_SUBSCRIPT_Y;public static final#long#XA_SUPERSCRIPT_X;public static final#long#XA_SUPERSCRIPT_Y;public static final#long#XA_UNDERLINE_POSITION;public static final#long#XA_UNDERLINE_THICKNESS;public static final#long#XA_VISUALID;public static final#long#XA_WEIGHT;public static final#long#XA_WINDOW;public static final#long#XA_WM_CLASS;public static final#long#XA_WM_CLIENT_MACHINE;public static final#long#XA_WM_COMMAND;public static final#long#XA_WM_HINTS;public static final#long#XA_WM_ICON_NAME;public static final#long#XA_WM_ICON_SIZE;public static final#long#XA_WM_NAME;public static final#long#XA_WM_NORMAL_HINTS;public static final#long#XA_WM_SIZE_HINTS;public static final#long#XA_WM_TRANSIENT_FOR;public static final#long#XA_WM_ZOOM_HINTS;public static final#long#XA_X_HEIGHT;static#java.util.HashMap#atomToAtom;static#java.util.HashMap#nameToAtom".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 5, "ctors");
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
