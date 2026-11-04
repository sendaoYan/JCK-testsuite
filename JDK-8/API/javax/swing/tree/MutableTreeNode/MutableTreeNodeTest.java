/*
 * @test
 * @summary javax.swing.tree.MutableTreeNode conformance: type shape via reflection
 *          (headless-safe), interface completeness, and polymorphic use.
 * @run main MutableTreeNodeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class MutableTreeNodeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Recording implementation used to exercise the interface reference.
    static class RecordingNode implements MutableTreeNode {
        MutableTreeNode parent, inserted;
        int insertIndex = -1, removedIndex = -1;
        MutableTreeNode removedNode;
        Object userObject;
        int detachCalls;
        public void insert(MutableTreeNode child, int index) { inserted = child; insertIndex = index; }
        public void remove(int index) { removedIndex = index; }
        public void remove(MutableTreeNode node) { removedNode = node; }
        public void setUserObject(Object object) { userObject = object; }
        public void removeFromParent() { detachCalls++; }
        public void setParent(MutableTreeNode newParent) { parent = newParent; }
        public TreeNode getChildAt(int childIndex) { return null; }
        public int getChildCount() { return 0; }
        public TreeNode getParent() { return parent; }
        public int getIndex(TreeNode node) { return -1; }
        public boolean getAllowsChildren() { return true; }
        public boolean isLeaf() { return true; }
        public Enumeration<? extends TreeNode> children() { return java.util.Collections.emptyEnumeration(); }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.MutableTreeNode", false, MutableTreeNodeTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "javax.swing.tree.TreeNode"), "implements javax.swing.tree.TreeNode");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("insert"), "method insert");
        check(names.contains("remove"), "method remove");
        check(names.contains("remove"), "method remove");
        check(names.contains("setUserObject"), "method setUserObject");
        check(names.contains("removeFromParent"), "method removeFromParent");
        check(names.contains("setParent"), "method setParent");
        check(names.size() == 12, "7 TreeNode names + 5 own names (both remove overloads share one name), got "
                + names.size() + ": " + names);
        check(c.getDeclaredMethods().length == 6, "exactly 6 declared methods");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "abstract method " + m.getName());
        }
        // Overloads: remove(int) and remove(MutableTreeNode).
        check(c.getMethod("remove", int.class) != null, "remove(int) overload");
        check(c.getMethod("remove", MutableTreeNode.class) != null, "remove(MutableTreeNode) overload");

        // Known implementations.
        Class<?> impl = Class.forName("javax.swing.tree.DefaultMutableTreeNode", false, MutableTreeNodeTest.class.getClassLoader());
        check(c.isAssignableFrom(impl), "DefaultMutableTreeNode is a MutableTreeNode");

        // Polymorphic contract through the interface reference.
        MutableTreeNode m = new RecordingNode();
        MutableTreeNode other = new RecordingNode();
        m.insert(other, 2);
        check(((RecordingNode) m).inserted == other && ((RecordingNode) m).insertIndex == 2, "insert through interface");
        m.remove(1);
        check(((RecordingNode) m).removedIndex == 1, "remove(int) through interface");
        m.remove(other);
        check(((RecordingNode) m).removedNode == other, "remove(node) through interface");
        m.setUserObject("u");
        check("u".equals(((RecordingNode) m).userObject), "setUserObject through interface");
        m.setParent(other);
        check(((RecordingNode) m).getParent() == other, "setParent/getParent through interface");
        m.removeFromParent();
        check(((RecordingNode) m).detachCalls == 1, "removeFromParent through interface");

        // DefaultMutableTreeNode honours the same contract through the interface type.
        MutableTreeNode dmtn = new DefaultMutableTreeNode("p");
        dmtn.setUserObject("q");
        check("q".equals(((DefaultMutableTreeNode) dmtn).getUserObject()), "DefaultMutableTreeNode via interface");
        MutableTreeNode kid = new DefaultMutableTreeNode("k");
        dmtn.insert(kid, 0);
        check(kid.getParent() == dmtn, "insert rewires parent via interface");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
