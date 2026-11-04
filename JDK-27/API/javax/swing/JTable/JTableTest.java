/*
 * @test
 * @summary javax.swing.JTable conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JTableTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public class JTableTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JTable");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 7, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.event.TableModelListener"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.Scrollable"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.swing.event.TableColumnModelListener"), "interface 2");
        check(c.getInterfaces()[3].getName().equals("javax.swing.event.ListSelectionListener"), "interface 3");
        check(c.getInterfaces()[4].getName().equals("javax.swing.event.CellEditorListener"), "interface 4");
        check(c.getInterfaces()[5].getName().equals("javax.accessibility.Accessible"), "interface 5");
        check(c.getInterfaces()[6].getName().equals("javax.swing.event.RowSorterListener"), "interface 6");
        // supplementary structural checks
        check(c.getDeclaredField("AUTO_RESIZE_OFF").getInt(null) == 0, "AUTO_RESIZE_OFF == 0");
        check(c.getDeclaredField("AUTO_RESIZE_ALL_COLUMNS").getInt(null) == 4, "AUTO_RESIZE_ALL_COLUMNS == 4");
        check(Class.forName("javax.swing.JTable$PrintMode").isEnum(), "nested PrintMode is enum");
        check(c.getDeclaredField("dataModel").getType().getName().equals("javax.swing.table.TableModel"), "protected dataModel field");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JFrame frame = new JFrame("JTableTest");
        try {
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            Object[][] full = {
                {"banana", "3", "x"},
                {"apple", "1", "y"},
                {"cherry", "2", "z"}};
            Object[] colNames = {"A", "B", "C"};
            DefaultTableModel model = new DefaultTableModel(full, colNames);
            JTable table = new JTable(model);
            frame.add(table);
            frame.pack();

            // fixed model content
            check(table.getRowCount() == 3, "row count 3");
            check(table.getColumnCount() == 3, "column count 3");
            check("banana".equals(table.getValueAt(0, 0)), "valueAt(0,0)");
            check("cherry".equals(table.getValueAt(2, 0)), "valueAt(2,0)");
            check("A".equals(table.getColumnName(0)), "column name 0");
            check(table.getColumnClass(0) == Object.class, "column class Object for DefaultTableModel");
            check(table.isCellEditable(0, 0), "DefaultTableModel cells editable");
            check(table.getModel() == model, "model same instance");

            // row selection model level
            table.setRowSelectionInterval(1, 1);
            check(table.getSelectedRow() == 1, "selected row 1");
            check(table.getSelectedRowCount() == 1, "selected row count 1");
            check(Arrays.equals(table.getSelectedRows(), new int[]{1}), "selected rows {1}");
            check(table.isRowSelected(1) && !table.isRowSelected(0), "isRowSelected(1) only");
            table.clearSelection();
            check(table.getSelectedRow() == -1, "clearSelection resets selected row");

            // explicit sorter: deterministic string ordering on column 0
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);
            check(table.getRowSorter() == sorter, "row sorter same instance");
            table.setRowSelectionInterval(1, 1);
            check(table.getSelectedRow() == 1, "selected row 1 before sort (view==model)");
            sorter.toggleSortOrder(0);  // ascending: apple(m1), banana(m0), cherry(m2)
            check(table.convertRowIndexToView(0) == 1, "asc: model 0 -> view 1");
            check(table.convertRowIndexToView(1) == 0, "asc: model 1 -> view 0");
            check(table.convertRowIndexToView(2) == 2, "asc: model 2 -> view 2");
            check(table.convertRowIndexToModel(0) == 1, "asc: view 0 -> model 1");
            check("apple".equals(table.getValueAt(0, 0)), "getValueAt converts view row 0 to model");
            check(table.getSelectedRow() == 0, "selection follows sort (updateSelectionOnSort)");
            check(table.getUpdateSelectionOnSort(), "updateSelectionOnSort default true");
            sorter.toggleSortOrder(0);  // descending: cherry(m2), banana(m0), apple(m1)
            check(table.convertRowIndexToView(0) == 1, "desc: model 0 -> view 1");
            check(table.convertRowIndexToView(2) == 0, "desc: model 2 -> view 0");
            check(table.getSelectedRow() == 2, "selection follows sort descending");

            // autoCreateRowSorter and clearing the sorter
            table.setAutoCreateRowSorter(true);
            check(table.getRowSorter() != null && table.getRowSorter() instanceof TableRowSorter,
                    "autoCreateRowSorter installs TableRowSorter");
            check(table.getAutoCreateRowSorter(), "autoCreateRowSorter flag true");
            table.setRowSorter(null);
            check(table.getRowSorter() == null, "setRowSorter(null) clears sorter");
            check(table.convertRowIndexToView(0) == 0 && table.convertRowIndexToModel(2) == 2,
                    "identity mapping without sorter");

            // column move semantics (view/model conversion)
            table.moveColumn(0, 1);
            check(table.convertColumnIndexToView(0) == 1, "after move(0,1): model col 0 -> view 1");
            check(table.convertColumnIndexToView(1) == 0, "after move(0,1): model col 1 -> view 0");
            check(table.convertColumnIndexToModel(0) == 1, "after move(0,1): view 0 -> model 1");
            check(table.convertColumnIndexToModel(1) == 0, "after move(0,1): view 1 -> model 0");
            check("B".equals(table.getColumnName(0)), "getColumnName uses view coordinates");
            check("A".equals(table.getColumnName(1)), "getColumnName view 1");
            TableColumn colB = table.getColumn("B");
            check("B".equals(colB.getHeaderValue()), "getColumn(B).headerValue");
            check("B".equals(colB.getIdentifier()), "getColumn(B).identifier");
            table.moveColumn(0, 1);  // move back
            check("A".equals(table.getColumnName(0)), "move back restores names");

            // cell selection via changeSelection and explicit intervals
            table.changeSelection(2, 1, false, false);
            check(table.getSelectedRow() == 2, "changeSelection selects row 2");
            check(table.getSelectedColumn() == 1, "changeSelection lead column 1");
            table.setColumnSelectionAllowed(true);
            check(table.getColumnSelectionAllowed(), "column selection allowed");
            table.setRowSelectionInterval(1, 1);
            table.setColumnSelectionInterval(2, 2);
            check(table.getSelectedColumn() == 2, "selected column 2");
            check(Arrays.equals(table.getSelectedColumns(), new int[]{2}), "selected columns {2}");
            check(table.getSelectedColumnCount() == 1, "selected column count 1");
            check(table.isColumnSelected(2), "isColumnSelected(2)");
            check(table.isCellSelected(1, 2), "isCellSelected(1,2) with row+column selection");

            // default editors/renderers for common classes
            check(table.getDefaultRenderer(Object.class) != null, "default renderer Object");
            check(table.getDefaultRenderer(Number.class) != null, "default renderer Number");
            check(table.getDefaultEditor(Object.class) != null, "default editor Object");
            check(table.getDefaultEditor(Boolean.class) != null, "default editor Boolean");
            TableCellRenderer custom = new DefaultTableCellRenderer();
            table.setDefaultRenderer(java.util.Date.class, custom);
            check(table.getDefaultRenderer(java.util.Date.class) == custom, "custom default renderer roundtrip");

            // geometry-ish properties (pure properties, no L&F values asserted)
            table.setRowHeight(30);
            check(table.getRowHeight() == 30, "row height roundtrip");
            check(table.getRowHeight(0) == 30, "per-row height follows row height");
            table.setRowMargin(4);
            check(table.getRowMargin() == 4, "row margin roundtrip");
            table.setShowGrid(false);
            check(!table.getShowHorizontalLines() && !table.getShowVerticalLines(), "setShowGrid(false)");
            table.setShowGrid(true);
            check(table.getShowHorizontalLines() && table.getShowVerticalLines(), "setShowGrid(true)");
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            check(table.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF, "auto resize mode roundtrip");

            // setValueAt roundtrip on the fixed model
            table.setValueAt("zeta", 2, 1);
            check("zeta".equals(table.getValueAt(2, 1)), "setValueAt/getValueAt roundtrip");
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JTableTest::testWhiteBox);
        runOnEDT(JTableTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}
