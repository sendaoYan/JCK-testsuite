/*
 * @test
 * @summary javax.swing.table.TableColumn conformance: column props (headless-safe):
 *          constructor defaults, width clamping invariants, identifier/headerValue
 *          interplay, property-change events, serialization round-trip.
 * @run main TableColumnTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.table.TableColumn;

public class TableColumnTest {
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

    static class Recorder implements PropertyChangeListener {
        String lastProperty;
        Object oldV, newV;
        int count;
        public void propertyChange(PropertyChangeEvent e) { lastProperty = e.getPropertyName(); oldV = e.getOldValue(); newV = e.getNewValue(); count++; }
    }

    static void testConstructorDefaults() {
        TableColumn c = new TableColumn();
        check(c.getModelIndex() == 0, "default modelIndex 0");
        check(c.getWidth() == 75, "default width 75");
        check(c.getMinWidth() == 15, "default minWidth 15");
        check(c.getMaxWidth() == Integer.MAX_VALUE, "default maxWidth Integer.MAX_VALUE");
        check(c.getPreferredWidth() == 75, "default preferredWidth 75");
        check(c.getIdentifier() == null, "default identifier null");
        check(c.getHeaderValue() == null, "default headerValue null");
        check(c.getResizable(), "default resizable true");
        check(c.getHeaderRenderer() == null, "default headerRenderer null");
        check(c.getCellRenderer() == null, "default cellRenderer null");
        check(c.getCellEditor() == null, "default cellEditor null");

        TableColumn c2 = new TableColumn(3);
        check(c2.getModelIndex() == 3, "ctor modelIndex 3");
        check(c2.getIdentifier() == null, "ctor identifier null when no headerValue");

        TableColumn c3 = new TableColumn(4, 90);
        check(c3.getModelIndex() == 4 && c3.getPreferredWidth() == 90, "ctor (modelIndex, width)");
        check(c3.getWidth() == 90, "ctor (modelIndex, width) also sets current width");
    }

    static void testIdentifierHeaderInterplay() {
        TableColumn c = new TableColumn(0);
        c.setHeaderValue("H");
        check("H".equals(c.getHeaderValue()), "headerValue round-trip");
        // getIdentifier falls back to headerValue until setIdentifier is used.
        check("H".equals(c.getIdentifier()), "identifier falls back to headerValue");
        c.setIdentifier("ID1");
        check("ID1".equals(c.getIdentifier()), "identifier set");
        check("H".equals(c.getHeaderValue()), "headerValue unaffected by setIdentifier");
        c.setIdentifier(null);
        check("H".equals(c.getIdentifier()), "null identifier falls back to headerValue dynamically");
        // identifier is an arbitrary Object, not only String
        c.setIdentifier(Integer.valueOf(7));
        check(Integer.valueOf(7).equals(c.getIdentifier()), "arbitrary identifier object");
        c.setHeaderValue(null);
        check(c.getHeaderValue() == null, "headerValue cleared");
    }

    static void testWidthClamping() {
        // Width is always clamped to [minWidth, maxWidth].
        TableColumn c = new TableColumn(0);
        c.setWidth(-1);
        check(c.getWidth() == 15, "setWidth(-1) clamps to minWidth");
        c.setWidth(Integer.MAX_VALUE);
        check(c.getWidth() == Integer.MAX_VALUE, "setWidth(MAX) accepted when max is MAX");
        c.setPreferredWidth(-5);
        check(c.getPreferredWidth() == 15, "setPreferredWidth(-5) clamps to minWidth");
        c.setPreferredWidth(Integer.MAX_VALUE);
        check(c.getPreferredWidth() == Integer.MAX_VALUE, "setPreferredWidth(MAX) accepted");

        // minWidth clamps to [0, maxWidth]; raising minWidth pushes width and preferred.
        TableColumn d = new TableColumn(0);
        d.setMinWidth(-7);
        check(d.getMinWidth() == 0, "setMinWidth(-7) clamps to 0");
        d.setMinWidth(30);
        check(d.getMinWidth() == 30, "setMinWidth(30)");
        check(d.getWidth() == 75 && d.getPreferredWidth() == 75, "width/pref unchanged while inside new range");
        d.setMinWidth(90);
        check(d.getWidth() == 90 && d.getPreferredWidth() == 90, "raising minWidth above width lifts both");

        // maxWidth below minWidth pulls everything down to the (smaller) upper bound.
        TableColumn e = new TableColumn(0);
        e.setMaxWidth(10);
        check(e.getMaxWidth() == 15, "setMaxWidth(10) clamps up to minWidth");
        check(e.getWidth() == 15 && e.getPreferredWidth() == 15, "width/pref pinned at 15");
        TableColumn f = new TableColumn(0);
        f.setMinWidth(30);
        f.setMaxWidth(40);
        check(f.getMinWidth() == 30 && f.getMaxWidth() == 40, "disjoint-but-consistent min/max");
        f.setWidth(999);
        check(f.getWidth() == 40, "setWidth clamps to maxWidth");
        f.setPreferredWidth(1);
        check(f.getPreferredWidth() == 30, "setPreferredWidth clamps to minWidth");
        // max clamps down to min
        TableColumn g = new TableColumn(0);
        g.setMinWidth(30);
        g.setMaxWidth(20);
        check(g.getMaxWidth() == 30, "setMaxWidth below min clamps to min");
    }

    static void testPropertyConstantsAndEvents() {
        // Documented (historically misspelled) property name constants.
        check("columWidth".equals(TableColumn.COLUMN_WIDTH_PROPERTY), "COLUMN_WIDTH_PROPERTY is 'columWidth'");
        check("headerValue".equals(TableColumn.HEADER_VALUE_PROPERTY), "HEADER_VALUE_PROPERTY is 'headerValue'");
        check("headerRenderer".equals(TableColumn.HEADER_RENDERER_PROPERTY), "HEADER_RENDERER_PROPERTY");
        check("cellRenderer".equals(TableColumn.CELL_RENDERER_PROPERTY), "CELL_RENDERER_PROPERTY");

        TableColumn c = new TableColumn(0);
        Recorder r = new Recorder();
        c.addPropertyChangeListener(r);
        c.setWidth(80);
        check(r.count == 1 && "width".equals(r.lastProperty), "width property event");
        check(((Integer) r.newV).intValue() == 80, "width event new value");
        c.setModelIndex(5);
        check("modelIndex".equals(r.lastProperty), "modelIndex property event");
        c.setHeaderValue("hv");
        check("headerValue".equals(r.lastProperty), "headerValue property event");
        c.setIdentifier("id");
        check("identifier".equals(r.lastProperty), "identifier property event");
        c.setMinWidth(20);
        check("minWidth".equals(r.lastProperty), "minWidth property event");
        c.setMaxWidth(500);
        check("maxWidth".equals(r.lastProperty), "maxWidth property event");
        c.setPreferredWidth(90);
        check("preferredWidth".equals(r.lastProperty), "preferredWidth property event");
        c.setResizable(false);
        check("isResizable".equals(r.lastProperty), "isResizable property event");
        // Setting the same value again must not fire.
        int before = r.count;
        c.setResizable(false);
        check(r.count == before, "no event when value unchanged");

        // Listener registry.
        c.removePropertyChangeListener(r);
        int after = r.count;
        c.setWidth(33);
        check(r.count == after, "removed listener not notified");
        check(c.getPropertyChangeListeners().length == 0, "no listeners left");
        c.addPropertyChangeListener(r);
        check(c.getPropertyChangeListeners().length == 1, "listener re-added");
    }

    static void testObsoleteAndMisc() {
        TableColumn c = new TableColumn(0);
        // Obsolete no-ops must not throw.
        c.disableResizedPosting();
        c.enableResizedPosting();
        check(true, "resized posting toggles are no-ops");
        // sizeWidthToFit with no header renderer is documented to do nothing.
        c.sizeWidthToFit();
        check(c.getWidth() == 75, "sizeWidthToFit without renderer leaves width");
        c.setWidth(75);
        c.sizeWidthToFit();
        check(true, "sizeWidthToFit does not throw headless");
        c.setModelIndex(-3);
        check(c.getModelIndex() == -3, "negative modelIndex stored verbatim (view mapping is the model's concern)");
    }

    static void testSerialization() throws Exception {
        TableColumn c = new TableColumn(9, 123);
        c.setHeaderValue("head");
        c.setIdentifier("ident");
        c.setMinWidth(20);
        c.setMaxWidth(400);
        c.setWidth(100);
        c.setResizable(false);
        TableColumn back = (TableColumn) roundTrip(c);
        check(back.getModelIndex() == 9, "round-trip modelIndex");
        check(back.getPreferredWidth() == 123, "round-trip preferredWidth");
        check("head".equals(back.getHeaderValue()), "round-trip headerValue");
        check("ident".equals(back.getIdentifier()), "round-trip identifier");
        check(back.getMinWidth() == 20, "round-trip minWidth");
        check(back.getMaxWidth() == 400, "round-trip maxWidth");
        check(back.getWidth() == 100, "round-trip width");
        check(!back.getResizable(), "round-trip resizable");
        // Mutating the copy must not touch the original.
        back.setWidth(55);
        check(c.getWidth() == 100, "original untouched by deserialized copy");
    }

    public static void main(String[] args) throws Exception {
        testConstructorDefaults();
        testIdentifierHeaderInterplay();
        testWidthClamping();
        testPropertyConstantsAndEvents();
        testObsoleteAndMisc();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
