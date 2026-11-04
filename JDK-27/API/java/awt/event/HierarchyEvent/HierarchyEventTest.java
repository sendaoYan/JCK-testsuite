/*
 * @test
 * @summary java.awt.event.HierarchyEvent conformance: white-box structure + black-box behavior.
 * @run main HierarchyEventTest
 */

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.HierarchyEvent;
import java.lang.reflect.Modifier;

public class HierarchyEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.HierarchyEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "HierarchyEvent is public");
        check(!Modifier.isAbstract(m), "HierarchyEvent is concrete");
        check(c.getSuperclass() == AWTEvent.class, "HierarchyEvent extends AWTEvent");
        check(c.getField("HIERARCHY_CHANGED").getType() == int.class, "HIERARCHY_CHANGED int");
        check(c.getField("PARENT_CHANGED").getType() == int.class, "PARENT_CHANGED int");
        check(c.getConstructor(Component.class, int.class, Component.class, Container.class) != null, "has 4-arg ctor");
        check(c.getConstructor(Component.class, int.class, Component.class, Container.class, long.class) != null, "has 5-arg ctor");
    }

    static void testBlackBox() throws Exception {
        Component src = new Component() { };
        Component changed = new Component() { };
        Container parent = new Container();
        HierarchyEvent e = new HierarchyEvent(src, HierarchyEvent.HIERARCHY_CHANGED,
                                              changed, parent, HierarchyEvent.PARENT_CHANGED);

        check(e.getID() == HierarchyEvent.HIERARCHY_CHANGED, "getID round-trips");
        check(e.getSource() == src, "getSource round-trips");
        check(e.getComponent() == src, "getComponent returns source");
        check(e.getChanged() == changed, "getChanged round-trips");
        check(e.getChangedParent() == parent, "getChangedParent round-trips");
        check(e.getChangeFlags() == HierarchyEvent.PARENT_CHANGED, "getChangeFlags round-trips");

        // combined flags
        long flags = HierarchyEvent.PARENT_CHANGED | HierarchyEvent.SHOWING_CHANGED;
        HierarchyEvent combo = new HierarchyEvent(src, HierarchyEvent.HIERARCHY_CHANGED, changed, parent, flags);
        check(combo.getChangeFlags() == flags, "combined flags round-trip");

        // documented constants
        check(HierarchyEvent.HIERARCHY_FIRST == 1400, "HIERARCHY_FIRST == 1400");
        check(HierarchyEvent.HIERARCHY_CHANGED == 1400, "HIERARCHY_CHANGED == 1400");
        check(HierarchyEvent.ANCESTOR_MOVED == 1401, "ANCESTOR_MOVED == 1401");
        check(HierarchyEvent.ANCESTOR_RESIZED == 1402, "ANCESTOR_RESIZED == 1402");
        check(HierarchyEvent.HIERARCHY_LAST == 1402, "HIERARCHY_LAST == 1402");
        check(HierarchyEvent.PARENT_CHANGED == 0x1, "PARENT_CHANGED == 0x1");
        check(HierarchyEvent.DISPLAYABILITY_CHANGED == 0x2, "DISPLAYABILITY_CHANGED == 0x2");
        check(HierarchyEvent.SHOWING_CHANGED == 0x4, "SHOWING_CHANGED == 0x4");

        // paramString
        check(e.paramString().contains("HIERARCHY_CHANGED"), "paramString HIERARCHY_CHANGED");
        check(e.paramString().contains("PARENT_CHANGED"), "paramString PARENT_CHANGED");
        HierarchyEvent moved = new HierarchyEvent(src, HierarchyEvent.ANCESTOR_MOVED, changed, parent);
        check(moved.paramString().contains("ANCESTOR_MOVED"), "paramString ANCESTOR_MOVED");
        check(moved.getChangeFlags() == 0, "4-arg ctor changeFlags 0");

        check(e instanceof AWTEvent, "HierarchyEvent instanceof AWTEvent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
