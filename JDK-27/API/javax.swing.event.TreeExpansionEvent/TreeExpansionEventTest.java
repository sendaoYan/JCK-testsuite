/*
 * @test
 * @summary javax.swing.event.TreeExpansionEvent conformance: white-box structure + black-box behavior.
 * @run main TreeExpansionEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.tree.TreePath;

public class TreeExpansionEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.TreeExpansionEvent");
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
        TreePath path = new TreePath(new Object[] { "root", "child" });
        javax.swing.event.TreeExpansionEvent ev = new javax.swing.event.TreeExpansionEvent(source, path);
        check(ev.getSource() == source, "getSource round-trip");
        check(ev.getPath() == path, "getPath identity round-trip");
        check(ev.getPath().getPathCount() == 2, "path has two components");
        check(ev.getPath().getPathComponent(0).equals("root"), "path root component");
        check(ev.getPath().getPathComponent(1).equals("child"), "path child component");

        TreePath single = new TreePath("root");
        javax.swing.event.TreeExpansionEvent singleEv = new javax.swing.event.TreeExpansionEvent(source, single);
        check(singleEv.getPath() == single, "single path identity");
        check(singleEv.getPath().getPathCount() == 1, "single path count 1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
