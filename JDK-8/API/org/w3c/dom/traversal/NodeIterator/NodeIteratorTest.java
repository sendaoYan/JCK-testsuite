/*
 * @test
 * @summary org.w3c.dom.traversal.NodeIterator conformance: reflection shape checks.
 * @run main NodeIteratorTest
 */

import java.lang.reflect.*;

public class NodeIteratorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.traversal.NodeIterator");
        check(c != null, "org.w3c.dom.traversal.NodeIterator loads");
        check(c.getName().equals("org.w3c.dom.traversal.NodeIterator"), "binary name is org.w3c.dom.traversal.NodeIterator");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.traversal.NodeIterator is public");
        check(c.isInterface(), "org.w3c.dom.traversal.NodeIterator is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getRoot"), "declares getRoot");
        check(declares(c, "getWhatToShow"), "declares getWhatToShow");
        check(declares(c, "getFilter"), "declares getFilter");
        check(declares(c, "getExpandEntityReferences"), "declares getExpandEntityReferences");
        check(declares(c, "nextNode"), "declares nextNode");
        check(declares(c, "previousNode"), "declares previousNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
