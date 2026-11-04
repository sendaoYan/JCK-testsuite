/*
 * @test
 * @summary javax.swing.event.TreeExpansionEvent conformance: white-box structure + black-box behavior.
 * @run main TreeExpansionEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.tree.TreePath;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

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


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.TreeExpansionEvent", false, TreeExpansionEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPath"), "method getPath");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
