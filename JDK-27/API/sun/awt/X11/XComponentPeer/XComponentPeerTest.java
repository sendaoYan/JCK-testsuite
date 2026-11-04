/*
 * @test
 * @summary sun.awt.X11.XComponentPeer conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XComponentPeerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XComponentPeerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XComponentPeer", false, XComponentPeerTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XComponentPeer".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.awt.X11.XWindow".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.dnd.peer.DropTargetPeer;java.awt.peer.ComponentPeer;sun.java2d.BackBufferCapsProvider".equals(interfaces(c)), "interfaces");
        check("#java.awt.Color#getPeerBackground();#java.awt.Color#getPeerForeground();#java.awt.Dimension#getPeerSize();#java.awt.Font#getPeerFont();#sun.awt.X11.XWindowPeer#getParentTopLevel();#void#draw3DOval(java.awt.Graphics,java.awt.Color,int,int,int,int,boolean);#void#draw3DRect(java.awt.Graphics,java.awt.Color,int,int,int,int,boolean);#void#drawScrollbar(java.awt.Graphics,java.awt.Color,int,int,int,int,int,int,boolean);#void#handleF10JavaKeyEvent(java.awt.event.KeyEvent);#void#handleJavaFocusEvent(java.awt.AWTEvent);#void#handleJavaInputMethodEvent(java.awt.event.InputMethodEvent);#void#handleJavaKeyEvent(java.awt.event.KeyEvent);#void#handleJavaMouseEvent(java.awt.event.MouseEvent);#void#handleJavaMouseWheelEvent(java.awt.event.MouseWheelEvent);#void#handleJavaWindowFocusEvent(java.awt.AWTEvent);#void#postInit(sun.awt.X11.XCreateWindowParams);#void#preInit(sun.awt.X11.XCreateWindowParams);#void#updateMotifColors(java.awt.Color);private#boolean#rejectFocusRequestHelper(java.lang.String);private#void#addTree(java.util.Collection,java.util.Set,java.awt.Container);protected#boolean#isEventDisabled(sun.awt.X11.XEvent);protected#boolean#isInitialReshape();public final#boolean#hasFocus();public final#boolean#isEnabled();public final#boolean#requestFocus(java.awt.Component,boolean,boolean,long,java.awt.event.FocusEvent$Cause);public final#void#pSetCursor(java.awt.Cursor);public#[Ljava.awt.Color;#getGUIcolors();public#boolean#canDetermineObscurity();public#boolean#handlesWheelScrolling();public#boolean#isFocusable();public#boolean#isObscured();public#boolean#isPaintPending();public#boolean#isReparentSupported();public#boolean#updateGraphicsData(java.awt.GraphicsConfiguration);public#java.awt.BufferCapabilities#getBackBufferCaps();public#java.awt.Color#getWinBackground();public#java.awt.Dimension#getMinimumSize();public#java.awt.Dimension#getPreferredSize();public#java.awt.Font#getFont();public#java.awt.FontMetrics#getFontMetrics(java.awt.Font);public#java.awt.Graphics#getGraphics();public#java.awt.Image#createImage(int,int);public#java.awt.Image#getBackBuffer();public#java.awt.Insets#getInsets();public#java.awt.image.VolatileImage#createVolatileImage(int,int);public#void#addDropTarget(java.awt.dnd.DropTarget);public#void#applyShape(sun.java2d.pipe.Region);public#void#beginLayout();public#void#beginValidate();public#void#coalescePaintEvent(java.awt.event.PaintEvent);public#void#createBuffers(int,java.awt.BufferCapabilities);public#void#destroyBuffers();public#void#draw3DOval(java.awt.Graphics,[Ljava.awt.Color;,int,int,int,int,boolean);public#void#draw3DRect(java.awt.Graphics,[Ljava.awt.Color;,int,int,int,int,boolean);public#void#drawMotif3DRect(java.awt.Graphics,int,int,int,int,boolean);public#void#endLayout();public#void#endValidate();public#void#flip(int,int,int,int,java.awt.BufferCapabilities$FlipContents);public#void#focusGained(java.awt.event.FocusEvent);public#void#focusLost(java.awt.event.FocusEvent);public#void#handleEvent(java.awt.AWTEvent);public#void#hide();public#void#layout();public#void#notifyTextComponentChange(boolean);public#void#pSetCursor(java.awt.Cursor,boolean);public#void#paint(java.awt.Graphics);public#void#print(java.awt.Graphics);public#void#removeDropTarget(java.awt.dnd.DropTarget);public#void#reparent(java.awt.peer.ContainerPeer);public#void#reshape(int,int,int,int);public#void#setBackground(java.awt.Color);public#void#setBounds(int,int,int,int,int);public#void#setBoundsOperation(int);public#void#setEnabled(boolean);public#void#setFont(java.awt.Font);public#void#setForeground(java.awt.Color);public#void#setVisible(boolean);public#void#setZOrder(java.awt.peer.ComponentPeer);public#void#updateCursorImmediately();static final#java.awt.AWTEvent#wrapInSequenced(java.awt.AWTEvent);static#[I#getRGBvals(java.awt.Color);static#[Ljava.awt.Color;#getSystemColors();static#java.lang.String#operationToString(int)".equals(methods(c)), "methods");
        check("#boolean#bHasFocus;#boolean#isLayouting;#boolean#paintPending;#java.awt.Color#background;#java.awt.Color#darkShadow;#java.awt.Color#foreground;#java.awt.Color#lightShadow;#java.awt.Color#selectColor;#java.awt.Font#font;private static final#sun.util.logging.PlatformLogger#buffersLog;private static final#sun.util.logging.PlatformLogger#enableLog;private static final#sun.util.logging.PlatformLogger#focusLog;private static final#sun.util.logging.PlatformLogger#fontLog;private static final#sun.util.logging.PlatformLogger#log;private static final#sun.util.logging.PlatformLogger#shapeLog;private#boolean#enabled;private#java.awt.BufferCapabilities#backBufferCaps;private#java.awt.image.VolatileImage#xBackBuffer;private#long#backBuffer;protected#int#boundsOperation;static final#int#BACKGROUND_COLOR;static final#int#FOREGROUND_COLOR;static final#int#HIGHLIGHT_COLOR;static final#int#SHADOW_COLOR;static#[Ljava.awt.Color;#systemColors".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 4, "ctors");
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
