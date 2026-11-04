/*
 * @test
 * @summary javax.swing.event.TreeModelListener conformance: white-box structure + black-box behavior.
 * @run main TreeModelListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

public class TreeModelListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void checkMethod(Class<?> c, String name, Class<?>... params) {
        try {
            Method m = c.getMethod(name, params);
            check(m.getReturnType() == void.class, name + " returns void");
            check(Modifier.isAbstract(m.getModifiers()), name + " abstract");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("missing method " + name);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.TreeModelListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.util.EventListener"), "interface 0");
        checkMethod(c, "treeNodesChanged", TreeModelEvent.class);
        checkMethod(c, "treeNodesInserted", TreeModelEvent.class);
        checkMethod(c, "treeNodesRemoved", TreeModelEvent.class);
        checkMethod(c, "treeStructureChanged", TreeModelEvent.class);
        check(c.getDeclaredMethods().length == 4, "declared method count == 4");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final String[] fired = new String[1];
        final TreeModelEvent[] last = new TreeModelEvent[1];
        TreeModelListener l = new TreeModelListener() {
            public void treeNodesChanged(TreeModelEvent e) { fired[0] = "changed"; last[0] = e; }
            public void treeNodesInserted(TreeModelEvent e) { fired[0] = "inserted"; last[0] = e; }
            public void treeNodesRemoved(TreeModelEvent e) { fired[0] = "removed"; last[0] = e; }
            public void treeStructureChanged(TreeModelEvent e) { fired[0] = "structure"; last[0] = e; }
        };

        Object src = new Object();
        Object[] path = new Object[] { "root", "node" };
        int[] indices = new int[] { 0, 2 };
        Object[] children = new Object[] { "a", "b" };

        TreeModelEvent changed = new TreeModelEvent(src, path, indices, children);
        l.treeNodesChanged(changed);
        check("changed".equals(fired[0]) && last[0] == changed, "treeNodesChanged fired");
        check(last[0].getSource() == src, "source round-trip");
        Object[] gotPath = last[0].getPath();
        check(gotPath.length == 2 && "root".equals(gotPath[0]) && "node".equals(gotPath[1]), "getPath contents round-trip");
        int[] gotIdx = last[0].getChildIndices();
        check(gotIdx.length == 2 && gotIdx[0] == 0 && gotIdx[1] == 2, "getChildIndices contents round-trip");
        Object[] gotChildren = last[0].getChildren();
        check(gotChildren.length == 2 && "a".equals(gotChildren[0]) && "b".equals(gotChildren[1]), "getChildren contents round-trip");
        TreePath gotTreePath = last[0].getTreePath();
        check(gotTreePath.getPathCount() == 2 && "root".equals(gotTreePath.getPathComponent(0)), "getTreePath derived");

        TreePath tp = new TreePath(new Object[] { "r", "n" });
        TreeModelEvent inserted = new TreeModelEvent(src, tp, new int[] { 1 }, new Object[] { "x" });
        l.treeNodesInserted(inserted);
        check("inserted".equals(fired[0]) && last[0] == inserted, "treeNodesInserted fired");
        check(last[0].getTreePath().equals(tp), "getTreePath round-trip");

        TreeModelEvent removed = new TreeModelEvent(src, path, indices, children);
        l.treeNodesRemoved(removed);
        check("removed".equals(fired[0]) && last[0] == removed, "treeNodesRemoved fired");
        check(last[0].getChildIndices().length == 2, "removed childIndices length");

        TreeModelEvent structure = new TreeModelEvent(src, tp);
        l.treeStructureChanged(structure);
        check("structure".equals(fired[0]) && last[0] == structure, "treeStructureChanged fired");
        check(last[0].getTreePath().equals(tp), "structure getTreePath round-trip");
        check(last[0].getChildIndices() != null && last[0].getChildIndices().length == 0, "structure childIndices empty");
        check(last[0].getChildren() == null, "structure children null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
