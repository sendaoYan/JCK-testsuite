/*
 * @test
 * @summary javax.swing.tree.DefaultMutableTreeNode conformance: tree nodes (headless-safe):
 *          add/insert/remove/removeAllChildren, ancestors/descendants, traversals
 *          (pre/post/breadth/depth/pathFromAncestor), siblings/leaves, paths and levels,
 *          null/out-of-range exception matrix, clone/serialization semantics.
 * @run main DefaultMutableTreeNodeTest
 */

import java.util.Enumeration;
import java.util.NoSuchElementException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class DefaultMutableTreeNodeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Exception; }

    static void checkThrows(Class<? extends Throwable> ex, String m, ThrowingRunnable r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + ": expected " + ex.getName() + " but got " + t.getClass().getName() + ": " + t.getMessage());
        }
        throw new AssertionError(m + ": expected " + ex.getName() + " but no exception");
    }

    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object r = ois.readObject();
        ois.close();
        return r;
    }

    static String enumStr(Enumeration<?> e) {
        StringBuilder s = new StringBuilder();
        while (e.hasMoreElements()) s.append(e.nextElement()).append(",");
        return s.toString();
    }

    /** Tree used by traversal tests: root -> {c1 -> g1, c2}. */
    static DefaultMutableTreeNode sample() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("c1");
        DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("c2");
        root.add(c1);
        root.add(c2);
        c1.add(new DefaultMutableTreeNode("g1"));
        return root;
    }

    static void testBasics() {
        DefaultMutableTreeNode n = new DefaultMutableTreeNode("root");
        check(n.isRoot() && n.getChildCount() == 0, "root");
        check("root".equals(n.getUserObject()), "user object");
        check(n.getParent() == null, "root parent null");
        check(n.getAllowsChildren(), "allows children by default");
        check(n.isLeaf(), "childless node is leaf");
        check(new DefaultMutableTreeNode().getUserObject() == null, "null user object default");
        check("".equals(new DefaultMutableTreeNode().toString()), "null user object toString is empty string");
        check("root".equals(n.toString()), "toString delegates to user object");
        DefaultMutableTreeNode noKids = new DefaultMutableTreeNode("k", false);
        check(!noKids.getAllowsChildren(), "allowsChildren=false ctor");
        check(noKids.isLeaf(), "disallows children means leaf");

        DefaultMutableTreeNode child = new DefaultMutableTreeNode("child");
        n.add(child);
        check(n.getChildCount() == 1, "child");
        check(n.getChildAt(0) == child, "getChildAt");
        check(child.getParent() == n, "parent set by add");
        check(n.getIndex(child) == 0, "getIndex");
        check(!n.isLeaf() && child.isLeaf(), "leaf-ness after add");
        check(n.getFirstChild() == child && n.getLastChild() == child, "first/last child");

        // insert at explicit positions, including the childCount boundary.
        DefaultMutableTreeNode b = new DefaultMutableTreeNode("b");
        n.insert(b, 0);
        check(n.getChildAt(0) == b && n.getChildAt(1) == child, "insert at 0 shifts");
        DefaultMutableTreeNode c = new DefaultMutableTreeNode("c");
        n.insert(c, n.getChildCount());
        check(n.getChildAt(2) == c, "insert at childCount boundary");
        check(b.getParent() == n && c.getParent() == n, "insert rewires parent");

        // remove(int), remove(MutableTreeNode), removeAllChildren.
        n.remove(0);
        check(n.getChildCount() == 2 && n.getChildAt(0) == child, "remove(int) shifts");
        check(b.getParent() == null, "remove(int) detaches parent");
        n.remove(child);
        check(n.getChildCount() == 1 && child.getParent() == null, "remove(node)");
        n.add(b);
        n.removeAllChildren();
        check(n.getChildCount() == 0 && b.getParent() == null, "removeAllChildren detaches all");
        child.removeFromParent();
        check(true, "removeFromParent on detached node is harmless");

        // setUserObject.
        n.setUserObject(Integer.valueOf(5));
        check(Integer.valueOf(5).equals(n.getUserObject()), "setUserObject");
        check("5".equals(n.toString()), "toString follows new user object");
    }

    static void testExceptionMatrix() {
        final DefaultMutableTreeNode n = new DefaultMutableTreeNode("n");
        final DefaultMutableTreeNode child = new DefaultMutableTreeNode("c");
        checkThrows(IllegalArgumentException.class, "insert null child",
                new ThrowingRunnable() { public void run() { n.insert(null, 0); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "insert index too big",
                new ThrowingRunnable() { public void run() { n.insert(new DefaultMutableTreeNode("x"), 1); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "insert negative index",
                new ThrowingRunnable() { public void run() { n.insert(new DefaultMutableTreeNode("x"), -1); } });
        checkThrows(IllegalArgumentException.class, "remove null node",
                new ThrowingRunnable() { public void run() { n.remove((javax.swing.tree.MutableTreeNode) null); } });
        checkThrows(IllegalArgumentException.class, "remove non-child node",
                new ThrowingRunnable() { public void run() { n.remove(child); } });
        checkThrows(IllegalArgumentException.class, "getIndex null",
                new ThrowingRunnable() { public void run() { n.getIndex((TreeNode) null); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "getChildAt beyond children",
                new ThrowingRunnable() { public void run() { n.getChildAt(0); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "remove(int) beyond children",
                new ThrowingRunnable() { public void run() { n.remove(0); } });
        checkThrows(IllegalArgumentException.class, "pathFromAncestor null",
                new ThrowingRunnable() { public void run() { n.pathFromAncestorEnumeration(null); } });
        checkThrows(IllegalArgumentException.class, "pathFromAncestor non-ancestor",
                new ThrowingRunnable() { public void run() { child.pathFromAncestorEnumeration(n); } });
        checkThrows(IllegalArgumentException.class, "getChildAfter null",
                new ThrowingRunnable() { public void run() { n.getChildAfter(null); } });
        final DefaultMutableTreeNode noKids = new DefaultMutableTreeNode("x", false);
        checkThrows(IllegalStateException.class, "insert when children disallowed",
                new ThrowingRunnable() { public void run() { noKids.insert(new DefaultMutableTreeNode("y"), 0); } });
        // setAllowsChildren(false) drops existing children.
        DefaultMutableTreeNode x = new DefaultMutableTreeNode("x");
        x.add(new DefaultMutableTreeNode("y"));
        x.setAllowsChildren(false);
        check(x.getChildCount() == 0, "setAllowsChildren(false) drops children");
        check(!x.getAllowsChildren(), "allowsChildren false after set");
        // Cycle prevention: inserting an ancestor below its descendant.
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("r");
        final DefaultMutableTreeNode mid = new DefaultMutableTreeNode("m");
        root.add(mid);
        checkThrows(IllegalArgumentException.class, "insert own ancestor creates cycle",
                new ThrowingRunnable() { public void run() { mid.insert(root, 0); } });
        // null-tolerant predicates.
        check(!n.isNodeChild(null), "isNodeChild(null) is false");
        check(!n.isNodeAncestor(null), "isNodeAncestor(null) is false");
        check(!n.isNodeDescendant(null), "isNodeDescendant(null) is false");
    }

    static void testRelations() {
        DefaultMutableTreeNode root = sample();
        DefaultMutableTreeNode c1 = (DefaultMutableTreeNode) root.getChildAt(0);
        DefaultMutableTreeNode c2 = (DefaultMutableTreeNode) root.getChildAt(1);
        DefaultMutableTreeNode g1 = (DefaultMutableTreeNode) c1.getChildAt(0);

        // Direction: x.isNodeAncestor(a) asks whether a is an ancestor of x;
        // x.isNodeDescendant(d) asks whether d is a descendant of x.
        check(root.isNodeAncestor(root), "isNodeAncestor self");
        check(g1.isNodeAncestor(root), "root is ancestor of grandchild");
        check(!root.isNodeAncestor(g1), "grandchild is not ancestor of root");
        check(g1.isNodeDescendant(g1), "isNodeDescendant self");
        check(root.isNodeDescendant(g1), "grandchild is a descendant of root");
        check(!g1.isNodeDescendant(root), "root is not a descendant of grandchild");
        check(g1.getRoot() == root && g1.isRoot() == false, "getRoot/isRoot");
        check(g1.getLevel() == 2 && c1.getLevel() == 1 && root.getLevel() == 0, "levels");
        check(root.getDepth() == 2 && c1.getDepth() == 1 && g1.getDepth() == 0, "depth");
        check(root.isNodeChild(c1) && !c1.isNodeChild(root), "isNodeChild");
        check(c1.isNodeSibling(c1) && c1.isNodeSibling(c2) && !root.isNodeSibling(c1), "isNodeSibling");
        check(root.getSiblingCount() == 1, "root sibling count is 1");
        check(c1.getSiblingCount() == 2, "sibling count includes self");
        check(root.isNodeRelated(g1), "isNodeRelated same tree");
        check(!root.isNodeRelated(new DefaultMutableTreeNode("z")), "isNodeRelated unrelated");
        check(g1.getSharedAncestor(c2) == root, "shared ancestor");
        check(c1.getSharedAncestor(g1) == c1, "shared ancestor is the nearer node");
        check(root.getSharedAncestor(new DefaultMutableTreeNode("z")) == null, "no shared ancestor -> null");
        check(root.getPreviousNode() == null, "root has no previous in preorder");
        check(root.getNextNode() == c1, "root next in preorder is first child");
        check(c1.getPreviousNode() == root, "previous node in preorder");
        check(c2.getNextNode() == null, "last node in preorder has no next");
        check(g1.getNextNode() == c2, "next node in preorder");
        check(root.getFirstChild() == c1 && root.getLastChild() == c2, "first/last child");
        check(root.getChildAfter(c1) == c2, "getChildAfter");
        check(root.getChildBefore(c1) == null, "getChildBefore first child");
        check(root.getChildAfter(c2) == null, "getChildAfter last child");
        check(c1.getNextSibling() == c2 && c2.getPreviousSibling() == c1, "siblings");
        check(c1.getNextSibling() == c2 && c2.getNextSibling() == null, "last sibling null");
        check(root.getFirstLeaf() == g1, "first leaf in depth-first order");
        check(root.getLastLeaf() == c2, "last leaf");
        check(g1.getNextLeaf() == c2 && c2.getNextLeaf() == null, "leaf chaining forward");
        check(c2.getPreviousLeaf() == g1 && g1.getPreviousLeaf() == null, "leaf chaining backward");
        check(root.getLeafCount() == 2, "leaf count");
        check(root.getChildCount() == 2, "child count");
        TreeNode[] path = g1.getPath();
        check(path.length == 3 && path[0] == root && path[1] == c1 && path[2] == g1, "getPath");
        Object[] uo = g1.getUserObjectPath();
        check(uo.length == 3 && "root".equals(uo[0]) && "g1".equals(uo[2]), "getUserObjectPath");
    }

    static void testTraversals() {
        DefaultMutableTreeNode root = sample();
        check("root,c1,g1,c2,".equals(enumStr(root.preorderEnumeration())), "preorder");
        check("g1,c1,c2,root,".equals(enumStr(root.postorderEnumeration())), "postorder");
        check("root,c1,c2,g1,".equals(enumStr(root.breadthFirstEnumeration())), "breadth-first");
        // depthFirst is postorder.
        check("g1,c1,c2,root,".equals(enumStr(root.depthFirstEnumeration())), "depth-first == postorder");
        check("root,c1,g1,".equals(enumStr(
                ((DefaultMutableTreeNode) root.getChildAt(0).getChildAt(0)).pathFromAncestorEnumeration(root))),
                "pathFromAncestor order");
        check("c1,c2,".equals(enumStr(root.children())), "children enumeration order");
        // Exhausted enumerations throw NoSuchElementException.
        Enumeration<?> e = root.children();
        e.nextElement();
        e.nextElement();
        boolean threw = false;
        try { e.nextElement(); } catch (NoSuchElementException ex) { threw = true; }
        check(threw, "exhausted children throws NoSuchElementException");
        check(!DefaultMutableTreeNode.EMPTY_ENUMERATION.hasMoreElements(), "EMPTY_ENUMERATION has no elements");
        DefaultMutableTreeNode leaf = new DefaultMutableTreeNode("l");
        check(leaf.preorderEnumeration().hasMoreElements(), "leaf preorder yields itself");
        check("l,".equals(enumStr(leaf.postorderEnumeration())), "leaf postorder yields itself");
    }

    static void testCloneAndSerialization() throws Exception {
        DefaultMutableTreeNode root = sample();
        // clone() copies user object by reference but drops parent and children.
        DefaultMutableTreeNode cl = (DefaultMutableTreeNode) root.clone();
        check(cl.getChildCount() == 0, "clone has no children");
        check(cl.getParent() == null, "clone has no parent");
        check("root".equals(cl.getUserObject()), "clone shares user object by reference");
        cl.setUserObject("changed");
        check("root".equals(root.getUserObject()), "user object reference shared, but field assignment is per node");

        DefaultMutableTreeNode back = (DefaultMutableTreeNode) roundTrip(root);
        check(back.getChildCount() == 2, "round-trip child count");
        check(back.getUserObject().equals("root"), "round-trip user object");
        DefaultMutableTreeNode bc1 = (DefaultMutableTreeNode) back.getChildAt(0);
        check(bc1.getChildCount() == 1, "round-trip grandchild count");
        check(bc1.getParent() == back, "round-trip parent links restored");
        check(bc1.getChildAt(0).getParent() == bc1, "round-trip deep parent links");
        DefaultMutableTreeNode added = new DefaultMutableTreeNode("new");
        back.add(added);
        check(back.getChildCount() == 3, "deserialized node still mutable");
    }

    public static void main(String[] args) throws Exception {
        testBasics();
        testExceptionMatrix();
        testRelations();
        testTraversals();
        testCloneAndSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
