/*
 * @test
 * @summary javax.swing.event.AncestorListener conformance: white-box structure + black-box behavior.
 * @run main AncestorListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.Container;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.util.HashSet;
import java.util.Set;

public class AncestorListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void checkMethod(Class<?> c, String name, Class<?>... params) {
        try {
            Method m = c.getMethod(name, params);
            check(m.getReturnType() == void.class, name + " returns void");
            check(Modifier.isAbstract(m.getModifiers()), name + " is abstract");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("missing method " + name);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.AncestorListener");
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
        checkMethod(c, "ancestorAdded", AncestorEvent.class);
        checkMethod(c, "ancestorRemoved", AncestorEvent.class);
        checkMethod(c, "ancestorMoved", AncestorEvent.class);
        check(c.getDeclaredMethods().length == 3, "declared method count == 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final String[] fired = new String[1];
        final AncestorEvent[] last = new AncestorEvent[1];
        AncestorListener l = new AncestorListener() {
            public void ancestorAdded(AncestorEvent e) { fired[0] = "added"; last[0] = e; }
            public void ancestorRemoved(AncestorEvent e) { fired[0] = "removed"; last[0] = e; }
            public void ancestorMoved(AncestorEvent e) { fired[0] = "moved"; last[0] = e; }
        };

        JComponent src = new JPanel();
        Container ancestor = new Container();
        Container ancestorParent = new Container();

        AncestorEvent added = new AncestorEvent(src, AncestorEvent.ANCESTOR_ADDED, ancestor, ancestorParent);
        l.ancestorAdded(added);
        check("added".equals(fired[0]), "ancestorAdded fired");
        check(last[0] == added, "ancestorAdded event instance");
        check(last[0].getComponent() == src, "getComponent round-trip");
        check(last[0].getAncestor() == ancestor, "getAncestor round-trip");
        check(last[0].getAncestorParent() == ancestorParent, "getAncestorParent round-trip");
        check(last[0].getID() == AncestorEvent.ANCESTOR_ADDED, "getID == ANCESTOR_ADDED");

        AncestorEvent removed = new AncestorEvent(src, AncestorEvent.ANCESTOR_REMOVED, ancestor, ancestorParent);
        l.ancestorRemoved(removed);
        check("removed".equals(fired[0]), "ancestorRemoved fired");
        check(last[0] == removed, "ancestorRemoved event instance");
        check(last[0].getComponent() == src, "getComponent round-trip 2");
        check(last[0].getAncestor() == ancestor, "getAncestor round-trip 2");
        check(last[0].getID() == AncestorEvent.ANCESTOR_REMOVED, "getID == ANCESTOR_REMOVED");

        AncestorEvent moved = new AncestorEvent(src, AncestorEvent.ANCESTOR_MOVED, ancestor, ancestorParent);
        l.ancestorMoved(moved);
        check("moved".equals(fired[0]), "ancestorMoved fired");
        check(last[0] == moved, "ancestorMoved event instance");
        check(last[0].getComponent() == src, "getComponent round-trip 3");
        check(last[0].getAncestorParent() == ancestorParent, "getAncestorParent round-trip 2");
        check(last[0].getID() == AncestorEvent.ANCESTOR_MOVED, "getID == ANCESTOR_MOVED");

        check(AncestorEvent.ANCESTOR_ADDED == 1 && AncestorEvent.ANCESTOR_REMOVED == 2 && AncestorEvent.ANCESTOR_MOVED == 3,
              "ANCESTOR_* id constants distinct/stable");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.AncestorListener", false, AncestorListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.util.EventListener"), "implements java.util.EventListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("ancestorAdded"), "method ancestorAdded");
        check(names.contains("ancestorRemoved"), "method ancestorRemoved");
        check(names.contains("ancestorMoved"), "method ancestorMoved");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
