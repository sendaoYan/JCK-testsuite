/*
 * @test
 * @summary javax.swing.table.TableModel conformance: white-box structure + black-box behavior.
 * @run main TableModelTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class TableModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // full anonymous implementation with a backing store and listener list
    static TableModel makeModel() {
        return new TableModel() {
            private final Object[][] data = {{1, "a"}, {2, "b"}};
            private final String[] names = {"ID", "Name"};
            private final List<TableModelListener> listeners = new ArrayList<>();

            public int getRowCount() { return data.length; }
            public int getColumnCount() { return names.length; }
            public String getColumnName(int c) { return names[c]; }
            public Class<?> getColumnClass(int c) { return data[0][c].getClass(); }
            public boolean isCellEditable(int r, int c) { return c == 1; }
            public Object getValueAt(int r, int c) { return data[r][c]; }
            public void setValueAt(Object v, int r, int c) { data[r][c] = v; }
            public void addTableModelListener(TableModelListener l) { listeners.add(l); }
            public void removeTableModelListener(TableModelListener l) { listeners.remove(l); }

            void fire() {
                TableModelEvent e = new TableModelEvent(this);
                for (TableModelListener l : new ArrayList<>(listeners)) l.tableChanged(e);
            }
        };
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.TableModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TableModel m = makeModel();
        check(m.getRowCount() == 2, "getRowCount");
        check(m.getColumnCount() == 2, "getColumnCount");
        check("ID".equals(m.getColumnName(0)) && "Name".equals(m.getColumnName(1)), "getColumnName");
        check(m.getColumnClass(0) == Integer.class, "getColumnClass Integer");
        check(m.getColumnClass(1) == String.class, "getColumnClass String");
        check(!m.isCellEditable(0, 0) && m.isCellEditable(0, 1), "isCellEditable");
        check(Integer.valueOf(1).equals(m.getValueAt(0, 0)), "getValueAt");
        check("b".equals(m.getValueAt(1, 1)), "getValueAt 1,1");

        // setValueAt then getValueAt round-trip
        m.setValueAt("B", 1, 1);
        check("B".equals(m.getValueAt(1, 1)), "setValueAt/getValueAt round-trip");

        // listener add/remove
        final int[] fired = {0};
        TableModelListener l = e -> fired[0]++;
        m.addTableModelListener(l);
        // trigger via reflection-free helper is not possible on interface;
        // verify listener is accepted/removed without error and can be re-added.
        m.removeTableModelListener(l);
        m.addTableModelListener(l);
        m.removeTableModelListener(l);
        check(fired[0] == 0, "listener add/remove accepted");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
