/*
 * @test
 * @summary javax.swing.event.TreeExpansionListener conformance: white-box structure + black-box behavior.
 * @run main TreeExpansionListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;

public class TreeExpansionListenerTest {
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
        Class<?> c = Class.forName("javax.swing.event.TreeExpansionListener");
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
        checkMethod(c, "treeExpanded", TreeExpansionEvent.class);
        checkMethod(c, "treeCollapsed", TreeExpansionEvent.class);
        check(c.getDeclaredMethods().length == 2, "declared method count == 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final String[] fired = new String[1];
        final TreeExpansionEvent[] last = new TreeExpansionEvent[1];
        TreeExpansionListener l = new TreeExpansionListener() {
            public void treeExpanded(TreeExpansionEvent e) { fired[0] = "expanded"; last[0] = e; }
            public void treeCollapsed(TreeExpansionEvent e) { fired[0] = "collapsed"; last[0] = e; }
        };

        Object src = new Object();
        TreePath path = new TreePath(new Object[] { "root", "child" });

        TreeExpansionEvent expanded = new TreeExpansionEvent(src, path);
        l.treeExpanded(expanded);
        check("expanded".equals(fired[0]) && last[0] == expanded, "treeExpanded fired");
        check(last[0].getSource() == src, "source round-trip");
        check(last[0].getPath() == path, "getPath round-trip");
        check(last[0].getPath().getPathCount() == 2, "path component count");

        TreeExpansionEvent collapsed = new TreeExpansionEvent(src, path);
        l.treeCollapsed(collapsed);
        check("collapsed".equals(fired[0]) && last[0] == collapsed, "treeCollapsed fired");
        check(last[0].getPath() == path, "collapsed getPath round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
