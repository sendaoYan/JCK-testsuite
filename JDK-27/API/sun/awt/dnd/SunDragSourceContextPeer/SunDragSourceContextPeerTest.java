/*
 * @test
 * @summary sun.awt.dnd.SunDragSourceContextPeer conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.dnd:+open
 * @run main SunDragSourceContextPeerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SunDragSourceContextPeerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.dnd.SunDragSourceContextPeer", false, SunDragSourceContextPeerTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.dnd.SunDragSourceContextPeer".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.dnd.peer.DragSourceContextPeer".equals(interfaces(c)), "interfaces");
        check("private static#java.lang.String#getExceptionMessage(boolean);private#void#cleanup();private#void#dragMotion(int,int,int,int);private#void#dragMouseMoved(int,int,int,int);private#void#operationChanged(int,int,int,int);protected abstract#void#setNativeCursor(long,java.awt.Cursor,int);protected abstract#void#startDrag(java.awt.datatransfer.Transferable,[J,java.util.Map);protected final#void#dragDropFinished(boolean,int,int,int);protected final#void#dragExit(int,int);protected final#void#postDragSourceDragEvent(int,int,int,int,int);protected synchronized#long#getNativeContext();protected synchronized#void#setNativeContext(long);protected synchronized#void#setTrigger(java.awt.dnd.DragGestureEvent);protected#java.awt.Component#getComponent();protected#java.awt.dnd.DragGestureEvent#getTrigger();protected#java.awt.dnd.DragSourceContext#getDragSourceContext();protected#void#dragEnter(int,int,int,int);public static#boolean#checkEvent(java.awt.AWTEvent);public static#boolean#isDragDropInProgress();public static#int#convertModifiersToDropAction(int,int);public static#void#checkDragDropInProgress();public static#void#setDragDropInProgress(boolean);public#java.awt.Cursor#getCursor();public#java.awt.Image#getDragImage();public#java.awt.Point#getDragImageOffset();public#void#quitSecondaryEventLoop();public#void#setCursor(java.awt.Cursor);public#void#startDrag(java.awt.dnd.DragSourceContext,java.awt.Cursor,java.awt.Image,java.awt.Point);public#void#startSecondaryEventLoop();public#void#transferablesFlavorsChanged()".equals(methods(c)), "methods");
        check("private static volatile#boolean#discardingMouseEvents;private static volatile#boolean#dragDropInProgress;private#int#sourceActions;private#java.awt.Component#component;private#java.awt.Cursor#cursor;private#java.awt.Image#dragImage;private#java.awt.Point#dragImageOffset;private#java.awt.dnd.DragGestureEvent#trigger;private#java.awt.dnd.DragSourceContext#dragSourceContext;private#long#nativeCtxt;protected static final#int#DISPATCH_CHANGED;protected static final#int#DISPATCH_ENTER;protected static final#int#DISPATCH_EXIT;protected static final#int#DISPATCH_FINISH;protected static final#int#DISPATCH_MOTION;protected static final#int#DISPATCH_MOUSE_MOVED".equals(fields(c)), "fields");
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
