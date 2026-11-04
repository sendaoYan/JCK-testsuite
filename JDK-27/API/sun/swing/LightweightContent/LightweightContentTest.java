/*
 * @test
 * @summary sun.swing.LightweightContent conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.swing:+open
 * @run main LightweightContentTest
 */

import java.util.*;
import java.lang.reflect.*;

public class LightweightContentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.LightweightContent", false, LightweightContentTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.swing.LightweightContent".equals(c.getName()), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public abstract#javax.swing.JComponent#getComponent();public abstract#void#focusGrabbed();public abstract#void#focusUngrabbed();public abstract#void#imageReshaped(int,int,int,int);public abstract#void#imageUpdated(int,int,int,int);public abstract#void#maximumSizeChanged(int,int);public abstract#void#minimumSizeChanged(int,int);public abstract#void#paintLock();public abstract#void#paintUnlock();public abstract#void#preferredSizeChanged(int,int);public#java.awt.dnd.DragGestureRecognizer#createDragGestureRecognizer(java.lang.Class,java.awt.dnd.DragSource,java.awt.Component,int,java.awt.dnd.DragGestureListener);public#java.awt.dnd.peer.DragSourceContextPeer#createDragSourceContextPeer(java.awt.dnd.DragGestureEvent);public#void#addDropTarget(java.awt.dnd.DropTarget);public#void#imageBufferReset([I,int,int,int,int,int);public#void#imageBufferReset([I,int,int,int,int,int,double,double);public#void#imageBufferReset([I,int,int,int,int,int,int);public#void#removeDropTarget(java.awt.dnd.DropTarget);public#void#setCursor(java.awt.Cursor)".equals(methods(c)), "methods");
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
