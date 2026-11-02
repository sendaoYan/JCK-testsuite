/*
 * @test
 * @summary sun.awt.LightweightFrame conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main LightweightFrameTest
 */

import java.util.*;
import java.lang.reflect.*;

public class LightweightFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.LightweightFrame", false, LightweightFrameTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.LightweightFrame".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.awt.Frame".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#void#setPeer(java.awt.peer.FramePeer);public abstract#double#getScaleFactorX();public abstract#double#getScaleFactorY();public abstract#int#getScaleFactor();public abstract#java.awt.dnd.DragGestureRecognizer#createDragGestureRecognizer(java.lang.Class,java.awt.dnd.DragSource,java.awt.Component,int,java.awt.dnd.DragGestureListener);public abstract#java.awt.dnd.peer.DragSourceContextPeer#createDragSourceContextPeer(java.awt.dnd.DragGestureEvent);public abstract#void#addDropTarget(java.awt.dnd.DropTarget);public abstract#void#grabFocus();public abstract#void#notifyDisplayChanged(double,double);public abstract#void#notifyDisplayChanged(int);public abstract#void#removeDropTarget(java.awt.dnd.DropTarget);public abstract#void#ungrabFocus();public final#boolean#isResizable();public final#java.awt.Container#getParent();public final#void#remove(java.awt.MenuComponent);public final#void#setIconImage(java.awt.Image);public final#void#setIconImages(java.util.List);public final#void#setMenuBar(java.awt.MenuBar);public final#void#setResizable(boolean);public final#void#setTitle(java.lang.String);public final#void#toBack();public final#void#toFront();public#java.awt.Graphics#getGraphics();public#java.awt.Rectangle#getHostBounds();public#void#addNotify();public#void#emulateActivation(boolean);public#void#setHostBounds(int,int,int,int)".equals(methods(c)), "methods");
        check("private#int#hostH;private#int#hostW;private#int#hostX;private#int#hostY".equals(fields(c)), "fields");
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
