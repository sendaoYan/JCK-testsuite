/*
 * @test
 * @summary com.sun.corba.se.impl.orbutil.graph.GraphImpl conformance: type shape via reflection (headless-safe).
 * @run main GraphImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class GraphImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.orbutil.graph.GraphImpl", false, GraphImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.AbstractSet"), "superclass java.util.AbstractSet");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.orbutil.graph.Graph"), "interface com.sun.corba.se.impl.orbutil.graph.Graph");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 5, "public declared method count");
        check(mn.contains("add"), "method add");
        check(mn.contains("getNodeData"), "method getNodeData");
        check(mn.contains("getRoots"), "method getRoots");
        check(mn.contains("iterator"), "method iterator");
        check(mn.contains("size"), "method size");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
