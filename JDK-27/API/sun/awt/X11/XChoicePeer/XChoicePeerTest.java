/*
 * @test
 * @summary sun.awt.X11.XChoicePeer conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XChoicePeerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XChoicePeerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XChoicePeer", false, XChoicePeerTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XChoicePeer".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("sun.awt.X11.XComponentPeer".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.peer.ChoicePeer;sun.awt.X11.ToplevelStateListener".equals(interfaces(c)), "interfaces");
        check("#boolean#prePostEvent(java.awt.AWTEvent);#void#handleJavaKeyEvent(java.awt.event.KeyEvent);#void#handleJavaMouseEvent(java.awt.event.MouseEvent);#void#handleJavaMouseWheelEvent(java.awt.event.MouseWheelEvent);#void#hidePopdownMenu();#void#paintPeer(java.awt.Graphics);#void#postInit(sun.awt.X11.XCreateWindowParams);#void#preInit(sun.awt.X11.XCreateWindowParams);#void#ungrabInputImpl();private#boolean#isMouseEventInChoice(java.awt.event.MouseEvent);protected#void#initGraphicsConfiguration();protected#void#paintFocus(java.awt.Graphics,int,int,int,int);public#boolean#handleMouseEventByChoice(java.awt.event.MouseEvent);public#boolean#handlesWheelScrolling();public#boolean#isFocusable();public#boolean#isUnfurled();public#java.awt.Dimension#getMinimumSize();public#void#add(java.lang.String,int);public#void#addXChoicePeerListener(sun.awt.X11.XChoicePeerListener);public#void#dispose();public#void#focusGained(java.awt.event.FocusEvent);public#void#focusLost(java.awt.event.FocusEvent);public#void#keyPressed(java.awt.event.KeyEvent);public#void#layout();public#void#mouseDragged(java.awt.event.MouseEvent);public#void#mousePressed(java.awt.event.MouseEvent);public#void#mouseReleased(java.awt.event.MouseEvent);public#void#remove(int);public#void#removeAll();public#void#removeXChoicePeerListener();public#void#select(int);public#void#setAlignUnder(java.awt.Component);public#void#setBackground(java.awt.Color);public#void#setBounds(int,int,int,int,int);public#void#setDrawSelectedItem(boolean);public#void#setEnabled(boolean);public#void#setFont(java.awt.Font);public#void#setForeground(java.awt.Color);public#void#stateChangedICCCM(int,int);public#void#stateChangedJava(int,int)".equals(methods(c)), "methods");
        check("private static final#int#MAX_UNFURLED_ITEMS;private static final#java.awt.Insets#focusInsets;private static final#sun.util.logging.PlatformLogger#log;private#boolean#dragging;private#boolean#drawSelectedItem;private#boolean#firstPress;private#boolean#mouseInSB;private#boolean#unfurled;private#boolean#wasDragged;private#int#dragStartIdx;private#java.awt.Component#alignUnder;private#sun.awt.X11.ListHelper#helper;private#sun.awt.X11.XChoicePeer$UnfurledChoice#unfurledChoice;private#sun.awt.X11.XChoicePeerListener#choiceListener;public static final#int#BORDER_WIDTH;public static final#int#ITEM_MARGIN;public static final#int#SCROLLBAR_WIDTH;public static final#int#TEXT_SPACE;static final#int#TEXT_XPAD;static final#int#TEXT_YPAD;static final#int#WIDGET_OFFSET;static final#java.awt.Color#focusColor".equals(fields(c)), "fields");
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
