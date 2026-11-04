/*
 * @test
 * @summary sun.awt.dnd.SunDropTargetContextPeer.EventDispatcher conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.dnd:+open
 * @run main EventDispatcherTest
 */

import java.util.*;
import java.lang.reflect.*;

public class EventDispatcherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.dnd.SunDropTargetContextPeer$EventDispatcher", false, EventDispatcherTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.dnd.SunDropTargetContextPeer$EventDispatcher".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#boolean#isDone();#int#getReturnValue();#void#dispatchEvent(sun.awt.dnd.SunDropTargetEvent);#void#registerEvent(sun.awt.dnd.SunDropTargetEvent);#void#setReturnValue(int);#void#unregisterEvent(sun.awt.dnd.SunDropTargetEvent);private#void#dispatchDropEvent(sun.awt.dnd.SunDropTargetEvent);private#void#dispatchEnterEvent(sun.awt.dnd.SunDropTargetEvent);private#void#dispatchExitEvent(sun.awt.dnd.SunDropTargetEvent);private#void#dispatchMotionEvent(sun.awt.dnd.SunDropTargetEvent);public#void#unregisterAllEvents()".equals(methods(c)), "methods");
        check("private final#[J#formats;private final#boolean#dispatchType;private final#int#actions;private final#int#dropAction;private final#java.util.HashSet#eventSet;private final#sun.awt.dnd.SunDropTargetContextPeer#peer;private#boolean#dispatcherDone;private#int#returnValue;private#long#nativeCtxt;static final#sun.awt.datatransfer.ToolkitThreadBlockedHandler#handler".equals(fields(c)), "fields");
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
