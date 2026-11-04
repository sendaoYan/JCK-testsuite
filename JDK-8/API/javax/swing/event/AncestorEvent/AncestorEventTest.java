/*
 * @test
 * @summary javax.swing.event.AncestorEvent conformance: white-box structure + black-box behavior.
 * @run main AncestorEventTest
 */

import java.awt.Container;
import java.lang.reflect.Modifier;
import javax.swing.JComponent;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class AncestorEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.AncestorEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.AWTEvent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getField("ANCESTOR_ADDED") != null, "field ANCESTOR_ADDED");
        check(c.getField("ANCESTOR_REMOVED") != null, "field ANCESTOR_REMOVED");
        check(c.getField("ANCESTOR_MOVED") != null, "field ANCESTOR_MOVED");
    }

    static void testBlackBox() {
        check(javax.swing.event.AncestorEvent.ANCESTOR_ADDED == 1, "ANCESTOR_ADDED == 1");
        check(javax.swing.event.AncestorEvent.ANCESTOR_REMOVED == 2, "ANCESTOR_REMOVED == 2");
        check(javax.swing.event.AncestorEvent.ANCESTOR_MOVED == 3, "ANCESTOR_MOVED == 3");

        JComponent comp = new javax.swing.JLabel();
        Container ancestor = new Container();
        Container ancestorParent = new Container();

        javax.swing.event.AncestorEvent added =
            new javax.swing.event.AncestorEvent(comp, javax.swing.event.AncestorEvent.ANCESTOR_ADDED, ancestor, ancestorParent);
        check(added.getSource() == comp, "getSource returns component");
        check(added.getComponent() == comp, "getComponent returns component");
        check(added.getAncestor() == ancestor, "getAncestor round-trip");
        check(added.getAncestorParent() == ancestorParent, "getAncestorParent round-trip");
        check(added.getID() == javax.swing.event.AncestorEvent.ANCESTOR_ADDED, "getID == ANCESTOR_ADDED");

        javax.swing.event.AncestorEvent removed =
            new javax.swing.event.AncestorEvent(comp, javax.swing.event.AncestorEvent.ANCESTOR_REMOVED, ancestor, null);
        check(removed.getID() == javax.swing.event.AncestorEvent.ANCESTOR_REMOVED, "getID == ANCESTOR_REMOVED");
        check(removed.getAncestorParent() == null, "removed ancestorParent null");

        javax.swing.event.AncestorEvent moved =
            new javax.swing.event.AncestorEvent(comp, javax.swing.event.AncestorEvent.ANCESTOR_MOVED, ancestor, ancestorParent);
        check(moved.getID() == javax.swing.event.AncestorEvent.ANCESTOR_MOVED, "getID == ANCESTOR_MOVED");
        check(moved.getComponent() == comp, "moved getComponent round-trip");
        check(moved.getAncestor() == ancestor, "moved getAncestor round-trip");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.AncestorEvent", false, AncestorEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.AWTEvent"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAncestor"), "method getAncestor");
        check(names.contains("getAncestorParent"), "method getAncestorParent");
        check(names.contains("getComponent"), "method getComponent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
