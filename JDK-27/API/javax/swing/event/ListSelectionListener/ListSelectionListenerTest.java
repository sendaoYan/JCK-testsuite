/*
 * @test
 * @summary javax.swing.event.ListSelectionListener conformance: white-box structure + black-box behavior.
 * @run main ListSelectionListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ListSelectionListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.ListSelectionListener");
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
        Method m = c.getMethod("valueChanged", ListSelectionEvent.class);
        check(m.getReturnType() == void.class, "valueChanged returns void");
        check(Modifier.isAbstract(m.getModifiers()), "valueChanged abstract");
        check(c.getDeclaredMethods().length == 1, "declared method count == 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final ListSelectionEvent[] last = new ListSelectionEvent[1];
        ListSelectionListener l = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) { last[0] = e; }
        };

        Object src = new Object();
        ListSelectionEvent ev = new ListSelectionEvent(src, 2, 4, true);
        l.valueChanged(ev);
        check(last[0] == ev, "valueChanged fired");
        check(last[0].getSource() == src, "getSource round-trip");
        check(last[0].getFirstIndex() == 2, "getFirstIndex round-trip");
        check(last[0].getLastIndex() == 4, "getLastIndex round-trip");
        check(last[0].getValueIsAdjusting() == true, "getValueIsAdjusting round-trip true");

        ListSelectionEvent ev2 = new ListSelectionEvent(src, 1, 1, false);
        l.valueChanged(ev2);
        check(last[0] == ev2, "valueChanged fired again");
        check(last[0].getFirstIndex() == 1 && last[0].getLastIndex() == 1, "single index selection");
        check(last[0].getValueIsAdjusting() == false, "getValueIsAdjusting round-trip false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
