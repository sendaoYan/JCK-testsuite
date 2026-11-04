/*
 * @test
 * @summary javax.swing.AbstractListModel conformance: white-box structure + black-box behavior.
 * @run main AbstractListModelTest
 */

import java.lang.reflect.Modifier;

import javax.swing.AbstractListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class AbstractListModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.AbstractListModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.ListModel"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AbstractListModel<String> m = new AbstractListModel<String>() {
            public int getSize() { return 3; }
            public String getElementAt(int i) { return "e" + i; }
        };

        check(m.getSize() == 3, "getSize");
        check("e1".equals(m.getElementAt(1)), "getElementAt");

        final int[] events = {0};
        ListDataListener l = new ListDataListener() {
            public void intervalAdded(ListDataEvent e) { events[0]++; }
            public void intervalRemoved(ListDataEvent e) { events[0]++; }
            public void contentsChanged(ListDataEvent e) { events[0]++; }
        };
        m.addListDataListener(l);
        check(m.getListDataListeners().length == 1, "listener registered");

        // fireContentsChanged is protected; a subclass exercises it
        class FiringModel extends AbstractListModel<String> {
            public int getSize() { return 0; }
            public String getElementAt(int i) { return null; }
            void fire() { fireContentsChanged(this, 0, 0); }
        }
        FiringModel firing = new FiringModel();
        firing.addListDataListener(l);
        firing.fire();
        check(events[0] == 1, "contentsChanged fired");

        // getListeners filters by type
        check(m.getListeners(ListDataListener.class).length == 1, "getListeners count");
        m.removeListDataListener(l);
        check(m.getListDataListeners().length == 0, "listener removed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
