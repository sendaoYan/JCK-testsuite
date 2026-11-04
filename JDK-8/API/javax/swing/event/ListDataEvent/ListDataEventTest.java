/*
 * @test
 * @summary javax.swing.event.ListDataEvent conformance: white-box structure + black-box behavior.
 * @run main ListDataEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.event.ListDataEvent;

public class ListDataEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.ListDataEvent");
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
        check(javax.swing.event.ListDataEvent.CONTENTS_CHANGED == 0, "CONTENTS_CHANGED == 0");
        check(javax.swing.event.ListDataEvent.INTERVAL_ADDED == 1, "INTERVAL_ADDED == 1");
        check(javax.swing.event.ListDataEvent.INTERVAL_REMOVED == 2, "INTERVAL_REMOVED == 2");

        Object source = new Object();
        javax.swing.event.ListDataEvent ev =
            new javax.swing.event.ListDataEvent(source, javax.swing.event.ListDataEvent.INTERVAL_ADDED, 4, 9);
        check(ev.getSource() == source, "getSource round-trip");
        check(ev.getType() == javax.swing.event.ListDataEvent.INTERVAL_ADDED, "getType round-trip");
        check(ev.getIndex0() == 4, "getIndex0 round-trip");
        check(ev.getIndex1() == 9, "getIndex1 round-trip");

        // Constructor normalizes the two indices to ascending order.
        javax.swing.event.ListDataEvent swapped =
            new javax.swing.event.ListDataEvent(source, javax.swing.event.ListDataEvent.INTERVAL_REMOVED, 20, 5);
        check(swapped.getIndex0() == 5, "getIndex0 normalized to min");
        check(swapped.getIndex1() == 20, "getIndex1 normalized to max");
        check(swapped.getType() == javax.swing.event.ListDataEvent.INTERVAL_REMOVED, "swapped getType");

        String s = ev.toString();
        check(s.contains("ListDataEvent"), "toString contains class name");
        check(s.contains("type=1"), "toString contains type");
        check(s.contains("index0=4") && s.contains("index1=9"), "toString contains indices");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static void test() throws Exception {
        ListDataEvent e = new ListDataEvent("src", ListDataEvent.INTERVAL_ADDED, 0, 2);
        check(e.getIndex0() == 0 && e.getIndex1() == 2, "indices");
        check(e.getType() == ListDataEvent.INTERVAL_ADDED, "type");
    }



    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
