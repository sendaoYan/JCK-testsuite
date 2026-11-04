/*
 * @test
 * @summary javax.swing.event.TableColumnModelListener conformance: white-box structure + black-box behavior.
 * @run main TableColumnModelListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;

public class TableColumnModelListenerTest {
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
        Class<?> c = Class.forName("javax.swing.event.TableColumnModelListener");
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
        checkMethod(c, "columnAdded", TableColumnModelEvent.class);
        checkMethod(c, "columnRemoved", TableColumnModelEvent.class);
        checkMethod(c, "columnMoved", TableColumnModelEvent.class);
        checkMethod(c, "columnMarginChanged", ChangeEvent.class);
        checkMethod(c, "columnSelectionChanged", ListSelectionEvent.class);
        check(c.getDeclaredMethods().length == 5, "declared method count == 5");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final String[] fired = new String[1];
        final Object[] last = new Object[1];
        TableColumnModelListener l = new TableColumnModelListener() {
            public void columnAdded(TableColumnModelEvent e) { fired[0] = "added"; last[0] = e; }
            public void columnRemoved(TableColumnModelEvent e) { fired[0] = "removed"; last[0] = e; }
            public void columnMoved(TableColumnModelEvent e) { fired[0] = "moved"; last[0] = e; }
            public void columnMarginChanged(ChangeEvent e) { fired[0] = "margin"; last[0] = e; }
            public void columnSelectionChanged(ListSelectionEvent e) { fired[0] = "selection"; last[0] = e; }
        };

        TableColumnModel model = new DefaultTableColumnModel();

        TableColumnModelEvent added = new TableColumnModelEvent(model, 0, 2);
        l.columnAdded(added);
        check("added".equals(fired[0]) && last[0] == added, "columnAdded fired");
        check(((TableColumnModelEvent) last[0]).getFromIndex() == 0, "added fromIndex round-trip");
        check(((TableColumnModelEvent) last[0]).getToIndex() == 2, "added toIndex round-trip");
        check(((TableColumnModelEvent) last[0]).getSource() == model, "added source round-trip");

        TableColumnModelEvent removed = new TableColumnModelEvent(model, 1, 1);
        l.columnRemoved(removed);
        check("removed".equals(fired[0]) && last[0] == removed, "columnRemoved fired");

        TableColumnModelEvent moved = new TableColumnModelEvent(model, 0, 3);
        l.columnMoved(moved);
        check("moved".equals(fired[0]) && last[0] == moved, "columnMoved fired");
        check(((TableColumnModelEvent) last[0]).getFromIndex() == 0 && ((TableColumnModelEvent) last[0]).getToIndex() == 3, "moved indices round-trip");

        ChangeEvent margin = new ChangeEvent(model);
        l.columnMarginChanged(margin);
        check("margin".equals(fired[0]) && last[0] == margin, "columnMarginChanged fired");

        ListSelectionEvent selection = new ListSelectionEvent(model, 1, 3, true);
        l.columnSelectionChanged(selection);
        check("selection".equals(fired[0]) && last[0] == selection, "columnSelectionChanged fired");
        check(((ListSelectionEvent) last[0]).getFirstIndex() == 1 && ((ListSelectionEvent) last[0]).getLastIndex() == 3, "selection indices round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
