/*
 * @test
 * @summary javax.swing.event.TableModelListener conformance: white-box structure + black-box behavior.
 * @run main TableModelListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.util.HashSet;
import java.util.Set;

public class TableModelListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.TableModelListener");
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
        Method m = c.getMethod("tableChanged", TableModelEvent.class);
        check(m.getReturnType() == void.class, "tableChanged returns void");
        check(Modifier.isAbstract(m.getModifiers()), "tableChanged abstract");
        check(c.getDeclaredMethods().length == 1, "declared method count == 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final TableModelEvent[] last = new TableModelEvent[1];
        TableModelListener l = new TableModelListener() {
            public void tableChanged(TableModelEvent e) { last[0] = e; }
        };

        TableModel model = new AbstractTableModel() {
            public int getRowCount() { return 10; }
            public int getColumnCount() { return 4; }
            public Object getValueAt(int r, int c) { return r * 100 + c; }
        };

        TableModelEvent insert = new TableModelEvent(model, 2, 5, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
        l.tableChanged(insert);
        check(last[0] == insert, "tableChanged fired");
        check(last[0].getSource() == model, "source round-trip");
        check(last[0].getFirstRow() == 2, "firstRow round-trip");
        check(last[0].getLastRow() == 5, "lastRow round-trip");
        check(last[0].getColumn() == TableModelEvent.ALL_COLUMNS, "column round-trip");
        check(last[0].getType() == TableModelEvent.INSERT, "type round-trip");

        TableModelEvent update = new TableModelEvent(model, 1, 1, 3, TableModelEvent.UPDATE);
        l.tableChanged(update);
        check(last[0].getType() == TableModelEvent.UPDATE && last[0].getColumn() == 3, "update type/column");

        TableModelEvent delete = new TableModelEvent(model, 0, 9, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
        l.tableChanged(delete);
        check(last[0].getType() == TableModelEvent.DELETE && last[0].getFirstRow() == 0 && last[0].getLastRow() == 9, "delete rows");

        TableModelEvent whole = new TableModelEvent(model);
        l.tableChanged(whole);
        check(last[0].getType() == TableModelEvent.UPDATE, "whole-model event defaults to UPDATE");
        check(last[0].getFirstRow() == 0 && last[0].getLastRow() == Integer.MAX_VALUE, "whole-model rows span all");
        check(last[0].getColumn() == TableModelEvent.ALL_COLUMNS, "whole-model column ALL_COLUMNS");

        check(TableModelEvent.INSERT != TableModelEvent.UPDATE && TableModelEvent.UPDATE != TableModelEvent.DELETE, "event type constants distinct");
        check(TableModelEvent.HEADER_ROW != TableModelEvent.INSERT, "HEADER_ROW distinct from INSERT");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.TableModelListener", false, TableModelListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.util.EventListener"), "implements java.util.EventListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("tableChanged"), "method tableChanged");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
