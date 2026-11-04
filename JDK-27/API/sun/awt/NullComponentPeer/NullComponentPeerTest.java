/*
 * @test
 * @summary sun.awt.NullComponentPeer conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main NullComponentPeerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class NullComponentPeerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.NullComponentPeer", false, NullComponentPeerTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.NullComponentPeer".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.peer.CanvasPeer;java.awt.peer.LightweightPeer;java.awt.peer.PanelPeer".equals(interfaces(c)), "interfaces");
        check("public#boolean#canDetermineObscurity();public#boolean#handleEvent(java.awt.Event);public#boolean#handlesWheelScrolling();public#boolean#isFocusable();public#boolean#isObscured();public#boolean#isPaintPending();public#boolean#isReparentSupported();public#boolean#requestFocus(java.awt.Component,boolean,boolean,long,java.awt.event.FocusEvent$Cause);public#boolean#updateGraphicsData(java.awt.GraphicsConfiguration);public#java.awt.Dimension#getMinimumSize();public#java.awt.Dimension#getPreferredSize();public#java.awt.Dimension#minimumSize();public#java.awt.Dimension#preferredSize();public#java.awt.FontMetrics#getFontMetrics(java.awt.Font);public#java.awt.Graphics#getGraphics();public#java.awt.GraphicsConfiguration#getAppropriateGraphicsConfiguration(java.awt.GraphicsConfiguration);public#java.awt.GraphicsConfiguration#getGraphicsConfiguration();public#java.awt.Image#createImage(int,int);public#java.awt.Image#getBackBuffer();public#java.awt.Insets#getInsets();public#java.awt.Insets#insets();public#java.awt.Point#getLocationOnScreen();public#java.awt.Rectangle#getBounds();public#java.awt.image.ColorModel#getColorModel();public#java.awt.image.VolatileImage#createVolatileImage(int,int);public#void#applyShape(sun.java2d.pipe.Region);public#void#beginLayout();public#void#beginValidate();public#void#coalescePaintEvent(java.awt.event.PaintEvent);public#void#createBuffers(int,java.awt.BufferCapabilities);public#void#destroyBuffers();public#void#disable();public#void#dispose();public#void#enable();public#void#endLayout();public#void#endValidate();public#void#flip(int,int,int,int,java.awt.BufferCapabilities$FlipContents);public#void#handleEvent(java.awt.AWTEvent);public#void#hide();public#void#layout();public#void#paint(java.awt.Graphics);public#void#print(java.awt.Graphics);public#void#repaint(long,int,int,int,int);public#void#reparent(java.awt.peer.ContainerPeer);public#void#reshape(int,int,int,int);public#void#setBackground(java.awt.Color);public#void#setBounds(int,int,int,int,int);public#void#setCursor(java.awt.Cursor);public#void#setEnabled(boolean);public#void#setFont(java.awt.Font);public#void#setForeground(java.awt.Color);public#void#setVisible(boolean);public#void#setZOrder(java.awt.peer.ComponentPeer);public#void#show();public#void#updateCursorImmediately()".equals(methods(c)), "methods");
        check("".equals(fields(c)), "fields");
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
