/*
 * @test
 * @summary javax.swing.event.TreeModelEvent conformance: white-box structure + black-box behavior.
 * @run main TreeModelEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.tree.TreePath;

public class TreeModelEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.TreeModelEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        Object source = new Object();
        Object[] pathComponents = { "root", "parent" };
        int[] indices = { 0, 2 };
        Object[] children = { "child0", "child2" };

        // Object[] path constructor builds a TreePath internally.
        javax.swing.event.TreeModelEvent ev =
            new javax.swing.event.TreeModelEvent(source, pathComponents, indices, children);
        check(ev.getSource() == source, "getSource round-trip");
        check(ev.getTreePath() != null, "getTreePath non-null");
        check(ev.getTreePath().getPathCount() == 2, "tree path count 2");
        check(ev.getTreePath().getPathComponent(0).equals("root"), "tree path root");
        check(ev.getTreePath().getPathComponent(1).equals("parent"), "tree path parent");

        Object[] path = ev.getPath();
        check(path.length == 2 && path[0].equals("root") && path[1].equals("parent"), "getPath round-trip");

        int[] childIndices = ev.getChildIndices();
        check(childIndices.length == 2 && childIndices[0] == 0 && childIndices[1] == 2, "getChildIndices round-trip");

        Object[] gotChildren = ev.getChildren();
        check(gotChildren.length == 2 && gotChildren[0].equals("child0") && gotChildren[1].equals("child2"),
              "getChildren round-trip");
        check(gotChildren != children, "getChildren returns defensive copy");

        // TreePath constructor round-trips the supplied path directly.
        TreePath tp = new TreePath(new Object[] { "root" });
        javax.swing.event.TreeModelEvent tpEv = new javax.swing.event.TreeModelEvent(source, tp);
        check(tpEv.getTreePath() == tp, "TreePath constructor identity");
        check(tpEv.getChildIndices().length == 0, "TreePath-only constructor empty indices");
        check(tpEv.getChildren() == null, "TreePath-only constructor null children");

        javax.swing.event.TreeModelEvent pathOnly = new javax.swing.event.TreeModelEvent(source, pathComponents);
        check(pathOnly.getTreePath().getPathCount() == 2, "Object[]-path-only constructor builds path");
        check(pathOnly.getChildIndices() != null && pathOnly.getChildIndices().length == 0,
              "Object[]-path-only constructor empty indices");
        check(pathOnly.getChildren() == null, "Object[]-path-only constructor null children");

        check(ev.toString() != null && ev.toString().length() > 0, "toString non-empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
