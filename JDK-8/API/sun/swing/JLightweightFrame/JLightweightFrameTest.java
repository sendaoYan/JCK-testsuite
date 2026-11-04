/*
 * @test
 * @summary sun.swing.JLightweightFrame conformance: reflection (no init, native/display-heavy).
 * @run main JLightweightFrameTest
 */

import java.util.*;
import java.lang.reflect.*;

public class JLightweightFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.JLightweightFrame", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.swing.JLightweightFrame".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.LightweightFrame".equals(c.getSuperclass().getName()), "superclass");
        check("javax.swing.RootPaneContainer".equals(interfaces(c)), "interfaces");
        check("private#void#initInterior();private#void#notifyImageUpdated(int,int,int,int);private#void#resizeBuffer(int,int,int);private#void#syncCopyBuffer(boolean,int,int,int,int,int);private#void#updateClientCursor();public#int#getScaleFactor();public#java.awt.Component#getGlassPane();public#java.awt.Container#getContentPane();public#java.awt.Graphics#getGraphics();public#java.awt.dnd.DragGestureRecognizer#createDragGestureRecognizer(java.lang.Class,java.awt.dnd.DragSource,java.awt.Component,int,java.awt.dnd.DragGestureListener);public#java.awt.dnd.peer.DragSourceContextPeer#createDragSourceContextPeer(java.awt.dnd.DragGestureEvent);public#javax.swing.JLayeredPane#getLayeredPane();public#javax.swing.JRootPane#getRootPane();public#void#addDropTarget(java.awt.dnd.DropTarget);public#void#addNotify();public#void#dispose();public#void#grabFocus();public#void#notifyDisplayChanged(int);public#void#overrideNativeWindowHandle(long,java.lang.Runnable);public#void#removeDropTarget(java.awt.dnd.DropTarget);public#void#reshape(int,int,int,int);public#void#setContent(sun.swing.LightweightContent);public#void#setContentPane(java.awt.Container);public#void#setGlassPane(java.awt.Component);public#void#setLayeredPane(javax.swing.JLayeredPane);public#void#ungrabFocus()".equals(methods(c)), "methods");
        check("private final#javax.swing.JRootPane#rootPane;private static#boolean#copyBufferEnabled;private volatile#int#scaleFactor;private#[I#copyBuffer;private#java.awt.Component#component;private#java.awt.image.BufferedImage#bbImage;private#java.beans.PropertyChangeListener#layoutSizeListener;private#javax.swing.JPanel#contentPane;private#sun.swing.LightweightContent#content;private#sun.swing.SwingUtilities2$RepaintListener#repaintListener".equals(fields(c)), "fields");
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
