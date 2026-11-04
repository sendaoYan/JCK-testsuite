/*
 * @test
 * @summary javax.swing.event.TreeWillExpandListener conformance: white-box structure + black-box behavior.
 * @run main TreeWillExpandListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

public class TreeWillExpandListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void checkThrowsExpandVeto(Method m) {
        Class<?>[] ex = m.getExceptionTypes();
        boolean has = false;
        for (Class<?> e : ex) if (e == ExpandVetoException.class) has = true;
        check(has, m.getName() + " declares ExpandVetoException");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.TreeWillExpandListener");
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
        Method expand = c.getMethod("treeWillExpand", TreeExpansionEvent.class);
        check(expand.getReturnType() == void.class, "treeWillExpand returns void");
        checkThrowsExpandVeto(expand);
        Method collapse = c.getMethod("treeWillCollapse", TreeExpansionEvent.class);
        check(collapse.getReturnType() == void.class, "treeWillCollapse returns void");
        checkThrowsExpandVeto(collapse);
        check(c.getDeclaredMethods().length == 2, "declared method count == 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final String[] fired = new String[1];
        final TreeExpansionEvent[] last = new TreeExpansionEvent[1];
        TreeWillExpandListener l = new TreeWillExpandListener() {
            public void treeWillExpand(TreeExpansionEvent e) { fired[0] = "expand"; last[0] = e; }
            public void treeWillCollapse(TreeExpansionEvent e) { fired[0] = "collapse"; last[0] = e; }
        };

        Object src = new Object();
        TreePath path = new TreePath(new Object[] { "root", "child" });

        TreeExpansionEvent expandEv = new TreeExpansionEvent(src, path);
        l.treeWillExpand(expandEv);
        check("expand".equals(fired[0]) && last[0] == expandEv, "treeWillExpand fired");
        check(last[0].getPath().equals(path), "expand getPath round-trip");

        TreeExpansionEvent collapseEv = new TreeExpansionEvent(src, path);
        l.treeWillCollapse(collapseEv);
        check("collapse".equals(fired[0]) && last[0] == collapseEv, "treeWillCollapse fired");
        check(last[0].getPath().equals(path), "collapse getPath round-trip");

        // Veto semantics: a listener may throw ExpandVetoException to block expansion.
        TreeWillExpandListener vetoer = new TreeWillExpandListener() {
            public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
                throw new ExpandVetoException(e, "vetoed");
            }
            public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
                throw new ExpandVetoException(e, "vetoed");
            }
        };
        boolean threw = false;
        try {
            vetoer.treeWillExpand(expandEv);
        } catch (ExpandVetoException e) {
            threw = true;
            check(e.getCause() == null, "ExpandVetoException cause null");
        }
        check(threw, "treeWillExpand may veto via ExpandVetoException");

        threw = false;
        try {
            vetoer.treeWillCollapse(collapseEv);
        } catch (ExpandVetoException e) {
            threw = true;
        }
        check(threw, "treeWillCollapse may veto via ExpandVetoException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
