/*
 * @test
 * @summary javax.swing.event.TreeSelectionEvent conformance: white-box structure + black-box behavior.
 * @run main TreeSelectionEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.tree.TreePath;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class TreeSelectionEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.TreeSelectionEvent");
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
        TreePath p1 = new TreePath(new Object[] { "root", "a" });
        TreePath p2 = new TreePath(new Object[] { "root", "b" });
        TreePath oldLead = new TreePath(new Object[] { "root", "old" });
        TreePath newLead = new TreePath(new Object[] { "root", "new" });

        TreePath[] paths = { p1, p2 };
        boolean[] areNew = { true, false };
        javax.swing.event.TreeSelectionEvent ev =
            new javax.swing.event.TreeSelectionEvent(source, paths, areNew, oldLead, newLead);

        check(ev.getSource() == source, "getSource round-trip");

        TreePath[] gotPaths = ev.getPaths();
        check(gotPaths.length == 2 && gotPaths[0].equals(p1) && gotPaths[1].equals(p2), "getPaths round-trip");
        check(gotPaths != paths, "getPaths returns defensive copy");

        check(ev.getPath() == p1, "getPath returns first path");
        check(ev.isAddedPath() == true, "isAddedPath() returns first flag");
        check(ev.isAddedPath(0) == true, "isAddedPath(0)");
        check(ev.isAddedPath(1) == false, "isAddedPath(1)");
        check(ev.isAddedPath(p1) == true, "isAddedPath(p1) by equals");
        check(ev.isAddedPath(p2) == false, "isAddedPath(p2) by equals");
        check(ev.getOldLeadSelectionPath() == oldLead, "getOldLeadSelectionPath round-trip");
        check(ev.getNewLeadSelectionPath() == newLead, "getNewLeadSelectionPath round-trip");

        // Single-path constructor.
        javax.swing.event.TreeSelectionEvent single =
            new javax.swing.event.TreeSelectionEvent(source, p1, false, oldLead, newLead);
        check(single.getPaths().length == 1 && single.getPaths()[0].equals(p1), "single getPaths");
        check(single.getPath().equals(p1), "single getPath");
        check(single.isAddedPath() == false, "single isAddedPath false");
        check(single.getOldLeadSelectionPath() == oldLead, "single old lead");
        check(single.getNewLeadSelectionPath() == newLead, "single new lead");

        // cloneWithSource returns an event with a new source and identical paths.
        Object newSource = new Object();
        javax.swing.event.TreeSelectionEvent cloned = (javax.swing.event.TreeSelectionEvent) ev.cloneWithSource(newSource);
        check(cloned != ev, "cloneWithSource distinct instance");
        check(cloned.getSource() == newSource, "cloneWithSource new source");
        check(cloned.getPaths().length == 2 && cloned.getPaths()[0].equals(p1) && cloned.getPaths()[1].equals(p2),
              "cloneWithSource copies paths");
        check(cloned.isAddedPath(0) == true && cloned.isAddedPath(1) == false, "cloneWithSource copies flags");
        check(cloned.getOldLeadSelectionPath() == oldLead && cloned.getNewLeadSelectionPath() == newLead,
              "cloneWithSource copies leads");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.TreeSelectionEvent", false, TreeSelectionEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPaths"), "method getPaths");
        check(names.contains("getPath"), "method getPath");
        check(names.contains("isAddedPath"), "method isAddedPath");
        check(names.contains("isAddedPath"), "method isAddedPath");
        check(names.contains("isAddedPath"), "method isAddedPath");
        check(names.contains("getOldLeadSelectionPath"), "method getOldLeadSelectionPath");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
