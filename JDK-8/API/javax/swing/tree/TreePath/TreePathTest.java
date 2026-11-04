/*
 * @test
 * @summary javax.swing.tree.TreePath conformance: immutable path (headless-safe):
 *          single-object and array constructors with null/empty validation,
 *          component access with range checks, parent/child path derivation,
 *          descendant semantics, equals/hashCode across constructions, serialization.
 * @run main TreePathTest
 */

import javax.swing.tree.TreePath;

public class TreePathTest {
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

    public static void main(String[] args) throws Exception {
        testConstructors();
        testAccessors();
        testEquality();
        testPathOperations();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testConstructors() {
        TreePath single = new TreePath("root");
        check(single.getPathCount() == 1, "single-object path count");
        check("root".equals(single.getLastPathComponent()), "single-object last component");
        TreePath arr = new TreePath(new Object[]{"a", "b"});
        check(arr.getPathCount() == 2, "array path count");
        // The array is defensively copied.
        Object[] src = new Object[]{"a", "b"};
        TreePath cp = new TreePath(src);
        src[0] = "mutated";
        check("a".equals(cp.getPathComponent(0)), "array constructor copies input");
        checkThrows(IllegalArgumentException.class, "single null object rejected",
                new ThrowingRunnable() { public void run() { new TreePath((Object) null); } });
        checkThrows(IllegalArgumentException.class, "null array rejected",
                new ThrowingRunnable() { public void run() { new TreePath((Object[]) null); } });
        checkThrows(IllegalArgumentException.class, "empty array rejected",
                new ThrowingRunnable() { public void run() { new TreePath(new Object[0]); } });
        checkThrows(IllegalArgumentException.class, "null last element rejected",
                new ThrowingRunnable() { public void run() { new TreePath(new Object[]{"a", null}); } });
    }

    static void testAccessors() {
        TreePath p = new TreePath(new Object[]{"a", "b", "c"});
        check("b".equals(p.getPathComponent(1)), "getPathComponent");
        check("c".equals(p.getLastPathComponent()), "getLastPathComponent");
        Object[] got = p.getPath();
        check(got.length == 3 && "a".equals(got[0]) && "c".equals(got[2]), "getPath");
        check("[a, b, c]".equals(p.toString()), "toString: " + p.toString());
        checkThrows(IllegalArgumentException.class, "getPathComponent beyond count",
                new ThrowingRunnable() { public void run() { p.getPathComponent(3); } });
        checkThrows(IllegalArgumentException.class, "getPathComponent negative",
                new ThrowingRunnable() { public void run() { p.getPathComponent(-1); } });
    }

    static void testEquality() {
        TreePath p1 = new TreePath(new Object[]{"a", "b"});
        TreePath p2 = new TreePath("a").pathByAddingChild("b");
        check(p1.equals(p2), "equal contents equal regardless of construction");
        check(p1.hashCode() == p2.hashCode(), "hashCode consistent with equals");
        check(!p1.equals(null), "equals(null) false");
        check(!p1.equals("a"), "equals non-TreePath false");
        check(!p1.equals(new TreePath("a")), "different lengths not equal");
        check(!p1.equals(new TreePath(new Object[]{"a", "c"})), "different tail not equal");
        TreePath s1 = new TreePath("x");
        TreePath s2 = new TreePath("x");
        check(s1.equals(s2), "single-component equality");
    }

    static void testPathOperations() {
        TreePath root = new TreePath("a");
        TreePath mid = root.pathByAddingChild("b");
        TreePath leaf = mid.pathByAddingChild("c");
        check(mid.getPathCount() == 2 && leaf.getPathCount() == 3, "pathByAddingChild grows");
        check(mid.getParentPath().equals(root), "getParentPath of mid");
        check(leaf.getParentPath().equals(mid), "getParentPath of leaf");
        check(root.getParentPath() == null, "single-component path has null parent path");
        // isDescendant: x.isDescendant(d) asks whether d is a descendant of x.
        check(root.isDescendant(root), "every path is its own descendant");
        check(root.isDescendant(leaf), "root is ancestor: root.isDescendant(leaf)");
        check(mid.isDescendant(leaf), "mid is ancestor of leaf");
        check(!leaf.isDescendant(root), "leaf is not ancestor of root");
        check(!mid.isDescendant(root), "mid is not ancestor of root");
        check(!root.isDescendant(new TreePath("z")), "unrelated path is not a descendant");
        checkThrows(NullPointerException.class, "pathByAddingChild(null)",
                new ThrowingRunnable() { public void run() { root.pathByAddingChild(null); } });
        // The array returned by getPath is a live view of the internal array in this
        // implementation, so treat it as read-only; its contents match the path.
        check(java.util.Arrays.equals(leaf.getPath(), new Object[]{"a", "b", "c"}), "full path contents");
    }

    static void testSerialization() throws Exception {
        TreePath p = new TreePath(new Object[]{"a", "b", "c"});
        TreePath back = (TreePath) roundTrip(p);
        check(back.equals(p), "serialization round-trip equals");
        check(back.hashCode() == p.hashCode(), "serialization round-trip hashCode");
        check("c".equals(back.getLastPathComponent()), "round-trip last component");
        check(back.getPathCount() == 3, "round-trip count");
        TreePath back2 = (TreePath) roundTrip(new TreePath("solo"));
        check(back2.getPathCount() == 1 && "solo".equals(back2.getLastPathComponent()), "round-trip single");
    }
}
