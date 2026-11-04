/*
 * @test
 * @summary sun.awt.X11.XMenuBarPeer conformance: reflection (no init, native/display-heavy).
 * @run main XMenuBarPeerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XMenuBarPeerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XMenuBarPeer", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XMenuBarPeer".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.X11.XBaseMenuWindow".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.peer.MenuBarPeer".equals(interfaces(c)), "interfaces");
        check("#boolean#isFramePeerEnabled();#int#getDesiredHeight();#void#handleF10KeyPress(java.awt.event.KeyEvent);#void#postInit(sun.awt.X11.XCreateWindowParams);#void#print(java.awt.Graphics);#void#ungrabInputImpl();protected#java.awt.Rectangle#getSubmenuBounds(java.awt.Rectangle,java.awt.Dimension);protected#sun.awt.X11.XBaseMenuWindow#getParentMenuWindow();protected#sun.awt.X11.XMenuBarPeer$MappingData#map();protected#void#doDispose();protected#void#handleEvent(java.awt.AWTEvent);protected#void#updateSize();public#void#addHelpMenu(java.awt.Menu);public#void#addMenu(java.awt.Menu);public#void#delMenu(int);public#void#handleKeyPress(sun.awt.X11.XEvent);public#void#init(java.awt.Frame);public#void#paintPeer(java.awt.Graphics);public#void#reshape(int,int,int,int);public#void#setFont(java.awt.Font)".equals(methods(c)), "methods");
        check("private static final#int#BAR_ITEM_MARGIN_BOTTOM;private static final#int#BAR_ITEM_MARGIN_LEFT;private static final#int#BAR_ITEM_MARGIN_RIGHT;private static final#int#BAR_ITEM_MARGIN_TOP;private static final#int#BAR_ITEM_SPACING;private static final#int#BAR_SPACING_BOTTOM;private static final#int#BAR_SPACING_LEFT;private static final#int#BAR_SPACING_RIGHT;private static final#int#BAR_SPACING_TOP;private static#sun.util.logging.PlatformLogger#log;private#java.awt.MenuBar#menuBarTarget;private#sun.awt.X11.XFramePeer#framePeer;private#sun.awt.X11.XMenuPeer#helpMenu;static final#int#H_DIFF;static final#int#W_DIFF".equals(fields(c)), "fields");
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
