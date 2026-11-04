/*
 * @test
 * @summary javax.swing.event.ListSelectionEvent conformance: white-box structure + black-box behavior.
 * @run main ListSelectionEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.event.ListSelectionEvent;

public class ListSelectionEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.ListSelectionEvent");
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
        javax.swing.event.ListSelectionEvent ev = new javax.swing.event.ListSelectionEvent(source, 3, 10, true);
        check(ev.getSource() == source, "getSource round-trip");
        check(ev.getFirstIndex() == 3, "getFirstIndex round-trip");
        check(ev.getLastIndex() == 10, "getLastIndex round-trip");
        check(ev.getValueIsAdjusting() == true, "getValueIsAdjusting true");

        javax.swing.event.ListSelectionEvent notAdjusting = new javax.swing.event.ListSelectionEvent(source, 0, 0, false);
        check(notAdjusting.getValueIsAdjusting() == false, "getValueIsAdjusting false");
        check(notAdjusting.getFirstIndex() == 0 && notAdjusting.getLastIndex() == 0, "single index range");

        // Unlike ListDataEvent, indices are NOT normalized.
        javax.swing.event.ListSelectionEvent inverted = new javax.swing.event.ListSelectionEvent(source, 8, 2, false);
        check(inverted.getFirstIndex() == 8, "getFirstIndex not normalized");
        check(inverted.getLastIndex() == 2, "getLastIndex not normalized");

        String s = ev.toString();
        check(s.contains("ListSelectionEvent"), "toString contains class name");
        check(s.contains("firstIndex= 3") && s.contains("lastIndex= 10"), "toString contains indices");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static void test() throws Exception {
        ListSelectionEvent e = new ListSelectionEvent("src", 1, 5, false);
        check(e.getFirstIndex() == 1 && e.getLastIndex() == 5, "indices");
        check(!e.getValueIsAdjusting(), "adjusting");
    }



    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
