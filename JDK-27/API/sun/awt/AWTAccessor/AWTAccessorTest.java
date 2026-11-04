/*
 * @test
 * @summary sun.awt.AWTAccessor conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main AWTAccessorTest
 */

import java.util.*;
import java.lang.reflect.*;

public class AWTAccessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.AWTAccessor", false, AWTAccessorTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.AWTAccessor".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#void#ensureClassInitialized(java.lang.Class);public static#sun.awt.AWTAccessor$AWTEventAccessor#getAWTEventAccessor();public static#sun.awt.AWTAccessor$AccessibleBundleAccessor#getAccessibleBundleAccessor();public static#sun.awt.AWTAccessor$AccessibleContextAccessor#getAccessibleContextAccessor();public static#sun.awt.AWTAccessor$CheckboxMenuItemAccessor#getCheckboxMenuItemAccessor();public static#sun.awt.AWTAccessor$ClientPropertyKeyAccessor#getClientPropertyKeyAccessor();public static#sun.awt.AWTAccessor$ComponentAccessor#getComponentAccessor();public static#sun.awt.AWTAccessor$ContainerAccessor#getContainerAccessor();public static#sun.awt.AWTAccessor$CursorAccessor#getCursorAccessor();public static#sun.awt.AWTAccessor$DefaultKeyboardFocusManagerAccessor#getDefaultKeyboardFocusManagerAccessor();public static#sun.awt.AWTAccessor$DragSourceContextAccessor#getDragSourceContextAccessor();public static#sun.awt.AWTAccessor$DropTargetContextAccessor#getDropTargetContextAccessor();public static#sun.awt.AWTAccessor$EventQueueAccessor#getEventQueueAccessor();public static#sun.awt.AWTAccessor$FileDialogAccessor#getFileDialogAccessor();public static#sun.awt.AWTAccessor$FrameAccessor#getFrameAccessor();public static#sun.awt.AWTAccessor$ICC_ProfileAccessor#getICC_ProfileAccessor();public static#sun.awt.AWTAccessor$InputEventAccessor#getInputEventAccessor();public static#sun.awt.AWTAccessor$InvocationEventAccessor#getInvocationEventAccessor();public static#sun.awt.AWTAccessor$KeyEventAccessor#getKeyEventAccessor();public static#sun.awt.AWTAccessor$KeyboardFocusManagerAccessor#getKeyboardFocusManagerAccessor();public static#sun.awt.AWTAccessor$MenuAccessor#getMenuAccessor();public static#sun.awt.AWTAccessor$MenuBarAccessor#getMenuBarAccessor();public static#sun.awt.AWTAccessor$MenuComponentAccessor#getMenuComponentAccessor();public static#sun.awt.AWTAccessor$MenuItemAccessor#getMenuItemAccessor();public static#sun.awt.AWTAccessor$MouseEventAccessor#getMouseEventAccessor();public static#sun.awt.AWTAccessor$PopupMenuAccessor#getPopupMenuAccessor();public static#sun.awt.AWTAccessor$ScrollPaneAdjustableAccessor#getScrollPaneAdjustableAccessor();public static#sun.awt.AWTAccessor$SequencedEventAccessor#getSequencedEventAccessor();public static#sun.awt.AWTAccessor$SystemColorAccessor#getSystemColorAccessor();public static#sun.awt.AWTAccessor$SystemTrayAccessor#getSystemTrayAccessor();public static#sun.awt.AWTAccessor$ToolkitAccessor#getToolkitAccessor();public static#sun.awt.AWTAccessor$TrayIconAccessor#getTrayIconAccessor();public static#sun.awt.AWTAccessor$WindowAccessor#getWindowAccessor();public static#void#setAWTEventAccessor(sun.awt.AWTAccessor$AWTEventAccessor);public static#void#setAccessibleBundleAccessor(sun.awt.AWTAccessor$AccessibleBundleAccessor);public static#void#setAccessibleContextAccessor(sun.awt.AWTAccessor$AccessibleContextAccessor);public static#void#setCheckboxMenuItemAccessor(sun.awt.AWTAccessor$CheckboxMenuItemAccessor);public static#void#setClientPropertyKeyAccessor(sun.awt.AWTAccessor$ClientPropertyKeyAccessor);public static#void#setComponentAccessor(sun.awt.AWTAccessor$ComponentAccessor);public static#void#setContainerAccessor(sun.awt.AWTAccessor$ContainerAccessor);public static#void#setCursorAccessor(sun.awt.AWTAccessor$CursorAccessor);public static#void#setDefaultKeyboardFocusManagerAccessor(sun.awt.AWTAccessor$DefaultKeyboardFocusManagerAccessor);public static#void#setDragSourceContextAccessor(sun.awt.AWTAccessor$DragSourceContextAccessor);public static#void#setDropTargetContextAccessor(sun.awt.AWTAccessor$DropTargetContextAccessor);public static#void#setEventQueueAccessor(sun.awt.AWTAccessor$EventQueueAccessor);public static#void#setFileDialogAccessor(sun.awt.AWTAccessor$FileDialogAccessor);public static#void#setFrameAccessor(sun.awt.AWTAccessor$FrameAccessor);public static#void#setICC_ProfileAccessor(sun.awt.AWTAccessor$ICC_ProfileAccessor);public static#void#setInputEventAccessor(sun.awt.AWTAccessor$InputEventAccessor);public static#void#setInvocationEventAccessor(sun.awt.AWTAccessor$InvocationEventAccessor);public static#void#setKeyEventAccessor(sun.awt.AWTAccessor$KeyEventAccessor);public static#void#setKeyboardFocusManagerAccessor(sun.awt.AWTAccessor$KeyboardFocusManagerAccessor);public static#void#setMenuAccessor(sun.awt.AWTAccessor$MenuAccessor);public static#void#setMenuBarAccessor(sun.awt.AWTAccessor$MenuBarAccessor);public static#void#setMenuComponentAccessor(sun.awt.AWTAccessor$MenuComponentAccessor);public static#void#setMenuItemAccessor(sun.awt.AWTAccessor$MenuItemAccessor);public static#void#setMouseEventAccessor(sun.awt.AWTAccessor$MouseEventAccessor);public static#void#setPopupMenuAccessor(sun.awt.AWTAccessor$PopupMenuAccessor);public static#void#setScrollPaneAdjustableAccessor(sun.awt.AWTAccessor$ScrollPaneAdjustableAccessor);public static#void#setSequencedEventAccessor(sun.awt.AWTAccessor$SequencedEventAccessor);public static#void#setSystemColorAccessor(sun.awt.AWTAccessor$SystemColorAccessor);public static#void#setSystemTrayAccessor(sun.awt.AWTAccessor$SystemTrayAccessor);public static#void#setToolkitAccessor(sun.awt.AWTAccessor$ToolkitAccessor);public static#void#setTrayIconAccessor(sun.awt.AWTAccessor$TrayIconAccessor);public static#void#setWindowAccessor(sun.awt.AWTAccessor$WindowAccessor)".equals(methods(c)), "methods");
        check("private static#sun.awt.AWTAccessor$AWTEventAccessor#awtEventAccessor;private static#sun.awt.AWTAccessor$AccessibleBundleAccessor#accessibleBundleAccessor;private static#sun.awt.AWTAccessor$AccessibleContextAccessor#accessibleContextAccessor;private static#sun.awt.AWTAccessor$CheckboxMenuItemAccessor#checkboxMenuItemAccessor;private static#sun.awt.AWTAccessor$ClientPropertyKeyAccessor#clientPropertyKeyAccessor;private static#sun.awt.AWTAccessor$ComponentAccessor#componentAccessor;private static#sun.awt.AWTAccessor$ContainerAccessor#containerAccessor;private static#sun.awt.AWTAccessor$CursorAccessor#cursorAccessor;private static#sun.awt.AWTAccessor$DefaultKeyboardFocusManagerAccessor#defaultKeyboardFocusManagerAccessor;private static#sun.awt.AWTAccessor$DragSourceContextAccessor#dragSourceContextAccessor;private static#sun.awt.AWTAccessor$DropTargetContextAccessor#dropTargetContextAccessor;private static#sun.awt.AWTAccessor$EventQueueAccessor#eventQueueAccessor;private static#sun.awt.AWTAccessor$FileDialogAccessor#fileDialogAccessor;private static#sun.awt.AWTAccessor$FrameAccessor#frameAccessor;private static#sun.awt.AWTAccessor$ICC_ProfileAccessor#iccProfileAccessor;private static#sun.awt.AWTAccessor$InputEventAccessor#inputEventAccessor;private static#sun.awt.AWTAccessor$InvocationEventAccessor#invocationEventAccessor;private static#sun.awt.AWTAccessor$KeyEventAccessor#keyEventAccessor;private static#sun.awt.AWTAccessor$KeyboardFocusManagerAccessor#kfmAccessor;private static#sun.awt.AWTAccessor$MenuAccessor#menuAccessor;private static#sun.awt.AWTAccessor$MenuBarAccessor#menuBarAccessor;private static#sun.awt.AWTAccessor$MenuComponentAccessor#menuComponentAccessor;private static#sun.awt.AWTAccessor$MenuItemAccessor#menuItemAccessor;private static#sun.awt.AWTAccessor$MouseEventAccessor#mouseEventAccessor;private static#sun.awt.AWTAccessor$PopupMenuAccessor#popupMenuAccessor;private static#sun.awt.AWTAccessor$ScrollPaneAdjustableAccessor#scrollPaneAdjustableAccessor;private static#sun.awt.AWTAccessor$SequencedEventAccessor#sequencedEventAccessor;private static#sun.awt.AWTAccessor$SystemColorAccessor#systemColorAccessor;private static#sun.awt.AWTAccessor$SystemTrayAccessor#systemTrayAccessor;private static#sun.awt.AWTAccessor$ToolkitAccessor#toolkitAccessor;private static#sun.awt.AWTAccessor$TrayIconAccessor#trayIconAccessor;private static#sun.awt.AWTAccessor$WindowAccessor#windowAccessor".equals(fields(c)), "fields");
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
