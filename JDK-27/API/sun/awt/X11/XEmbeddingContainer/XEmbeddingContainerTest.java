/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XEmbeddingContainer conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XEmbeddingContainerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XEmbeddingContainerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XEmbeddingContainer", false, XEmbeddingContainerTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XEmbeddingContainer".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("sun.awt.X11.XEmbedHelper".equals(c.getSuperclass().getName()), "superclass");
        check("sun.awt.X11.XEventDispatcher".equals(interfaces(c)), "interfaces");
        check("#boolean#checkXEmbed(long);#java.awt.Component#createChildProxy(long);#sun.awt.X11.XEmbedChildProxyPeer#getChild(long);#void#add(long);#void#childResized(java.awt.Component);#void#deinstall();#void#detachChild(long);#void#focusGained(long);#void#focusLost(long);#void#forwardKeyEvent(long,java.awt.event.KeyEvent);#void#install(sun.awt.X11.XWindow);#void#notifyChildEmbedded(long);public#void#dispatchEvent(sun.awt.X11.XEvent);public#void#handleClientMessage(sun.awt.X11.XEvent)".equals(methods(c)), "methods");
        check("#java.util.HashMap#children;#sun.awt.X11.XWindow#embedder".equals(fields(c)), "fields");
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
