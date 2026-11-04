/*
 * @test
 * @summary sun.awt.EmbeddedFrame conformance: reflection (no init, native/display-heavy).
 * @run main EmbeddedFrameTest
 */

import java.util.*;
import java.lang.reflect.*;

public class EmbeddedFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.EmbeddedFrame", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.EmbeddedFrame".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.awt.Frame".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.KeyEventDispatcher;java.beans.PropertyChangeListener".equals(interfaces(c)), "interfaces");
        check("private#void#addTraversingOutListeners(java.awt.KeyboardFocusManager);private#void#removeTraversingOutListeners(java.awt.KeyboardFocusManager);protected#boolean#traverseOut(boolean);protected#java.awt.Point#getLocationPrivate();protected#java.awt.Rectangle#getBoundsPrivate();protected#void#setBoundsPrivate(int,int,int,int);protected#void#setLocationPrivate(int,int);protected#void#setPeer(java.awt.peer.ComponentPeer);public abstract#void#registerAccelerator(java.awt.AWTKeyStroke);public abstract#void#unregisterAccelerator(java.awt.AWTKeyStroke);public static#java.applet.Applet#getAppletIfAncestorOf(java.awt.Component);public#boolean#dispatchKeyEvent(java.awt.event.KeyEvent);public#boolean#isCursorAllowed();public#boolean#isResizable();public#boolean#supportsXEmbed();public#boolean#traverseIn(boolean);public#java.awt.Container#getParent();public#java.awt.Cursor#getCursor();public#void#addNotify();public#void#hide();public#void#notifyModalBlocked(java.awt.Dialog,boolean);public#void#propertyChange(java.beans.PropertyChangeEvent);public#void#registerListeners();public#void#remove(java.awt.MenuComponent);public#void#setCursorAllowed(boolean);public#void#setIconImage(java.awt.Image);public#void#setIconImages(java.util.List);public#void#setMenuBar(java.awt.MenuBar);public#void#setResizable(boolean);public#void#setTitle(java.lang.String);public#void#show();public#void#synthesizeWindowActivation(boolean);public#void#toBack();public#void#toFront()".equals(methods(c)), "methods");
        check("private static final#long#serialVersionUID;private#boolean#isCursorAllowed;private#boolean#supportsXEmbed;private#java.awt.KeyboardFocusManager#appletKFM;protected static final#boolean#BACKWARD;protected static final#boolean#FORWARD".equals(fields(c)), "fields");
        check(c.getDeclaredConstructors().length == 5, "ctors");
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
