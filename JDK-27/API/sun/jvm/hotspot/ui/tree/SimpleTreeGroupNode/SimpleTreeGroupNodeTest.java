/*
 * @test
 * @summary sun.jvm.hotspot.ui.tree.SimpleTreeGroupNode conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui.tree:+open
 * @run main SimpleTreeGroupNodeTest
 */
import java.lang.reflect.*;

public class SimpleTreeGroupNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.tree.SimpleTreeGroupNode", false, SimpleTreeGroupNodeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.ui.tree.SimpleTreeNode"), "interface sun.jvm.hotspot.ui.tree.SimpleTreeNode");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "isLeaf"), "method isLeaf");
        check(hasMethod(c, "getChildCount"), "method getChildCount");
        check(hasMethod(c, "getChild"), "method getChild");
        check(hasMethod(c, "getIndexOfChild"), "method getIndexOfChild");
        check(hasMethod(c, "addChild"), "method addChild");
        check(hasMethod(c, "removeChild"), "method removeChild");
        check(hasMethod(c, "removeAllChildren"), "method removeAllChildren");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
