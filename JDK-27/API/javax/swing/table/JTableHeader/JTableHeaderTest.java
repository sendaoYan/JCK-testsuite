/*
 * @test
 * @summary javax.swing.table.JTableHeader conformance: white-box structure + black-box behavior.
 * @run main JTableHeaderTest
 */

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Modifier;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class JTableHeaderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.JTableHeader");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.event.TableColumnModelListener"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.Accessible"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // default constructor creates its own column model
        JTableHeader h0 = new JTableHeader();
        check(h0.getColumnModel() != null, "default ctor creates column model");

        // constructor with an explicit column model
        DefaultTableColumnModel cm = new DefaultTableColumnModel();
        TableColumn a = new TableColumn(0); a.setIdentifier("a"); a.setWidth(75);
        TableColumn b = new TableColumn(1); b.setIdentifier("b"); b.setWidth(100);
        cm.addColumn(a);
        cm.addColumn(b);
        JTableHeader h = new JTableHeader(cm);
        check(h.getColumnModel() == cm, "ctor keeps supplied column model");

        // set/get column model round-trip
        DefaultTableColumnModel cm2 = new DefaultTableColumnModel();
        cm2.addColumn(new TableColumn(0));
        h.setColumnModel(cm2);
        check(h.getColumnModel() == cm2, "setColumnModel round-trip");
        h.setColumnModel(cm);
        check(h.getColumnModel() == cm, "setColumnModel back to original");

        // reordering / resizing allowed
        check(h.getReorderingAllowed(), "reordering allowed default true");
        h.setReorderingAllowed(false);
        check(!h.getReorderingAllowed(), "set/get reorderingAllowed false");
        h.setReorderingAllowed(true);
        check(h.getReorderingAllowed(), "set/get reorderingAllowed true");
        check(h.getResizingAllowed(), "resizing allowed default true");
        h.setResizingAllowed(false);
        check(!h.getResizingAllowed(), "set/get resizingAllowed false");
        h.setResizingAllowed(true);
        check(h.getResizingAllowed(), "set/get resizingAllowed true");

        // initial drag/resize state
        check(h.getDraggedColumn() == null, "draggedColumn initially null");
        check(h.getDraggedDistance() == 0, "draggedDistance initially 0");
        check(h.getResizingColumn() == null, "resizingColumn initially null");

        // update in real time
        check(h.getUpdateTableInRealTime(), "updateTableInRealTime default true");
        h.setUpdateTableInRealTime(false);
        check(!h.getUpdateTableInRealTime(), "set/get updateTableInRealTime");

        // default renderer present and settable
        check(h.getDefaultRenderer() != null, "default renderer present");
        TableCellRenderer r = new DefaultTableCellRenderer();
        h.setDefaultRenderer(r);
        check(h.getDefaultRenderer() == r, "set/get defaultRenderer");

        // geometry helpers
        check(h.columnAtPoint(new Point(-5, 0)) == -1, "columnAtPoint negative x == -1");
        check(h.columnAtPoint(new Point(0, 0)) == 0, "columnAtPoint 0 == 0");
        Rectangle rect = h.getHeaderRect(0);
        check(rect != null, "getHeaderRect non-null");
        check(rect.width == 75, "getHeaderRect(0) width == column width");
        Dimension pref = h.getPreferredSize();
        check(pref != null && pref.width >= 0, "getPreferredSize non-negative");

        // UI identity
        check("TableHeaderUI".equals(h.getUIClassID()), "getUIClassID TableHeaderUI");
        check(h.getUI() != null, "getUI non-null");
        h.updateUI();
        check(h.getUI() != null, "updateUI keeps UI");

        // accessible context
        check(h.getAccessibleContext() != null, "getAccessibleContext non-null");

        // tooltip text for a point outside any column does not throw and yields null
        java.awt.event.MouseEvent me = new java.awt.event.MouseEvent(h,
                java.awt.event.MouseEvent.MOUSE_MOVED, 0L, 0, -5, 0, 1, false);
        String tip = h.getToolTipText(me);
        check(tip == null, "getToolTipText(valid event) callable and null for no column");

        // column model listener methods (part of interface) are callable
        h.columnAdded(new javax.swing.event.TableColumnModelEvent(cm, 0, 0));
        h.columnRemoved(new javax.swing.event.TableColumnModelEvent(cm, 0, 0));
        h.columnMoved(new javax.swing.event.TableColumnModelEvent(cm, 0, 0));
        h.columnMarginChanged(new javax.swing.event.ChangeEvent(h));
        h.columnSelectionChanged(new javax.swing.event.ListSelectionEvent(h, 0, 0, false));
        h.resizeAndRepaint();
        check(true, "column model listener callbacks callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
