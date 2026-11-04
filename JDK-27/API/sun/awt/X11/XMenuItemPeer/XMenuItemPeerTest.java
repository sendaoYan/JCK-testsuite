/*
 * @test
 * @summary sun.awt.X11.XMenuItemPeer conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XMenuItemPeerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XMenuItemPeerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XMenuItemPeer", false, XMenuItemPeerTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XMenuItemPeer".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.peer.MenuItemPeer".equals(interfaces(c)), "interfaces");
        check("#boolean#isContainerShowing();#boolean#isSeparator();#boolean#isTargetEnabled();#boolean#isTargetItemEnabled();#java.awt.Font#getTargetFont();#java.awt.MenuItem#getTarget();#java.awt.MenuShortcut#getTargetShortcut();#java.awt.Point#getTextOrigin();#java.awt.Rectangle#getBounds();#java.lang.String#getShortcutText();#java.lang.String#getTargetActionCommand();#java.lang.String#getTargetLabel();#sun.awt.X11.XBaseMenuWindow#getContainer();#sun.awt.X11.XMenuItemPeer$TextMetrics#calcTextMetrics();#sun.awt.X11.XMenuItemPeer$TextMetrics#getTextMetrics();#void#action(long,int);#void#map(java.awt.Rectangle,java.awt.Point);#void#repaintIfShowing();#void#resetTextMetrics();#void#setContainer(sun.awt.X11.XBaseMenuWindow);public#void#dispose();public#void#setEnabled(boolean);public#void#setFont(java.awt.Font);public#void#setLabel(java.lang.String)".equals(methods(c)), "methods");
        check("private static final#int#SEPARATOR_HEIGHT;private static final#int#SEPARATOR_WIDTH;private#java.awt.MenuItem#target;private#java.awt.Point#textOrigin;private#java.awt.Rectangle#bounds;private#sun.awt.X11.XBaseMenuWindow#container;private#sun.awt.X11.XMenuItemPeer$TextMetrics#textMetrics".equals(fields(c)), "fields");
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
