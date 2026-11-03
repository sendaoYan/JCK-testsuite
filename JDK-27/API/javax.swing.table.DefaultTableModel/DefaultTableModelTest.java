/*
 * @test
 * @summary javax.swing.table.DefaultTableModel conformance: white-box structure + black-box behavior.
 * @run main DefaultTableModelTest
 */

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Vector;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

public class DefaultTableModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.DefaultTableModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.table.AbstractTableModel"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // default constructor: empty
        DefaultTableModel m0 = new DefaultTableModel();
        check(m0.getRowCount() == 0, "default: 0 rows");
        check(m0.getColumnCount() == 0, "default: 0 columns");

        // (rowCount, columnCount): null cells, spreadsheet column names
        DefaultTableModel m1 = new DefaultTableModel(3, 2);
        check(m1.getRowCount() == 3, "(3,2): rows");
        check(m1.getColumnCount() == 2, "(3,2): columns");
        check("A".equals(m1.getColumnName(0)), "(3,2): col0 name A");
        check("B".equals(m1.getColumnName(1)), "(3,2): col1 name B");
        check(m1.getValueAt(0, 0) == null, "(3,2): null cell");
        check(m1.isCellEditable(0, 0), "DefaultTableModel cells editable");
        check(m1.getColumnClass(0) == Object.class, "getColumnClass default Object");

        // Object[][] + Object[] columnIdentifiers
        DefaultTableModel m2 = new DefaultTableModel(
                new Object[][]{{1, "a"}, {2, "b"}, {3, "c"}},
                new Object[]{"ID", "Name"});
        check(m2.getRowCount() == 3, "array ctor: rows");
        check(m2.getColumnCount() == 2, "array ctor: columns");
        check("ID".equals(m2.getColumnName(0)), "array ctor: col name ID");
        check("Name".equals(m2.getColumnName(1)), "array ctor: col name Name");
        check(Integer.valueOf(1).equals(m2.getValueAt(0, 0)), "array ctor: value 0,0");
        check("b".equals(m2.getValueAt(1, 1)), "array ctor: value 1,1");
        check(m2.findColumn("Name") == 1, "findColumn Name == 1");

        // Vector dataVector + Vector columnIdentifiers
        Vector<String> colIds = new Vector<>(List.of("X", "Y"));
        Vector<Vector<Object>> data = new Vector<>();
        data.add(new Vector<>(List.of(10, 20)));
        data.add(new Vector<>(List.of(30, 40)));
        DefaultTableModel m3 = new DefaultTableModel(data, colIds);
        check(m3.getRowCount() == 2, "vector ctor: rows");
        check(m3.getColumnCount() == 2, "vector ctor: columns");
        check(Integer.valueOf(10).equals(m3.getValueAt(0, 0)), "vector ctor: value 0,0");
        check(Integer.valueOf(40).equals(m3.getValueAt(1, 1)), "vector ctor: value 1,1");

        // (Object[] columnNames, int rowCount) and (Vector columnNames, int rowCount)
        DefaultTableModel m4 = new DefaultTableModel(new Object[]{"P", "Q"}, 2);
        check(m4.getRowCount() == 2, "arrayNames ctor: rows");
        check(m4.getColumnCount() == 2, "arrayNames ctor: columns");
        check("P".equals(m4.getColumnName(0)), "arrayNames ctor: name P");
        DefaultTableModel m5 = new DefaultTableModel(new Vector<>(List.of("R")), 1);
        check(m5.getRowCount() == 1, "vectorNames ctor: rows");
        check(m5.getColumnCount() == 1, "vectorNames ctor: columns");
        check("R".equals(m5.getColumnName(0)), "vectorNames ctor: name R");

        // addRow / insertRow / removeRow
        DefaultTableModel mm = new DefaultTableModel(new Object[][]{{1, "a"}}, new Object[]{"ID", "Name"});
        mm.addRow(new Object[]{2, "b"});
        check(mm.getRowCount() == 2, "addRow: rows");
        check(Integer.valueOf(2).equals(mm.getValueAt(1, 0)), "addRow: appended value");
        mm.insertRow(0, new Object[]{0, "z"});
        check(mm.getRowCount() == 3, "insertRow: rows");
        check(Integer.valueOf(0).equals(mm.getValueAt(0, 0)), "insertRow: at front");
        mm.removeRow(1);
        check(mm.getRowCount() == 2, "removeRow: rows");
        check(Integer.valueOf(2).equals(mm.getValueAt(1, 0)), "removeRow: shifted value");

        // addColumn (three overloads) + setColumnIdentifiers + setColumnCount
        DefaultTableModel mc = new DefaultTableModel(2, 1);
        mc.addColumn("Extra");
        check(mc.getColumnCount() == 2, "addColumn(name): count");
        check("Extra".equals(mc.getColumnName(1)), "addColumn(name): last name");
        mc.addColumn("Data", new Object[]{7, 8});
        check(mc.getColumnCount() == 3, "addColumn(name,array): count");
        check(Integer.valueOf(7).equals(mc.getValueAt(0, 2)), "addColumn(name,array): data 0");
        check(Integer.valueOf(8).equals(mc.getValueAt(1, 2)), "addColumn(name,array): data 1");
        mc.addColumn("V", new Vector<>(List.of(11, 12)));
        check(mc.getColumnCount() == 4, "addColumn(name,vector): count");
        check(Integer.valueOf(12).equals(mc.getValueAt(1, 3)), "addColumn(name,vector): data");
        mc.setColumnIdentifiers(new Object[]{"a", "b", "c", "d"});
        check("a".equals(mc.getColumnName(0)), "setColumnIdentifiers(array): name 0");
        check("d".equals(mc.getColumnName(3)), "setColumnIdentifiers(array): name 3");
        mc.setColumnIdentifiers(new Vector<>(List.of("W", "X", "Y", "Z")));
        check("W".equals(mc.getColumnName(0)), "setColumnIdentifiers(vector): name 0");
        mc.setColumnCount(2);
        check(mc.getColumnCount() == 2, "setColumnCount shrink: count");
        check(mc.getRowCount() == 2, "setColumnCount shrink keeps rows");

        // getDataVector / setDataVector round-trip
        DefaultTableModel md = new DefaultTableModel(new Object[][]{{1}}, new Object[]{"N"});
        md.setDataVector(new Object[][]{{5, 6}, {7, 8}}, new Object[]{"A", "B"});
        check(md.getRowCount() == 2 && md.getColumnCount() == 2, "setDataVector(array): shape");
        Vector dv = md.getDataVector();
        check(dv.size() == 2, "getDataVector: rows");
        check(Integer.valueOf(6).equals(((Vector) dv.get(0)).get(1)), "getDataVector: cell 0,1");

        // setValueAt / getValueAt + multi-step chain
        DefaultTableModel ms = new DefaultTableModel(new Object[][]{{1, "a"}, {2, "b"}}, new Object[]{"ID", "Name"});
        ms.setValueAt("A", 0, 1);
        check("A".equals(ms.getValueAt(0, 1)), "setValueAt/getValueAt round-trip");

        // setRowCount / moveRow
        DefaultTableModel mr = new DefaultTableModel(new Object[][]{{1}, {2}, {3}}, new Object[]{"N"});
        mr.setRowCount(2);
        check(mr.getRowCount() == 2, "setRowCount shrink");
        check(Integer.valueOf(2).equals(mr.getValueAt(1, 0)), "setRowCount keeps first rows");
        DefaultTableModel mmv = new DefaultTableModel(new Object[][]{{1}, {2}, {3}}, new Object[]{"N"});
        mmv.moveRow(0, 0, 2);
        check(Integer.valueOf(2).equals(mmv.getValueAt(0, 0)), "moveRow: 2 to front");
        check(Integer.valueOf(3).equals(mmv.getValueAt(1, 0)), "moveRow: 3 shifted");
        check(Integer.valueOf(1).equals(mmv.getValueAt(2, 0)), "moveRow: 1 to end");

        // TableModelListener firing via fireTable* and mutators
        final TableModelEvent[] last = new TableModelEvent[1];
        final int[] count = {0};
        TableModelListener l = e -> { last[0] = e; count[0]++; };
        DefaultTableModel ml = new DefaultTableModel(new Object[][]{{1, "a"}}, new Object[]{"ID", "Name"});
        ml.addTableModelListener(l);
        ml.addRow(new Object[]{2, "b"});
        check(count[0] == 1, "addRow fires once");
        check(last[0].getType() == TableModelEvent.INSERT, "addRow fires INSERT");
        ml.setValueAt("B", 1, 1);
        check(last[0].getType() == TableModelEvent.UPDATE, "setValueAt fires UPDATE");
        check(last[0].getFirstRow() == 1 && last[0].getColumn() == 1, "setValueAt event location");
        ml.removeRow(0);
        check(last[0].getType() == TableModelEvent.DELETE, "removeRow fires DELETE");
        ml.fireTableStructureChanged();
        check(last[0].getFirstRow() == TableModelEvent.HEADER_ROW, "fireTableStructureChanged HEADER_ROW");
        ml.fireTableDataChanged();
        check(last[0].getFirstRow() == 0 && last[0].getLastRow() == Integer.MAX_VALUE, "fireTableDataChanged full range");
        check(last[0].getColumn() == TableModelEvent.ALL_COLUMNS, "fireTableDataChanged ALL_COLUMNS");
        ml.removeTableModelListener(l);
        int before = count[0];
        ml.fireTableDataChanged();
        check(count[0] == before, "removeTableModelListener stops delivery");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
