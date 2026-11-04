/*
 * @test
 * @summary javax.swing.tree.TreeNode conformance: type shape via reflection
 *          (headless-safe), interface completeness, and polymorphic use.
 * @run main TreeNodeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class TreeNodeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.TreeNode", false, TreeNodeTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getChildAt"), "method getChildAt");
        check(names.contains("getChildCount"), "method getChildCount");
        check(names.contains("getParent"), "method getParent");
        check(names.contains("getIndex"), "method getIndex");
        check(names.contains("getAllowsChildren"), "method getAllowsChildren");
        check(names.contains("isLeaf"), "method isLeaf");
        check(names.contains("children"), "method children");
        check(names.size() == 7, "exactly the 7 specified methods, got " + names.size() + ": " + names);
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "abstract method " + m.getName());
        }
        check(c.getMethod("children").getReturnType() == Enumeration.class, "children returns Enumeration");
        check(c.getMethod("getParent").getReturnType() == TreeNode.class, "getParent returns TreeNode");

        // Known implementations.
        check(hasInterface(Class.forName("javax.swing.tree.MutableTreeNode", false, TreeNodeTest.class.getClassLoader()),
                "javax.swing.tree.TreeNode"), "MutableTreeNode extends TreeNode");
        Class<?> impl = Class.forName("javax.swing.tree.DefaultMutableTreeNode", false, TreeNodeTest.class.getClassLoader());
        check(c.isAssignableFrom(impl), "DefaultMutableTreeNode is a TreeNode");

        // Polymorphic contract through the interface reference.
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("parent");
        DefaultMutableTreeNode kid = new DefaultMutableTreeNode("kid");
        node.add(kid);
        TreeNode t = node;
        check(t.getParent() == null, "getParent through interface");
        check(t.getChildAt(0) == kid, "getChildAt through interface");
        check(t.getChildCount() == 1, "getChildCount through interface");
        check(t.getIndex(kid) == 0, "getIndex through interface");
        check(t.getAllowsChildren(), "getAllowsChildren through interface");
        check(!t.isLeaf(), "isLeaf through interface");
        Enumeration<?> e = t.children();
        check(e.hasMoreElements() && e.nextElement() == kid, "children through interface");
        check(((TreeNode) kid).getParent() == node, "child parent through interface");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
