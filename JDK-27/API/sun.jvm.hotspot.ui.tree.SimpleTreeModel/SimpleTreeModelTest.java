/*
 * @test
 * @summary sun.jvm.hotspot.ui.tree.SimpleTreeModel conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui.tree:+open
 * @run main SimpleTreeModelTest
 */
import java.lang.reflect.*;

public class SimpleTreeModelTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.tree.SimpleTreeModel", false, SimpleTreeModelTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "javax.swing.tree.TreeModel"), "interface javax.swing.tree.TreeModel");
        check(hasMethod(c, "getRoot"), "method getRoot");
        check(hasMethod(c, "isLeaf"), "method isLeaf");
        check(hasMethod(c, "getChildCount"), "method getChildCount");
        check(hasMethod(c, "getChild"), "method getChild");
        check(hasMethod(c, "valueForPathChanged"), "method valueForPathChanged");
        check(hasMethod(c, "addTreeModelListener"), "method addTreeModelListener");
        check(hasMethod(c, "removeTreeModelListener"), "method removeTreeModelListener");
        check(hasMethod(c, "setRoot"), "method setRoot");
        check(hasMethod(c, "getIndexOfChild"), "method getIndexOfChild");
        check(hasMethod(c, "fireTreeStructureChanged"), "method fireTreeStructureChanged");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
