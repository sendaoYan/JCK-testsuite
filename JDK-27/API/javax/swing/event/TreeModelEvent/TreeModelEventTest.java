/*
 * @test
 * @summary javax.swing.event.TreeModelEvent conformance: white-box structure + black-box behavior.
 * @run main TreeModelEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.tree.TreePath;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

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


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.TreeModelEvent", false, TreeModelEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getTreePath"), "method getTreePath");
        check(names.contains("getPath"), "method getPath");
        check(names.contains("getChildren"), "method getChildren");
        check(names.contains("getChildIndices"), "method getChildIndices");
    }


    // ---- Part 2b: (TreePath, int[], Object[]) constructor ----
    static void testExtraContracts() {
        Object sourceTp = new Object();
        TreePath tp = new TreePath(new Object[] { "r", "p" });
        int[] indicesTp = { 0, 2 };
        Object[] childrenTp = { "c0", "c2" };
        javax.swing.event.TreeModelEvent ev =
            new javax.swing.event.TreeModelEvent(sourceTp, tp, indicesTp, childrenTp);
        check(ev.getSource() == sourceTp, "(TreePath,int[],Object[]) source round-trip");
        check(ev.getTreePath() == tp, "(TreePath,int[],Object[]) path identity");
        check(ev.getPath().length == 2 && ev.getPath()[0].equals("r"), "(TreePath,int[],Object[]) getPath");
        check(ev.getChildIndices().length == 2 && ev.getChildIndices()[0] == 0
              && ev.getChildIndices()[1] == 2, "(TreePath,int[],Object[]) indices round-trip");
        check(ev.getChildren().length == 2 && ev.getChildren()[0].equals("c0")
              && ev.getChildren()[1].equals("c2"), "(TreePath,int[],Object[]) children round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        testExtraContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
