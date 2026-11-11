/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XMenuWindow conformance: reflection (no init, native/display-heavy).
 * @run main XMenuWindowTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XMenuWindowTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XMenuWindow", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XMenuWindow".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.X11.XBaseMenuWindow".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#boolean#ensureCreated();#boolean#isCreated();#java.awt.Dimension#getDesiredSize();#java.util.Vector#getMenuTargetItems();#sun.awt.X11.XMenuPeer#getMenuPeer();#void#hide();#void#postInit(sun.awt.X11.XCreateWindowParams);#void#show(java.awt.Rectangle);protected#java.awt.Dimension#getCaptionSize();protected#java.awt.Rectangle#getSubmenuBounds(java.awt.Rectangle,java.awt.Dimension);protected#sun.awt.X11.XBaseMenuWindow#getParentMenuWindow();protected#sun.awt.X11.XMenuWindow$MappingData#map();protected#void#paintCaption(java.awt.Graphics,java.awt.Rectangle);protected#void#updateSize();public#void#paintPeer(java.awt.Graphics)".equals(methods(c)), "methods");
        check("private static final#[I#CHECKMARK_X;private static final#[I#CHECKMARK_Y;private static final#int#CHECKMARK_SIZE;private static final#int#WINDOW_ITEM_INDENT;private static final#int#WINDOW_ITEM_MARGIN_BOTTOM;private static final#int#WINDOW_ITEM_MARGIN_LEFT;private static final#int#WINDOW_ITEM_MARGIN_RIGHT;private static final#int#WINDOW_ITEM_MARGIN_TOP;private static final#int#WINDOW_SHORTCUT_SPACING;private static final#int#WINDOW_SPACING_BOTTOM;private static final#int#WINDOW_SPACING_LEFT;private static final#int#WINDOW_SPACING_RIGHT;private static final#int#WINDOW_SPACING_TOP;private static#sun.util.logging.PlatformLogger#log;private#sun.awt.X11.XMenuPeer#menuPeer".equals(fields(c)), "fields");
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
