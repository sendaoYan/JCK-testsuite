/*
 * @test
 * @summary sun.awt.AWTAccessor.ComponentAccessor conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main ComponentAccessorTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ComponentAccessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.AWTAccessor$ComponentAccessor", false, ComponentAccessorTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.AWTAccessor$ComponentAccessor".equals(c.getName()), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public abstract#boolean#canBeFocusOwner(java.awt.Component);public abstract#boolean#getBackgroundEraseDisabled(java.awt.Component);public abstract#boolean#getIgnoreRepaint(java.awt.Component);public abstract#boolean#isDisplayable(java.awt.Component);public abstract#boolean#isEnabled(java.awt.Component);public abstract#boolean#isLightweight(java.awt.Component);public abstract#boolean#isVisible(java.awt.Component);public abstract#int#getHeight(java.awt.Component);public abstract#int#getWidth(java.awt.Component);public abstract#int#getX(java.awt.Component);public abstract#int#getY(java.awt.Component);public abstract#java.awt.Color#getBackground(java.awt.Component);public abstract#java.awt.Color#getForeground(java.awt.Component);public abstract#java.awt.Container#getParent(java.awt.Component);public abstract#java.awt.Cursor#getCursor(java.awt.Component);public abstract#java.awt.Font#getFont(java.awt.Component);public abstract#java.awt.Point#getLocation(java.awt.Component);public abstract#java.awt.Rectangle#getBounds(java.awt.Component);public abstract#java.awt.image.BufferStrategy#getBufferStrategy(java.awt.Component);public abstract#java.awt.peer.ComponentPeer#getPeer(java.awt.Component);public abstract#void#createBufferStrategy(java.awt.Component,int,java.awt.BufferCapabilities);public abstract#void#processEvent(java.awt.Component,java.awt.AWTEvent);public abstract#void#requestFocus(java.awt.Component,java.awt.event.FocusEvent$Cause);public abstract#void#revalidateSynchronously(java.awt.Component);public abstract#void#setBackground(java.awt.Component,java.awt.Color);public abstract#void#setBackgroundEraseDisabled(java.awt.Component,boolean);public abstract#void#setGraphicsConfiguration(java.awt.Component,java.awt.GraphicsConfiguration);public abstract#void#setLocation(java.awt.Component,int,int);public abstract#void#setParent(java.awt.Component,java.awt.Container);public abstract#void#setPeer(java.awt.Component,java.awt.peer.ComponentPeer);public abstract#void#setRequestFocusController(sun.awt.RequestFocusController);public abstract#void#setSize(java.awt.Component,int,int)".equals(methods(c)), "methods");
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
