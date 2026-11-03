/*
 * @test
 * @summary javax.swing.table.AbstractTableModel conformance: white-box structure + black-box behavior.
 * @run main AbstractTableModelTest
 */

import java.lang.reflect.Modifier;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

public class AbstractTableModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // a concrete backing model used by the black-box part
    static class MyModel extends AbstractTableModel {
        private final Object[][] data;
        MyModel(Object[][] data) { this.data = data; }
        public int getRowCount() { return data.length; }
        public int getColumnCount() { return data[0].length; }
        public Object getValueAt(int r, int c) { return data[r][c]; }
        public void setValueAt(Object v, int r, int c) { data[r][c] = v; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.AbstractTableModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.table.TableModel"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        MyModel m = new MyModel(new Object[][]{{"r0c0", "r0c1"}, {"r1c0", "r1c1"}});
        check(m.getRowCount() == 2, "rowCount");
        check(m.getColumnCount() == 2, "columnCount");
        check("r1c0".equals(m.getValueAt(1, 0)), "getValueAt");

        // default getColumnName spreadsheet convention
        check("A".equals(m.getColumnName(0)), "getColumnName 0 -> A");
        check("B".equals(m.getColumnName(1)), "getColumnName 1 -> B");
        check("Z".equals(m.getColumnName(25)), "getColumnName 25 -> Z");
        check("AA".equals(m.getColumnName(26)), "getColumnName 26 -> AA");
        check("AB".equals(m.getColumnName(27)), "getColumnName 27 -> AB");

        // findColumn
        check(m.findColumn("B") == 1, "findColumn B == 1");
        check(m.findColumn("missing") == -1, "findColumn missing == -1");

        // default getColumnClass / isCellEditable / setValueAt override
        check(m.getColumnClass(0) == Object.class, "default getColumnClass Object");
        check(!m.isCellEditable(0, 0), "default isCellEditable false");
        m.setValueAt("new", 0, 1);
        check("new".equals(m.getValueAt(0, 1)), "setValueAt override stores");

        // listeners
        final TableModelEvent[] last = new TableModelEvent[1];
        final int[] count = {0};
        TableModelListener l = e -> { last[0] = e; count[0]++; };
        m.addTableModelListener(l);
        check(m.getTableModelListeners().length == 1, "getTableModelListeners length 1");
        check(m.getListeners(TableModelListener.class).length == 1, "getListeners(TableModelListener) length 1");

        m.fireTableCellUpdated(1, 0);
        check(count[0] == 1, "fireTableCellUpdated fires once");
        check(last[0].getType() == TableModelEvent.UPDATE, "cell updated type UPDATE");
        check(last[0].getFirstRow() == 1 && last[0].getLastRow() == 1, "cell updated row range");
        check(last[0].getColumn() == 0, "cell updated column");

        m.fireTableRowsUpdated(0, 1);
        check(last[0].getType() == TableModelEvent.UPDATE, "rows updated type UPDATE");
        check(last[0].getFirstRow() == 0 && last[0].getLastRow() == 1, "rows updated range");
        check(last[0].getColumn() == TableModelEvent.ALL_COLUMNS, "rows updated ALL_COLUMNS");

        m.fireTableRowsInserted(2, 3);
        check(last[0].getType() == TableModelEvent.INSERT, "rows inserted type INSERT");
        check(last[0].getFirstRow() == 2 && last[0].getLastRow() == 3, "rows inserted range");

        m.fireTableRowsDeleted(0, 0);
        check(last[0].getType() == TableModelEvent.DELETE, "rows deleted type DELETE");
        check(last[0].getFirstRow() == 0 && last[0].getLastRow() == 0, "rows deleted range");

        m.fireTableStructureChanged();
        check(last[0].getType() == TableModelEvent.UPDATE, "structure changed type UPDATE");
        check(last[0].getFirstRow() == TableModelEvent.HEADER_ROW, "structure changed HEADER_ROW");

        m.fireTableDataChanged();
        check(last[0].getFirstRow() == 0 && last[0].getLastRow() == Integer.MAX_VALUE, "data changed full range");

        int before = count[0];
        m.removeTableModelListener(l);
        m.fireTableDataChanged();
        check(count[0] == before, "removeTableModelListener stops delivery");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
