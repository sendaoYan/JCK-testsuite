/*
 * @test
 * @summary javax.swing.event.TreeSelectionListener conformance: white-box structure + black-box behavior.
 * @run main TreeSelectionListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

public class TreeSelectionListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.TreeSelectionListener");
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
        Method m = c.getMethod("valueChanged", TreeSelectionEvent.class);
        check(m.getReturnType() == void.class, "valueChanged returns void");
        check(Modifier.isAbstract(m.getModifiers()), "valueChanged abstract");
        check(c.getDeclaredMethods().length == 1, "declared method count == 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final TreeSelectionEvent[] last = new TreeSelectionEvent[1];
        TreeSelectionListener l = new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) { last[0] = e; }
        };

        Object src = new Object();
        TreePath path = new TreePath(new Object[] { "root", "child" });
        TreePath oldLead = new TreePath(new Object[] { "root", "old" });
        TreePath newLead = new TreePath(new Object[] { "root", "new" });

        TreeSelectionEvent ev = new TreeSelectionEvent(src, path, true, oldLead, newLead);
        l.valueChanged(ev);
        check(last[0] == ev, "valueChanged fired");
        check(last[0].getSource() == src, "getSource round-trip");
        check(last[0].getPath().equals(path), "getPath round-trip");
        check(last[0].isAddedPath() == true, "isAddedPath round-trip");
        check(last[0].getOldLeadSelectionPath().equals(oldLead), "old lead round-trip");
        check(last[0].getNewLeadSelectionPath().equals(newLead), "new lead round-trip");

        TreeSelectionEvent ev2 = new TreeSelectionEvent(src, path, false, oldLead, newLead);
        l.valueChanged(ev2);
        check(last[0].isAddedPath() == false, "isAddedPath false round-trip");

        // multi-path constructor + isAddedPath(int)/getPaths()
        TreePath[] paths = new TreePath[] { path, newLead };
        boolean[] areNew = new boolean[] { true, false };
        TreeSelectionEvent multi = new TreeSelectionEvent(src, paths, areNew, oldLead, newLead);
        l.valueChanged(multi);
        check(last[0].getPaths().length == 2, "getPaths length");
        check(last[0].isAddedPath(0) == true, "isAddedPath(0)");
        check(last[0].isAddedPath(1) == false, "isAddedPath(1)");
        check(last[0].isAddedPath(path) == true, "isAddedPath(path)");

        Object src2 = new Object();
        TreeSelectionEvent cloned = (TreeSelectionEvent) multi.cloneWithSource(src2);
        check(cloned.getSource() == src2, "cloneWithSource round-trip");
        check(cloned.getPaths().length == 2, "cloneWithSource preserves paths");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
