/*
 * @test
 * @summary javax.swing.tree.MutableTreeNode conformance: white-box structure + black-box behavior.
 * @run main MutableTreeNodeTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class MutableTreeNodeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.MutableTreeNode");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.tree.TreeNode"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final List<MutableTreeNode> kids = new ArrayList<>();
        final MutableTreeNode[] parentRef = new MutableTreeNode[1];
        final Object[] userObject = new Object[1];

        final MutableTreeNode node = new MutableTreeNode() {
            public TreeNode getChildAt(int i) { return kids.get(i); }
            public int getChildCount() { return kids.size(); }
            public TreeNode getParent() { return parentRef[0]; }
            public int getIndex(TreeNode n) { return kids.indexOf(n); }
            public boolean getAllowsChildren() { return true; }
            public boolean isLeaf() { return kids.isEmpty(); }
            public Enumeration<TreeNode> children() {
                return Collections.enumeration(new ArrayList<TreeNode>(kids));
            }
            public void insert(MutableTreeNode c, int i) { kids.add(i, c); }
            public void remove(int i) { kids.remove(i); }
            public void remove(MutableTreeNode c) { kids.remove(c); }
            public void setUserObject(Object o) { userObject[0] = o; }
            public void removeFromParent() {
                MutableTreeNode p = (MutableTreeNode) getParent();
                if (p != null) p.remove(this);
                parentRef[0] = null;
            }
            public void setParent(MutableTreeNode p) { parentRef[0] = p; }
        };

        // Exercise TreeNode methods via MutableTreeNode.
        check(node.getAllowsChildren() == true, "getAllowsChildren");
        check(node.isLeaf() == true, "initially leaf");
        check(node.getParent() == null, "initially no parent");

        // insert + queries.
        MutableTreeNode childA = new DefaultMutableTreeNode("a");
        MutableTreeNode childB = new DefaultMutableTreeNode("b");
        node.insert(childA, 0);
        node.insert(childB, 1);
        check(node.getChildCount() == 2, "getChildCount 2");
        check(node.getChildAt(0) == childA && node.getChildAt(1) == childB, "getChildAt");
        check(node.getIndex(childB) == 1, "getIndex childB");
        check(node.isLeaf() == false, "no longer leaf");
        Enumeration<? extends TreeNode> kidsEnum = node.children();
        check(kidsEnum.hasMoreElements() && kidsEnum.nextElement() == childA, "children() order");

        // setUserObject.
        node.setUserObject("payload");
        check("payload".equals(userObject[0]), "setUserObject stores value");

        // setParent + removeFromParent.
        DefaultMutableTreeNode parentNode = new DefaultMutableTreeNode("parent");
        node.setParent(parentNode);
        check(node.getParent() == parentNode, "setParent/getParent");
        node.setParent(null);
        check(node.getParent() == null, "setParent(null)");
        parentNode.add(node);
        check(parentNode.getChildCount() == 1, "parent holds node");
        check(node.getParent() == parentNode, "add sets parent");
        node.removeFromParent();
        check(parentNode.getChildCount() == 0, "removeFromParent removed from parent");
        check(node.getParent() == null, "removeFromParent clears parent");

        // remove(int) and remove(MutableTreeNode).
        node.remove(0); // removes childA
        check(node.getChildCount() == 1 && node.getChildAt(0) == childB, "remove(int)");
        node.remove(childB);
        check(node.getChildCount() == 0, "remove(MutableTreeNode)");
        check(node.isLeaf() == true, "back to leaf");

        check(MutableTreeNode.class.isAssignableFrom(DefaultMutableTreeNode.class), "DefaultMutableTreeNode implements MutableTreeNode");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
