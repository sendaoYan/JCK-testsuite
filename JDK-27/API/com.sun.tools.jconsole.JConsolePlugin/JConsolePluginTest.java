/*
 * @test
 * @summary com.sun.tools.jconsole.JConsolePlugin conformance: plugin lifecycle.
 * @run main JConsolePluginTest
 */

import java.lang.reflect.*;
import java.util.*;

public class JConsolePluginTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    public static void main(String[] a) throws Exception {
        Class<?> c = load("com.sun.tools.jconsole.JConsolePlugin");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        Set<String> m = methodNames(c);
        check(m.contains("setContext"), "setContext");
        check(m.contains("getContext"), "getContext");
        check(m.contains("getTabs"), "getTabs");
        check(m.contains("newSwingWorker"), "newSwingWorker");
        check(m.contains("dispose"), "dispose");
        com.sun.tools.jconsole.JConsolePlugin p = new com.sun.tools.jconsole.JConsolePlugin() {
            public java.util.Map<String,javax.swing.JPanel> getTabs() { return new java.util.HashMap<>(); }
            public javax.swing.SwingWorker<?,?> newSwingWorker() { return null; }
        };
        check(p.getContext() == null, "getContext null");
        p.dispose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
