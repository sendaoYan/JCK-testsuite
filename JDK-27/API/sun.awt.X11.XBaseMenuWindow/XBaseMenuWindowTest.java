/*
 * @test
 * @summary sun.awt.X11.XBaseMenuWindow conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XBaseMenuWindowTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XBaseMenuWindowTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XBaseMenuWindow", false, XBaseMenuWindowTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XBaseMenuWindow".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.awt.X11.XWindow".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#[Lsun.awt.X11.XMenuItemPeer;#copyItems();#java.awt.Color#getBackgroundColor();#java.awt.Color#getDarkShadowColor();#java.awt.Color#getDisabledColor();#java.awt.Color#getForegroundColor();#java.awt.Color#getLightShadowColor();#java.awt.Color#getSelectedColor();#java.awt.GraphicsConfiguration#getCurrentGraphicsConfiguration();#java.awt.Rectangle#fitWindowAbove(java.awt.Rectangle,java.awt.Dimension,java.awt.Rectangle);#java.awt.Rectangle#fitWindowBelow(java.awt.Rectangle,java.awt.Dimension,java.awt.Rectangle);#java.awt.Rectangle#fitWindowLeft(java.awt.Rectangle,java.awt.Dimension,java.awt.Rectangle);#java.awt.Rectangle#fitWindowRight(java.awt.Rectangle,java.awt.Dimension,java.awt.Rectangle);#java.awt.Rectangle#fitWindowToScreen(java.awt.Dimension,java.awt.Rectangle);#sun.awt.X11.XBaseMenuWindow#getMenuWindowFromPoint(java.awt.Point);#sun.awt.X11.XBaseMenuWindow#getRootMenuWindow();#sun.awt.X11.XBaseMenuWindow#getShowingLeaf();#sun.awt.X11.XBaseMenuWindow$MappingData#getMappingData();#sun.awt.X11.XMenuItemPeer#getFirstSelectableItem();#sun.awt.X11.XMenuItemPeer#getItem(int);#sun.awt.X11.XMenuItemPeer#getItemFromPoint(java.awt.Point);#sun.awt.X11.XMenuItemPeer#getNextSelectableItem();#sun.awt.X11.XMenuItemPeer#getPrevSelectableItem();#sun.awt.X11.XMenuItemPeer#getSelectedItem();#sun.awt.X11.XMenuPeer#getShowingSubmenu();#void#doHandleJavaKeyEvent(java.awt.event.KeyEvent);#void#doHandleJavaMouseEvent(java.awt.event.MouseEvent);#void#doHandleJavaPaintEvent(java.awt.event.PaintEvent);#void#draw3DRect(java.awt.Graphics,int,int,int,int,boolean);#void#instantPreInit(sun.awt.X11.XCreateWindowParams);#void#postEvent(java.awt.AWTEvent);#void#postPaintEvent();#void#replaceColors(java.awt.Color);#void#resetColors();#void#selectItem(sun.awt.X11.XMenuItemPeer,boolean);final#void#setItemsFont(java.awt.Font);private#void#doShowSubmenu(sun.awt.X11.XMenuPeer);protected abstract#java.awt.Rectangle#getSubmenuBounds(java.awt.Rectangle,java.awt.Dimension);protected abstract#sun.awt.X11.XBaseMenuWindow#getParentMenuWindow();protected abstract#sun.awt.X11.XBaseMenuWindow$MappingData#map();protected abstract#void#updateSize();protected#boolean#isEventDisabled(sun.awt.X11.XEvent);protected#void#doDispose();protected#void#handleEvent(java.awt.AWTEvent);protected#void#resetMapping();public#boolean#grabInput();public#void#addItem(java.awt.MenuItem);public#void#delItem(int);public#void#dispose();public#void#reloadItems(java.util.Vector);static#java.lang.Object#getMenuTreeLock()".equals(methods(c)), "methods");
        check("private static#java.lang.Object#menuTreeLock;private static#sun.util.logging.PlatformLogger#log;private#int#selectedIndex;private#java.awt.Color#backgroundColor;private#java.awt.Color#darkShadowColor;private#java.awt.Color#disabledColor;private#java.awt.Color#foregroundColor;private#java.awt.Color#lightShadowColor;private#java.awt.Color#selectedColor;private#java.util.ArrayList#items;private#sun.awt.X11.XBaseMenuWindow$MappingData#mappingData;private#sun.awt.X11.XMenuPeer#showingMousePressedSubmenu;private#sun.awt.X11.XMenuPeer#showingSubmenu;protected#boolean#hasPointerMoved;protected#java.awt.Point#grabInputPoint".equals(fields(c)), "fields");
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
