/*
 * @test
 * @summary javax.swing.table.DefaultTableModel conformance: rows/columns (headless-safe):
 *          all six constructors, row add/insert/remove/move with exact TableModelEvent
 *          rows/columns, column add/identifiers, setDataVector liveness, boundary
 *          ArrayIndexOutOfBoundsException matrix, serialization.
 * @run main DefaultTableModelTest
 */

import java.util.Vector;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

public class DefaultTableModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Exception; }

    static void checkThrows(Class<? extends Throwable> ex, String m, ThrowingRunnable r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + ": expected " + ex.getName() + " but got " + t.getClass().getName() + ": " + t.getMessage());
        }
        throw new AssertionError(m + ": expected " + ex.getName() + " but no exception");
    }

    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object r = ois.readObject();
        ois.close();
        return r;
    }

    static class Recorder implements TableModelListener {
        int type = Integer.MIN_VALUE, first = -9, last = -9, col = -9, count;
        public void tableChanged(TableModelEvent e) {
            type = e.getType(); first = e.getFirstRow(); last = e.getLastRow(); col = e.getColumn(); count++;
        }
        boolean was(int t, int f, int l, int c) { return count > 0 && type == t && first == f && last == l && col == c; }
    }

    @SuppressWarnings("unchecked")
    static void testConstructors() {
        DefaultTableModel m = new DefaultTableModel();
        check(m.getRowCount() == 0 && m.getColumnCount() == 0, "empty");
        DefaultTableModel m2 = new DefaultTableModel(3, 2);
        check(m2.getRowCount() == 3 && m2.getColumnCount() == 2, "(rows, cols) ctor");
        check(m2.getValueAt(0, 0) == null, "(rows, cols) cells are null");
        DefaultTableModel m3 = new DefaultTableModel(new Object[]{"A"}, 2);
        check(m3.getRowCount() == 2 && m3.getColumnCount() == 1, "(Object[] ids, rows) ctor");
        check("A".equals(m3.getColumnName(0)), "(Object[] ids) column name");
        DefaultTableModel m4 = new DefaultTableModel(new Vector<Object>(java.util.Arrays.asList("V1", "V2")), 1);
        check(m4.getRowCount() == 1 && m4.getColumnCount() == 2, "(Vector ids, rows) ctor");
        check("V2".equals(m4.getColumnName(1)), "(Vector ids) column name");
        Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
        Vector<Object> r0 = new Vector<Object>(java.util.Arrays.asList((Object) "a", "b"));
        rows.add(r0);
        DefaultTableModel m5 = new DefaultTableModel(rows, new Vector<Object>(java.util.Arrays.asList((Object) "C0", "C1")));
        check(m5.getRowCount() == 1 && m5.getColumnCount() == 2, "(Vector, Vector) ctor");
        check("b".equals(m5.getValueAt(0, 1)), "(Vector, Vector) values");
        DefaultTableModel m6 = new DefaultTableModel(new Object[][]{{"x", "y"}}, new Object[]{"L", "R"});
        check(m6.getRowCount() == 1 && m6.getColumnCount() == 2, "(Object[][], Object[]) ctor");
        check("y".equals(m6.getValueAt(0, 1)) && "R".equals(m6.getColumnName(1)), "(Object[][], Object[]) values/names");
    }

    static void testRowOperationsAndEvents() {
        DefaultTableModel m = new DefaultTableModel(new Object[]{"C"}, 0);
        Recorder rec = new Recorder();
        m.addTableModelListener(rec);

        m.addRow(new Object[]{"v"});
        check(m.getRowCount() == 1 && "v".equals(m.getValueAt(0, 0)), "row");
        check(rec.was(TableModelEvent.INSERT, 0, 0, TableModelEvent.ALL_COLUMNS), "addRow event INSERT 0-0 all-columns");

        m.addRow(new Vector<Object>(java.util.Arrays.asList((Object) "w")));
        check(rec.was(TableModelEvent.INSERT, 1, 1, TableModelEvent.ALL_COLUMNS), "addRow(Vector) event INSERT 1-1");
        // addRow with short array pads missing trailing cells with null.
        m.addRow(new Object[]{});
        check(m.getRowCount() == 3 && m.getValueAt(2, 0) == null, "short addRow pads null");
        check(rec.was(TableModelEvent.INSERT, 2, 2, TableModelEvent.ALL_COLUMNS), "short addRow event");

        m.insertRow(1, new Object[]{"mid"});
        check("mid".equals(m.getValueAt(1, 0)), "insertRow position");
        check("v".equals(m.getValueAt(0, 0)) && "w".equals(m.getValueAt(2, 0)), "insertRow shifts");
        check(rec.was(TableModelEvent.INSERT, 1, 1, TableModelEvent.ALL_COLUMNS), "insertRow event");
        m.insertRow(m.getRowCount(), new Object[]{"end"});
        check("end".equals(m.getValueAt(m.getRowCount() - 1, 0)), "insertRow at rowCount");

        m.setValueAt("Z", 0, 0);
        check("Z".equals(m.getValueAt(0, 0)), "setValueAt round-trip");
        check(rec.was(TableModelEvent.UPDATE, 0, 0, 0), "setValueAt event UPDATE cell");

        m.removeRow(1);
        check("w".equals(m.getValueAt(1, 0)), "removeRow shifts up");
        check(rec.was(TableModelEvent.DELETE, 1, 1, TableModelEvent.ALL_COLUMNS), "removeRow event DELETE");

        // moveRow: single row to the end.
        DefaultTableModel mv = new DefaultTableModel(new Object[][]{{"A"}, {"B"}, {"C"}}, new Object[]{"X"});
        mv.moveRow(0, 0, 2);
        check("B".equals(mv.getValueAt(0, 0)) && "C".equals(mv.getValueAt(1, 0)) && "A".equals(mv.getValueAt(2, 0)),
                "moveRow(0,0,2) moves block to end");
        checkThrows(ArrayIndexOutOfBoundsException.class, "moveRow range too far",
                new ThrowingRunnable() { public void run() { mv.moveRow(0, 1, 2); } });

        // setRowCount grows with INSERT event for the new range, shrinks with DELETE.
        DefaultTableModel gr = new DefaultTableModel(new Object[]{"X"}, 3);
        Recorder rec2 = new Recorder();
        gr.addTableModelListener(rec2);
        gr.setRowCount(5);
        check(gr.getRowCount() == 5, "setRowCount grow");
        check(rec2.was(TableModelEvent.INSERT, 3, 4, TableModelEvent.ALL_COLUMNS), "setRowCount grow event");
        gr.setRowCount(2);
        check(gr.getRowCount() == 2, "setRowCount shrink");
        check(rec2.was(TableModelEvent.DELETE, 2, 4, TableModelEvent.ALL_COLUMNS), "setRowCount shrink event");
        check(gr.getValueAt(0, 0) == null, "grown cells are null");
        // setNumRows is the legacy spelling of setRowCount.
        gr.setNumRows(1);
        check(gr.getRowCount() == 1, "setNumRows legacy");

        // Boundary exception matrix.
        DefaultTableModel bm = new DefaultTableModel(2, 2);
        checkThrows(ArrayIndexOutOfBoundsException.class, "getValueAt row oob",
                new ThrowingRunnable() { public void run() { bm.getValueAt(9, 0); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "getValueAt col oob",
                new ThrowingRunnable() { public void run() { bm.getValueAt(0, 9); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "setValueAt row oob",
                new ThrowingRunnable() { public void run() { bm.setValueAt("x", 9, 0); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "setValueAt col oob",
                new ThrowingRunnable() { public void run() { bm.setValueAt("x", 0, 9); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "insertRow oob",
                new ThrowingRunnable() { public void run() { bm.insertRow(99, new Object[]{"a", "b"}); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "removeRow oob",
                new ThrowingRunnable() { public void run() { bm.removeRow(9); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "removeRow negative",
                new ThrowingRunnable() { public void run() { bm.removeRow(-1); } });
        check(bm.isCellEditable(0, 0), "DefaultTableModel cells are editable by default");
    }

    @SuppressWarnings("unchecked")
    static void testColumns() {
        DefaultTableModel m = new DefaultTableModel(1, 1);
        Recorder rec = new Recorder();
        m.addTableModelListener(rec);
        m.addColumn("C");
        check(m.getColumnCount() == 2, "addColumn");
        check(rec.was(TableModelEvent.UPDATE, TableModelEvent.HEADER_ROW, TableModelEvent.HEADER_ROW,
                TableModelEvent.ALL_COLUMNS), "addColumn event is header UPDATE");
        check("C".equals(m.getColumnName(1)), "addColumn identifier name");
        m.addColumn("D", new Object[]{"d"});
        check("d".equals(m.getValueAt(0, 2)), "addColumn with data");
        m.addColumn("E", new Vector<Object>(java.util.Arrays.asList((Object) "e")));
        check("e".equals(m.getValueAt(0, 3)), "addColumn with Vector data");
        m.addColumn(null);
        check(m.getColumnName(4) == null || "E".equals(m.getColumnName(3)), "null identifier accepted");

        DefaultTableModel named = new DefaultTableModel(new Object[]{"A", "B"}, 1);
        check("A".equals(named.getColumnName(0)) && "B".equals(named.getColumnName(1)), "identifiers used as names");
        named.setColumnIdentifiers(new Object[]{"X", "Y", "Z"});
        check(named.getColumnCount() == 3 && "Z".equals(named.getColumnName(2)), "setColumnIdentifiers(Object[])");
        named.setColumnIdentifiers(new Vector<Object>(java.util.Arrays.asList((Object) "P", "Q")));
        check(named.getColumnCount() == 2 && "Q".equals(named.getColumnName(1)), "setColumnIdentifiers(Vector)");
        named.setColumnCount(4);
        check(named.getColumnCount() == 4, "setColumnCount grow");
        check(named.getValueAt(0, 3) == null, "grown cells null");
        // Grown columns get spreadsheet-style names (identifier padding is null).
        check(!"A".equals(named.getColumnName(3)), "grown column name is not an old identifier");
        named.setColumnCount(1);
        check(named.getColumnCount() == 1, "setColumnCount shrink");
        // Fallback spreadsheet naming when no identifiers are set.
        DefaultTableModel anon = new DefaultTableModel(1, 0);
        anon.addColumn("A");
        anon.addColumn("B");
        check("A".equals(anon.getColumnName(0)), "explicit identifier wins");
        DefaultTableModel letters = new DefaultTableModel(1, 3);
        check("A".equals(letters.getColumnName(0)) && "B".equals(letters.getColumnName(1))
                && "C".equals(letters.getColumnName(2)), "spreadsheet letters for null identifiers");
    }

    @SuppressWarnings("unchecked")
    static void testDataVector() {
        DefaultTableModel m = new DefaultTableModel(new Object[][]{{"u"}, {"v"}, {"w"}}, new Object[]{"P"});
        // JDK-9+ declares getDataVector() as Vector<Vector> (JDK-8 returned raw Vector).
        @SuppressWarnings("unchecked")
        Vector<Vector> data = m.getDataVector();
        check(data.size() == 3, "getDataVector size");
        // getDataVector is a live view: mutations through it are visible in the model.
        ((Vector<Object>) data.get(0)).set(0, "ZZ");
        check("ZZ".equals(m.getValueAt(0, 0)), "dataVector mutation is live");
        // setDataVector swaps the whole storage and fires a structure (header) event.
        Recorder rec = new Recorder();
        m.addTableModelListener(rec);
        m.setDataVector(new Object[][]{{"n1", "n2"}}, new Object[]{"P", "Q"});
        check(rec.was(TableModelEvent.UPDATE, TableModelEvent.HEADER_ROW, TableModelEvent.HEADER_ROW,
                TableModelEvent.ALL_COLUMNS), "setDataVector fires header UPDATE");
        check(m.getRowCount() == 1 && m.getColumnCount() == 2, "setDataVector shape");
        check("n2".equals(m.getValueAt(0, 1)) && "Q".equals(m.getColumnName(1)), "setDataVector content");
        // setDataVector with short rows pads them to the identifier count.
        m.setDataVector(new Object[][]{{"only"}}, new Object[]{"P", "Q", "R"});
        check(m.getColumnCount() == 3 && m.getValueAt(0, 2) == null, "setDataVector pads short rows");
    }

    static void testSerialization() throws Exception {
        DefaultTableModel m = new DefaultTableModel(new Object[][]{{"a", "1"}, {"b", "2"}}, new Object[]{"L", "R"});
        m.setValueAt("Z", 0, 0);
        DefaultTableModel back = (DefaultTableModel) roundTrip(m);
        check(back.getRowCount() == 2 && back.getColumnCount() == 2, "round-trip shape");
        check("Z".equals(back.getValueAt(0, 0)), "round-trip value");
        check("b".equals(back.getValueAt(1, 0)), "round-trip value 2");
        check("R".equals(back.getColumnName(1)), "round-trip column name");
        back.addRow(new Object[]{"c", "3"});
        check(back.getRowCount() == 3 && m.getRowCount() == 2, "deserialized model still mutable");
        check(back.isCellEditable(0, 0), "round-trip editability");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testRowOperationsAndEvents();
        testColumns();
        testDataVector();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
