/*
 * @test
 * @summary javax.swing.table.TableColumn conformance: white-box structure + black-box behavior.
 * @run main TableColumnTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class TableColumnTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.TableColumn");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // default constructor defaults
        TableColumn c0 = new TableColumn();
        check(c0.getModelIndex() == 0, "default: modelIndex 0");
        check(c0.getWidth() == 75, "default: width 75");
        check(c0.getPreferredWidth() == 75, "default: preferredWidth 75");
        check(c0.getMinWidth() == 15, "default: minWidth 15");
        check(c0.getMaxWidth() == Integer.MAX_VALUE, "default: maxWidth Integer.MAX_VALUE");
        check(c0.getResizable(), "default: resizable true");
        check(c0.getHeaderValue() == null, "default: headerValue null");
        check(c0.getIdentifier() == null, "default: identifier null");

        // width-taking constructors
        TableColumn c1 = new TableColumn(7);
        check(c1.getModelIndex() == 7, "(modelIndex): modelIndex");
        TableColumn c2 = new TableColumn(9, 120);
        check(c2.getModelIndex() == 9 && c2.getWidth() == 120, "(modelIndex,width)");

        // set/get modelIndex, identifier, headerValue
        c2.setModelIndex(11);
        check(c2.getModelIndex() == 11, "set/get modelIndex");
        c2.setIdentifier("col-id");
        check("col-id".equals(c2.getIdentifier()), "set/get identifier");
        c2.setHeaderValue("Header");
        check("Header".equals(c2.getHeaderValue()), "set/get headerValue");

        // width round-trip and clamping
        c2.setMaxWidth(200);
        c2.setMinWidth(20);
        c2.setPreferredWidth(150);
        check(c2.getPreferredWidth() == 150, "set/get preferredWidth");
        c2.setWidth(150);
        check(c2.getWidth() == 150, "set/get width");
        c2.setWidth(9999);
        check(c2.getWidth() == 200, "setWidth clamps to maxWidth");
        c2.setWidth(1);
        check(c2.getWidth() == 20, "setWidth clamps to minWidth");
        check(c2.getMinWidth() == 20 && c2.getMaxWidth() == 200, "set/get min/maxWidth");

        // resizable
        c2.setResizable(false);
        check(!c2.getResizable(), "set/get resizable false");
        c2.setResizable(true);
        check(c2.getResizable(), "set/get resizable true");

        // renderers/editor round-trip
        TableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        TableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        TableCellEditor editor = new TableCellEditor() {
            public java.awt.Component getTableCellEditorComponent(javax.swing.JTable t, Object v, boolean s, int r, int col) { return new javax.swing.JLabel(); }
            public Object getCellEditorValue() { return null; }
            public boolean isCellEditable(java.util.EventObject e) { return true; }
            public boolean shouldSelectCell(java.util.EventObject e) { return true; }
            public boolean stopCellEditing() { return true; }
            public void cancelCellEditing() { }
            public void addCellEditorListener(javax.swing.event.CellEditorListener l) { }
            public void removeCellEditorListener(javax.swing.event.CellEditorListener l) { }
        };
        c2.setHeaderRenderer(headerRenderer);
        c2.setCellRenderer(cellRenderer);
        c2.setCellEditor(editor);
        check(c2.getHeaderRenderer() == headerRenderer, "set/get headerRenderer");
        check(c2.getCellRenderer() == cellRenderer, "set/get cellRenderer");
        check(c2.getCellEditor() == editor, "set/get cellEditor");

        // 4-arg constructor round-trip
        TableColumn c3 = new TableColumn(5, 100, cellRenderer, editor);
        check(c3.getModelIndex() == 5, "4-arg: modelIndex");
        check(c3.getWidth() == 100, "4-arg: width");
        check(c3.getCellRenderer() == cellRenderer, "4-arg: cellRenderer");
        check(c3.getCellEditor() == editor, "4-arg: cellEditor");

        // property change listener firing
        final String[] propName = new String[1];
        final Object[] oldV = new Object[1];
        final Object[] newV = new Object[1];
        PropertyChangeListener pcl = e -> {
            propName[0] = e.getPropertyName();
            oldV[0] = e.getOldValue();
            newV[0] = e.getNewValue();
        };
        TableColumn c4 = new TableColumn();
        c4.addPropertyChangeListener(pcl);
        c4.setIdentifier("before");
        c4.setIdentifier("after");
        check("identifier".equals(propName[0]), "identifier change fires property");
        check("before".equals(oldV[0]) && "after".equals(newV[0]), "identifier change old/new value");
        c4.setHeaderValue("H");
        check("headerValue".equals(propName[0]), "headerValue change fires property");
        c4.setModelIndex(3);
        check("modelIndex".equals(propName[0]), "modelIndex change fires property");
        c4.setWidth(80);
        check("width".equals(propName[0]), "width change fires property");
        c4.removePropertyChangeListener(pcl);
        c4.setIdentifier("ignored");
        check("ignored".equals(c4.getIdentifier()), "identifier still settable after listener removed");
        check("width".equals(propName[0]), "no event after listener removed");

        // sizeWidthToFit: clamps min/preferred/width to renderer size (positive widths)
        TableColumn c5 = new TableColumn(0);
        c5.setHeaderRenderer(new DefaultTableCellRenderer());
        c5.setHeaderValue("a header");
        c5.sizeWidthToFit();
        check(c5.getWidth() == c5.getPreferredWidth(), "sizeWidthToFit: width == preferredWidth");
        check(c5.getMinWidth() >= 0, "sizeWidthToFit: minWidth non-negative");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
