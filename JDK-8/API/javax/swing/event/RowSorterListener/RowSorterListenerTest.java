/*
 * @test
 * @summary javax.swing.event.RowSorterListener conformance: white-box structure + black-box behavior.
 * @run main RowSorterListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.RowSorter;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.HashSet;
import java.util.Set;

public class RowSorterListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.RowSorterListener");
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
        Method m = c.getMethod("sorterChanged", RowSorterEvent.class);
        check(m.getReturnType() == void.class, "sorterChanged returns void");
        check(Modifier.isAbstract(m.getModifiers()), "sorterChanged abstract");
        check(c.getDeclaredMethods().length == 1, "declared method count == 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final RowSorterEvent[] last = new RowSorterEvent[1];
        RowSorterListener l = new RowSorterListener() {
            public void sorterChanged(RowSorterEvent e) { last[0] = e; }
        };

        RowSorter<TableModel> sorter = new TableRowSorter<TableModel>();
        int[] prev = new int[] { 5, 3, 1 };
        RowSorterEvent sorted = new RowSorterEvent(sorter, RowSorterEvent.Type.SORTED, prev);
        l.sorterChanged(sorted);
        check(last[0] == sorted, "sorterChanged fired");
        check(last[0].getSource() == sorter, "getSource returns typed RowSorter");
        check(last[0].getType() == RowSorterEvent.Type.SORTED, "getType SORTED");
        check(last[0].getPreviousRowCount() == 3, "getPreviousRowCount == 3");
        check(last[0].convertPreviousRowIndexToModel(0) == 5, "convert(0)==5");
        check(last[0].convertPreviousRowIndexToModel(1) == 3, "convert(1)==3");
        check(last[0].convertPreviousRowIndexToModel(2) == 1, "convert(2)==1");
        check(last[0].convertPreviousRowIndexToModel(-1) == -1, "convert(-1)==-1");
        check(last[0].convertPreviousRowIndexToModel(3) == -1, "convert(3)==-1 out of range");

        RowSorterEvent orderChanged = new RowSorterEvent(sorter);
        l.sorterChanged(orderChanged);
        check(last[0].getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED, "getType SORT_ORDER_CHANGED");
        check(last[0].getPreviousRowCount() == 0, "getPreviousRowCount == 0 for order-changed");
        check(last[0].convertPreviousRowIndexToModel(0) == -1, "convert == -1 when no mapping");
        check(last[0].getSource() == sorter, "order-changed source round-trip");

        RowSorterEvent.Type[] types = RowSorterEvent.Type.values();
        check(types.length == 2, "Type.values() length == 2");
        check(RowSorterEvent.Type.valueOf("SORT_ORDER_CHANGED") == RowSorterEvent.Type.SORT_ORDER_CHANGED, "valueOf SORT_ORDER_CHANGED");
        check(RowSorterEvent.Type.valueOf("SORTED") == RowSorterEvent.Type.SORTED, "valueOf SORTED");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.RowSorterListener", false, RowSorterListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.util.EventListener"), "implements java.util.EventListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("sorterChanged"), "method sorterChanged");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
