/*
 * @test
 * @summary javax.swing.tree.DefaultMutableTreeNode conformance: white-box structure + black-box behavior.
 * @run main DefaultMutableTreeNodeTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class DefaultMutableTreeNodeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.DefaultMutableTreeNode");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.lang.Cloneable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.tree.MutableTreeNode"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.io.Serializable"), "interface 2");
    }

    static List<String> names(Enumeration<? extends TreeNode> e) {
        List<String> out = new ArrayList<>();
        while (e.hasMoreElements()) {
            out.add(String.valueOf(e.nextElement()));
        }
        return out;
    }

    static List<String> list(String... s) {
        List<String> out = new ArrayList<>();
        for (String x : s) out.add(x);
        return out;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Constructors.
        DefaultMutableTreeNode empty = new DefaultMutableTreeNode();
        check(empty.getUserObject() == null, "no-arg user object null");
        check(empty.getAllowsChildren() == true, "no-arg allows children true");
        DefaultMutableTreeNode one = new DefaultMutableTreeNode("x");
        check("x".equals(one.getUserObject()), "user object ctor");
        check(one.getAllowsChildren() == true, "user object ctor allows children");
        DefaultMutableTreeNode leafOnly = new DefaultMutableTreeNode("leafOnly", false);
        check(leafOnly.getAllowsChildren() == false, "allows-children ctor false");
        check(!DefaultMutableTreeNode.EMPTY_ENUMERATION.hasMoreElements(), "EMPTY_ENUMERATION empty");

        // Build multi-level hierarchy: root -> (a -> (a1,a2)), b(leaf), c.
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode a = new DefaultMutableTreeNode("a");
        DefaultMutableTreeNode b = new DefaultMutableTreeNode("b", false);
        DefaultMutableTreeNode c = new DefaultMutableTreeNode("c");
        DefaultMutableTreeNode a1 = new DefaultMutableTreeNode("a1", false);
        DefaultMutableTreeNode a2 = new DefaultMutableTreeNode("a2", false);

        root.add(a);
        root.add(c);
        a.add(a1);
        a.add(a2);
        root.insert(b, 1); // root children become: a, b, c

        // Child/parent/index queries.
        check(root.getChildCount() == 3, "root child count 3");
        check(a.getChildCount() == 2, "a child count 2");
        check(b.getChildCount() == 0, "b child count 0");
        check(root.getChildAt(0) == a, "getChildAt 0 == a");
        check(root.getChildAt(1) == b, "getChildAt 1 == b");
        check(root.getChildAt(2) == c, "getChildAt 2 == c");
        check(root.getIndex(a) == 0, "index of a");
        check(root.getIndex(b) == 1, "index of b");
        check(root.getIndex(c) == 2, "index of c");
        check(root.getIndex(a1) == -1, "index of non-child is -1");
        check(a.getIndex(a1) == 0, "index of a1 under a");
        check(a.getParent() == root, "a parent");
        check(a1.getParent() == a, "a1 parent");

        // Levels / depths.
        check(root.getLevel() == 0, "root level 0");
        check(a.getLevel() == 1, "a level 1");
        check(a1.getLevel() == 2, "a1 level 2");
        check(b.getLevel() == 1, "b level 1");
        check(root.getDepth() == 2, "root depth 2");
        check(a.getDepth() == 1, "a depth 1");
        check(b.getDepth() == 0, "b depth 0");

        // Paths.
        TreeNode[] pathA2 = a2.getPath();
        check(pathA2.length == 3 && pathA2[0] == root && pathA2[1] == a && pathA2[2] == a2, "getPath a2");
        Object[] uoPath = a2.getUserObjectPath();
        check(uoPath.length == 3 && "root".equals(uoPath[0]) && "a".equals(uoPath[1]) && "a2".equals(uoPath[2]), "getUserObjectPath a2");

        // Root / isRoot.
        check(a1.getRoot() == root, "getRoot a1");
        check(root.isRoot() == true, "root isRoot");
        check(a1.isRoot() == false, "a1 not root");

        // Leaf queries.
        check(a1.isLeaf() == true, "a1 isLeaf");
        check(a.isLeaf() == false, "a not leaf");
        check(b.isLeaf() == true, "b isLeaf");

        // Ancestor / descendant / related / sibling.
        check(a1.isNodeAncestor(root) == true, "a1 isNodeAncestor root");
        check(root.isNodeAncestor(a1) == false, "root isNodeAncestor a1 false");
        check(root.isNodeDescendant(a1) == true, "root isNodeDescendant a1");
        check(a1.isNodeDescendant(root) == false, "a1 isNodeDescendant root false");
        check(a.isNodeDescendant(a) == true, "node descendant of itself");
        check(a1.getSharedAncestor(a2) == a, "shared ancestor a1,a2 == a");
        check(a1.getSharedAncestor(b) == root, "shared ancestor a1,b == root");
        check(a1.getSharedAncestor(a1) == a1, "shared ancestor self == self");
        check(a1.getSharedAncestor(null) == null, "shared ancestor null");
        check(a1.isNodeRelated(a2) == true, "related a1,a2");
        check(a1.isNodeRelated(new DefaultMutableTreeNode("other")) == false, "unrelated");

        check(a1.isNodeSibling(a2) == true, "siblings a1,a2");
        check(a.isNodeSibling(b) == true, "siblings a,b");
        check(a1.isNodeSibling(b) == false, "not siblings a1,b");
        check(a.getSiblingCount() == 3, "a sibling count 3");
        check(a1.getSiblingCount() == 2, "a1 sibling count 2");
        check(root.getSiblingCount() == 1, "root sibling count 1");
        check(a.getNextSibling() == b, "a next sibling b");
        check(b.getPreviousSibling() == a, "b previous sibling a");
        check(c.getNextSibling() == null, "c next sibling null");
        check(a.getPreviousSibling() == null, "a previous sibling null");

        // First/last child + childAfter/childBefore.
        check(root.getFirstChild() == a, "first child a");
        check(root.getLastChild() == c, "last child c");
        check(root.getChildAfter(a) == b, "childAfter a == b");
        check(root.getChildBefore(b) == a, "childBefore b == a");
        check(root.getChildBefore(a) == null, "childBefore a null");
        check(root.getChildAfter(c) == null, "childAfter c null");

        // Preorder neighbors.
        check(root.getNextNode() == a, "nextNode root == a");
        check(a.getNextNode() == a1, "nextNode a == a1");
        check(a2.getNextNode() == b, "nextNode a2 == b");
        check(c.getNextNode() == null, "nextNode c null");
        check(a.getPreviousNode() == root, "prevNode a == root");
        check(c.getPreviousNode() == b, "prevNode c == b");
        check(root.getPreviousNode() == null, "prevNode root null");

        // Leaf traversal.
        check(root.getFirstLeaf() == a1, "firstLeaf a1");
        check(root.getLastLeaf() == c, "lastLeaf c");
        check(root.getLeafCount() == 4, "leaf count 4");
        check(a2.getNextLeaf() == b, "nextLeaf a2 == b");
        check(b.getPreviousLeaf() == a2, "prevLeaf b == a2");

        // Enumerations (deterministic order).
        check(names(root.preorderEnumeration()).equals(list("root", "a", "a1", "a2", "b", "c")), "preorder");
        check(names(root.postorderEnumeration()).equals(list("a1", "a2", "a", "b", "c", "root")), "postorder");
        check(names(root.depthFirstEnumeration()).equals(list("a1", "a2", "a", "b", "c", "root")), "depthFirst == postorder");
        check(names(root.breadthFirstEnumeration()).equals(list("root", "a", "b", "c", "a1", "a2")), "breadthFirst");
        check(names(root.children()).equals(list("a", "b", "c")), "children() order");
        check(names(a2.pathFromAncestorEnumeration(root)).equals(list("root", "a", "a2")), "pathFromAncestor");

        // User object + toString round-trip.
        a2.setUserObject("renamed");
        check("renamed".equals(a2.getUserObject()), "setUserObject/getUserObject");
        check("renamed".equals(a2.toString()), "toString reflects user object");
        DefaultMutableTreeNode nullUO = new DefaultMutableTreeNode(null);
        check("".equals(nullUO.toString()), "toString empty for null user object");

        // clone(): shallow copy, no parent/children, same user object.
        DefaultMutableTreeNode cloneA = (DefaultMutableTreeNode) a.clone();
        check(cloneA != a, "clone distinct object");
        check(cloneA.getParent() == null, "clone parent null");
        check(cloneA.getChildCount() == 0, "clone child count 0");
        check(cloneA.getUserObject() == a.getUserObject(), "clone shares user object");

        // Raw setParent does not touch child arrays.
        DefaultMutableTreeNode orphan = new DefaultMutableTreeNode("orphan");
        orphan.setParent(root);
        check(orphan.getParent() == root, "setParent sets parent");
        orphan.setParent(null);

        // Removal / re-parenting on a separate subtree.
        DefaultMutableTreeNode r2 = new DefaultMutableTreeNode("r2");
        DefaultMutableTreeNode x = new DefaultMutableTreeNode("x");
        DefaultMutableTreeNode y = new DefaultMutableTreeNode("y");
        DefaultMutableTreeNode z = new DefaultMutableTreeNode("z");
        r2.add(x);
        r2.add(y);
        r2.add(z);
        check(r2.getChildCount() == 3, "r2 child count 3");
        r2.remove(1); // removes y
        check(r2.getChildCount() == 2 && r2.getChildAt(0) == x && r2.getChildAt(1) == z, "remove(int)");
        check(y.getParent() == null, "removed child parent null");
        z.removeFromParent();
        check(z.getParent() == null, "removeFromParent parent null");
        check(r2.getChildCount() == 1, "r2 child count after removeFromParent");
        DefaultMutableTreeNode w = new DefaultMutableTreeNode("w");
        r2.add(w);
        check(r2.getChildAt(1) == w, "add appends");
        r2.insert(z, 0); // moves z (currently parentless) to front
        check(r2.getChildAt(0) == z && r2.getChildAt(1) == x && r2.getChildAt(2) == w, "insert at index");
        // insert re-parents a node already owned elsewhere
        DefaultMutableTreeNode r3 = new DefaultMutableTreeNode("r3");
        DefaultMutableTreeNode mover = new DefaultMutableTreeNode("mover");
        r2.add(mover);       // mover under r2 (index 3)
        r3.insert(mover, 0); // move mover to r3
        check(r3.getChildAt(0) == mover && mover.getParent() == r3, "insert moves from old parent");
        check(r2.getChildCount() == 3, "old parent lost moved child");
        r2.remove(x);
        check(r2.getChildCount() == 2, "remove(MutableTreeNode)");
        r2.removeAllChildren();
        check(r2.getChildCount() == 0, "removeAllChildren");
        check(z.getParent() == null && w.getParent() == null, "removeAllChildren clears parents");

        // setAllowsChildren(false) drops children.
        DefaultMutableTreeNode parent2 = new DefaultMutableTreeNode("p2");
        parent2.add(new DefaultMutableTreeNode("k1"));
        parent2.add(new DefaultMutableTreeNode("k2"));
        check(parent2.getChildCount() == 2, "parent2 has 2 children");
        parent2.setAllowsChildren(false);
        check(parent2.getAllowsChildren() == false, "setAllowsChildren false");
        check(parent2.getChildCount() == 0, "setAllowsChildren(false) removed children");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
