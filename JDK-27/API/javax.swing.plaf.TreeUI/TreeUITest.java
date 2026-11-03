/*
 * @test
 * @summary javax.swing.plaf.TreeUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TreeUITest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class TreeUITest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    static Object reflectNew(String className, Class<?>[] paramTypes, Object... args) throws Exception {
        Class<?> c = Class.forName(className);
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor(paramTypes);
        ctor.setAccessible(true);
        return ctor.newInstance(args);
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(TreeUITest::testWhiteBox);
        runOnEDT(TreeUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.TreeUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.ComponentUI"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JTree tree = new javax.swing.JTree();
        javax.swing.plaf.TreeUI ui = (javax.swing.plaf.TreeUI) tree.getUI();
        check(ui != null, "tree UI non-null");
        check(ui.getRowCount(tree) >= 1, "row count >= 1");
        javax.swing.tree.TreePath root = ui.getPathForRow(tree, 0);
        check(root != null, "root path");
        check(ui.getRowForPath(tree, root) == 0, "row for root path");
        check(ui.getPathBounds(tree, root) != null, "path bounds");
        check(ui.isEditing(tree) == false, "not editing");
        check(ui.getClosestPathForLocation(tree, 0, 0) != null, "closest path");
    }
}
