/*
 * @test
 * @summary javax.swing.event.ListDataListener conformance: white-box structure + black-box behavior.
 * @run main ListDataListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class ListDataListenerTest {
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
        Class<?> c = Class.forName("javax.swing.event.ListDataListener");
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
        checkMethod(c, "intervalAdded", ListDataEvent.class);
        checkMethod(c, "intervalRemoved", ListDataEvent.class);
        checkMethod(c, "contentsChanged", ListDataEvent.class);
        check(c.getDeclaredMethods().length == 3, "declared method count == 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final String[] fired = new String[1];
        final ListDataEvent[] last = new ListDataEvent[1];
        ListDataListener l = new ListDataListener() {
            public void intervalAdded(ListDataEvent e) { fired[0] = "added"; last[0] = e; }
            public void intervalRemoved(ListDataEvent e) { fired[0] = "removed"; last[0] = e; }
            public void contentsChanged(ListDataEvent e) { fired[0] = "changed"; last[0] = e; }
        };

        Object src = new Object();

        ListDataEvent added = new ListDataEvent(src, ListDataEvent.INTERVAL_ADDED, 0, 2);
        l.intervalAdded(added);
        check("added".equals(fired[0]), "intervalAdded fired");
        check(last[0] == added, "intervalAdded event instance");
        check(last[0].getType() == ListDataEvent.INTERVAL_ADDED, "added type round-trip");
        check(last[0].getIndex0() == 0 && last[0].getIndex1() == 2, "added index round-trip");
        check(last[0].getSource() == src, "added source round-trip");

        ListDataEvent removed = new ListDataEvent(src, ListDataEvent.INTERVAL_REMOVED, 1, 1);
        l.intervalRemoved(removed);
        check("removed".equals(fired[0]), "intervalRemoved fired");
        check(last[0].getType() == ListDataEvent.INTERVAL_REMOVED, "removed type round-trip");
        check(last[0].getIndex0() == 1 && last[0].getIndex1() == 1, "removed index round-trip");

        ListDataEvent changed = new ListDataEvent(src, ListDataEvent.CONTENTS_CHANGED, 0, 5);
        l.contentsChanged(changed);
        check("changed".equals(fired[0]), "contentsChanged fired");
        check(last[0].getType() == ListDataEvent.CONTENTS_CHANGED, "changed type round-trip");
        check(last[0].getIndex0() == 0 && last[0].getIndex1() == 5, "changed index round-trip");

        check(ListDataEvent.CONTENTS_CHANGED != ListDataEvent.INTERVAL_ADDED, "CONTENTS_CHANGED != INTERVAL_ADDED");
        check(ListDataEvent.INTERVAL_ADDED != ListDataEvent.INTERVAL_REMOVED, "INTERVAL_ADDED != INTERVAL_REMOVED");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
