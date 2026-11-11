/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XButtonPeer conformance: reflection (no init, native/display-heavy).
 * @run main XButtonPeerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XButtonPeerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XButtonPeer", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XButtonPeer".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.X11.XComponentPeer".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.peer.ButtonPeer".equals(interfaces(c)), "interfaces");
        check("#void#handleJavaKeyEvent(java.awt.event.KeyEvent);#void#handleJavaMouseEvent(java.awt.event.MouseEvent);#void#paintPeer(java.awt.Graphics);#void#preInit(sun.awt.X11.XCreateWindowParams);protected#java.lang.String#getPropertyPrefix();protected#void#paintButtonPressed(java.awt.Graphics,java.awt.Component);protected#void#paintFocus(java.awt.Graphics,int,int,int,int);protected#void#paintText(java.awt.Graphics,java.awt.Component,java.awt.Rectangle,java.lang.String);public#boolean#isFocusable();public#java.awt.Dimension#getMinimumSize();public#java.awt.Dimension#minimumSize();public#void#action(long,int);public#void#dispose();public#void#focusGained(java.awt.event.FocusEvent);public#void#focusLost(java.awt.event.FocusEvent);public#void#paintBorder(java.awt.Graphics,int,int,int,int);public#void#setBackground(java.awt.Color);public#void#setLabel(java.lang.String)".equals(methods(c)), "methods");
        check("#java.lang.String#label;private static final#java.lang.String#propertyPrefix;private#boolean#armed;private#boolean#disposed;private#boolean#pressed;private#java.awt.Insets#borderInsets;private#java.awt.Insets#contentAreaInsets;private#java.awt.Insets#focusInsets;protected#java.awt.Color#focusColor".equals(fields(c)), "fields");
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
