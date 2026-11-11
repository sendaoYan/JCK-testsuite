/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XEmbedChildProxyPeer conformance: reflection (no init, native/display-heavy).
 * @run main XEmbedChildProxyPeerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XEmbedChildProxyPeerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XEmbedChildProxyPeer", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XEmbedChildProxyPeer".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.peer.ComponentPeer;sun.awt.X11.XEventDispatcher".equals(interfaces(c)), "interfaces");
        check("#boolean#simulateMotifRequestFocus(java.awt.Component,boolean,boolean,long);#java.awt.Window#getTopLevel(java.awt.Component);#void#childResized();#void#handleConfigureNotify(sun.awt.X11.XEvent);#void#handlePropertyNotify(sun.awt.X11.XEvent);#void#initDispatching();#void#postEvent(java.awt.AWTEvent);#void#requestXEmbedFocus();public#boolean#canDetermineObscurity();public#boolean#handlesWheelScrolling();public#boolean#isFocusable();public#boolean#isObscured();public#boolean#isReparentSupported();public#boolean#prepareImage(java.awt.Image,int,int,java.awt.image.ImageObserver);public#boolean#requestFocus(java.awt.Component,boolean,boolean,long,sun.awt.CausedFocusEvent$Cause);public#boolean#updateGraphicsData(java.awt.GraphicsConfiguration);public#int#checkImage(java.awt.Image,int,int,java.awt.image.ImageObserver);public#java.awt.Dimension#getMinimumSize();public#java.awt.Dimension#getPreferredSize();public#java.awt.Dimension#minimumSize();public#java.awt.Dimension#preferredSize();public#java.awt.FontMetrics#getFontMetrics(java.awt.Font);public#java.awt.Graphics#getGraphics();public#java.awt.GraphicsConfiguration#getGraphicsConfiguration();public#java.awt.Image#createImage(int,int);public#java.awt.Image#createImage(java.awt.image.ImageProducer);public#java.awt.Image#getBackBuffer();public#java.awt.Point#getLocationOnScreen();public#java.awt.Rectangle#getBounds();public#java.awt.Toolkit#getToolkit();public#java.awt.image.ColorModel#getColorModel();public#java.awt.image.VolatileImage#createVolatileImage(int,int);public#void#applyShape(sun.java2d.pipe.Region);public#void#coalescePaintEvent(java.awt.event.PaintEvent);public#void#createBuffers(int,java.awt.BufferCapabilities);public#void#destroyBuffers();public#void#disable();public#void#dispatchEvent(sun.awt.X11.XEvent);public#void#dispose();public#void#enable();public#void#flip(int,int,int,int,java.awt.BufferCapabilities$FlipContents);public#void#handleEvent(java.awt.AWTEvent);public#void#hide();public#void#layout();public#void#paint(java.awt.Graphics);public#void#print(java.awt.Graphics);public#void#repaint(long,int,int,int,int);public#void#reparent(java.awt.peer.ContainerPeer);public#void#reshape(int,int,int,int);public#void#setBackground(java.awt.Color);public#void#setBounds(int,int,int,int,int);public#void#setBoundsOperation(int);public#void#setEnabled(boolean);public#void#setFont(java.awt.Font);public#void#setForeground(java.awt.Color);public#void#setVisible(boolean);public#void#setZOrder(java.awt.peer.ComponentPeer);public#void#show();public#void#updateCursorImmediately()".equals(methods(c)), "methods");
        check("#long#handle;#sun.awt.X11.XEmbedChildProxy#proxy;#sun.awt.X11.XEmbeddingContainer#container".equals(fields(c)), "fields");
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
