/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XLightweightFramePeer conformance: reflection (no init, native/display-heavy).
 * @run main XLightweightFramePeerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XLightweightFramePeerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XLightweightFramePeer", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XLightweightFramePeer".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.X11.XFramePeer".equals(c.getSuperclass().getName()), "superclass");
        check("sun.awt.OverrideNativeWindowHandle".equals(interfaces(c)), "interfaces");
        check("private#sun.awt.LightweightFrame#getLwTarget();protected#void#requestXFocus(long,boolean);public#java.awt.Graphics#getGraphics();public#long#getOverriddenWindowHandle();public#void#addDropTarget(java.awt.dnd.DropTarget);public#void#overrideWindowHandle(long);public#void#removeDropTarget(java.awt.dnd.DropTarget);public#void#setGrab(boolean);public#void#updateCursorImmediately();public#void#xSetVisible(boolean)".equals(methods(c)), "methods");
        check("private volatile#long#overriddenWindowHandle".equals(fields(c)), "fields");
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
