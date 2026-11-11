/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XEmbedCanvasPeer conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XEmbedCanvasPeerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XEmbedCanvasPeerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XEmbedCanvasPeer", false, XEmbedCanvasPeerTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XEmbedCanvasPeer".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("sun.awt.X11.XCanvasPeer".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.KeyEventPostProcessor;java.awt.event.WindowFocusListener;sun.awt.ModalityListener;sun.awt.WindowIDProvider".equals(interfaces(c)), "interfaces");
        check("#boolean#isApplicationActive();#boolean#isXEmbedActive();#java.awt.Rectangle#getClientBounds();#java.awt.Window#getTopLevel(java.awt.Component);#void#canvasFocusGained(java.awt.event.FocusEvent);#void#canvasFocusLost(java.awt.event.FocusEvent);#void#childDestroyed();#void#childResized();#void#deinstallAcceleratorListener();#void#deinstallActivateListener();#void#deinstallModalityListener();#void#detachChild();#void#embedChild(long);#void#endDispatching();#void#focusNext();#void#focusPrev();#void#forwardKeyEvent(java.awt.event.KeyEvent);#void#grabKey(long,long);#void#initDispatching();#void#installAcceleratorListener();#void#installActivateListener();#void#installModalityListener();#void#notifyChildEmbedded();#void#propogateRegisterAccelerator(java.awt.AWTKeyStroke);#void#propogateUnRegisterAccelerator(java.awt.AWTKeyStroke);#void#registerAccelerator(long,long,long);#void#requestXEmbedFocus();#void#ungrabKey(long,long);#void#unregisterAccelerator(long);protected#void#postInit(sun.awt.X11.XCreateWindowParams);protected#void#preInit(sun.awt.X11.XCreateWindowParams);public#boolean#isFocusable();public#boolean#postProcessKeyEvent(java.awt.event.KeyEvent);public#boolean#processXEmbedDnDEvent(long,int);public#java.awt.Dimension#getMinimumSize();public#java.awt.Dimension#getPreferredSize();public#void#dispatchEvent(sun.awt.X11.XEvent);public#void#dispose();public#void#handleClientMessage(sun.awt.X11.XEvent);public#void#handleEvent(java.awt.AWTEvent);public#void#modalityPopped(sun.awt.ModalityEvent);public#void#modalityPushed(sun.awt.ModalityEvent);public#void#removeXEmbedDropTarget();public#void#setXEmbedDropTarget();public#void#windowGainedFocus(java.awt.event.WindowEvent);public#void#windowLostFocus(java.awt.event.WindowEvent);static#[B#getBData(java.awt.event.KeyEvent)".equals(methods(c)), "methods");
        check("#boolean#applicationActive;#java.lang.Object#ACCEL_LOCK;#java.lang.Object#GRAB_LOCK;#java.util.Map#accel_lookup;#java.util.Map#accelerators;#java.util.Set#grabbed_keys;#sun.awt.X11.XEmbedCanvasPeer$XEmbedServer#xembed;private static final#sun.util.logging.PlatformLogger#xembedLog".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 3, "ctors");
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
