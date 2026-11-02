/*
 * @test
 * @summary sun.awt.ComponentFactory conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main ComponentFactoryTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ComponentFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.ComponentFactory", false, ComponentFactoryTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.ComponentFactory".equals(c.getName()), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public#java.awt.dnd.peer.DragSourceContextPeer#createDragSourceContextPeer(java.awt.dnd.DragGestureEvent);public#java.awt.peer.ButtonPeer#createButton(java.awt.Button);public#java.awt.peer.CanvasPeer#createCanvas(java.awt.Canvas);public#java.awt.peer.CheckboxMenuItemPeer#createCheckboxMenuItem(java.awt.CheckboxMenuItem);public#java.awt.peer.CheckboxPeer#createCheckbox(java.awt.Checkbox);public#java.awt.peer.ChoicePeer#createChoice(java.awt.Choice);public#java.awt.peer.DesktopPeer#createDesktopPeer(java.awt.Desktop);public#java.awt.peer.DialogPeer#createDialog(java.awt.Dialog);public#java.awt.peer.FileDialogPeer#createFileDialog(java.awt.FileDialog);public#java.awt.peer.FontPeer#getFontPeer(java.lang.String,int);public#java.awt.peer.FramePeer#createFrame(java.awt.Frame);public#java.awt.peer.LabelPeer#createLabel(java.awt.Label);public#java.awt.peer.LightweightPeer#createComponent(java.awt.Component);public#java.awt.peer.ListPeer#createList(java.awt.List);public#java.awt.peer.MenuBarPeer#createMenuBar(java.awt.MenuBar);public#java.awt.peer.MenuItemPeer#createMenuItem(java.awt.MenuItem);public#java.awt.peer.MenuPeer#createMenu(java.awt.Menu);public#java.awt.peer.MouseInfoPeer#getMouseInfoPeer();public#java.awt.peer.PanelPeer#createPanel(java.awt.Panel);public#java.awt.peer.PopupMenuPeer#createPopupMenu(java.awt.PopupMenu);public#java.awt.peer.RobotPeer#createRobot(java.awt.GraphicsDevice);public#java.awt.peer.ScrollPanePeer#createScrollPane(java.awt.ScrollPane);public#java.awt.peer.ScrollbarPeer#createScrollbar(java.awt.Scrollbar);public#java.awt.peer.TaskbarPeer#createTaskbarPeer(java.awt.Taskbar);public#java.awt.peer.TextAreaPeer#createTextArea(java.awt.TextArea);public#java.awt.peer.TextFieldPeer#createTextField(java.awt.TextField);public#java.awt.peer.WindowPeer#createWindow(java.awt.Window);public#sun.awt.datatransfer.DataTransferer#getDataTransferer()".equals(methods(c)), "methods");
        check("".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 0, "ctors");
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
