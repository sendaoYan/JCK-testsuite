/*
 * @test
 * @summary sun.awt.HeadlessToolkit conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main HeadlessToolkitTest
 */

import java.util.*;
import java.lang.reflect.*;

public class HeadlessToolkitTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.HeadlessToolkit", false, HeadlessToolkitTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.HeadlessToolkit".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.awt.Toolkit".equals(c.getSuperclass().getName()), "superclass");
        check("sun.awt.ComponentFactory;sun.awt.KeyboardFocusManagerPeerProvider".equals(interfaces(c)), "interfaces");
        check("protected#boolean#isDynamicLayoutSet();protected#void#loadSystemColors([I);public#[Ljava.awt.event.AWTEventListener;#getAWTEventListeners();public#[Ljava.awt.event.AWTEventListener;#getAWTEventListeners(long);public#[Ljava.lang.String;#getFontList();public#boolean#areExtraMouseButtonsEnabled();public#boolean#getLockingKeyState(int);public#boolean#isAlwaysOnTopSupported();public#boolean#isDesktopSupported();public#boolean#isDynamicLayoutActive();public#boolean#isModalExclusionTypeSupported(java.awt.Dialog$ModalExclusionType);public#boolean#isModalityTypeSupported(java.awt.Dialog$ModalityType);public#boolean#isTraySupported();public#boolean#prepareImage(java.awt.Image,int,int,java.awt.image.ImageObserver);public#int#checkImage(java.awt.Image,int,int,java.awt.image.ImageObserver);public#int#getMaximumCursorColors();public#int#getMenuShortcutKeyMask();public#int#getMenuShortcutKeyMaskEx();public#int#getScreenResolution();public#java.awt.Cursor#createCustomCursor(java.awt.Image,java.awt.Point,java.lang.String);public#java.awt.Dimension#getBestCursorSize(int,int);public#java.awt.Dimension#getScreenSize();public#java.awt.EventQueue#getSystemEventQueueImpl();public#java.awt.FontMetrics#getFontMetrics(java.awt.Font);public#java.awt.Image#createImage([B);public#java.awt.Image#createImage([B,int,int);public#java.awt.Image#createImage(java.awt.image.ImageProducer);public#java.awt.Image#createImage(java.lang.String);public#java.awt.Image#createImage(java.net.URL);public#java.awt.Image#getImage(java.lang.String);public#java.awt.Image#getImage(java.net.URL);public#java.awt.Insets#getScreenInsets(java.awt.GraphicsConfiguration);public#java.awt.PrintJob#getPrintJob(java.awt.Frame,java.lang.String,java.awt.JobAttributes,java.awt.PageAttributes);public#java.awt.PrintJob#getPrintJob(java.awt.Frame,java.lang.String,java.util.Properties);public#java.awt.Toolkit#getUnderlyingToolkit();public#java.awt.datatransfer.Clipboard#getSystemClipboard();public#java.awt.dnd.DragGestureRecognizer#createDragGestureRecognizer(java.lang.Class,java.awt.dnd.DragSource,java.awt.Component,int,java.awt.dnd.DragGestureListener);public#java.awt.image.ColorModel#getColorModel();public#java.awt.peer.FontPeer#getFontPeer(java.lang.String,int);public#java.awt.peer.KeyboardFocusManagerPeer#getKeyboardFocusManagerPeer();public#java.awt.peer.SystemTrayPeer#createSystemTray(java.awt.SystemTray);public#java.awt.peer.TrayIconPeer#createTrayIcon(java.awt.TrayIcon);public#java.util.Map#mapInputMethodHighlight(java.awt.im.InputMethodHighlight);public#sun.awt.GlobalCursorManager#getGlobalCursorManager();public#void#addAWTEventListener(java.awt.event.AWTEventListener,long);public#void#addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener);public#void#beep();public#void#removeAWTEventListener(java.awt.event.AWTEventListener);public#void#removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener);public#void#setDynamicLayout(boolean);public#void#setLockingKeyState(int,boolean);public#void#sync()".equals(methods(c)), "methods");
        check("private final#java.awt.Toolkit#tk;private static final#java.awt.peer.KeyboardFocusManagerPeer#kfmPeer;private#sun.awt.ComponentFactory#componentFactory".equals(fields(c)), "fields");
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
