/*
 * @test
 * @summary javax.swing.DefaultListSelectionModel conformance: selection intervals
 *          (headless-safe): defaults, set/add/remove interval semantics including
 *          inverted intervals, all three selection modes with their truncation
 *          rules, anchor/lead tracking, value-is-adjusting, insert/remove index
 *          interval shifts, exact ListSelectionEvent payloads, exception matrix,
 *          serialization round-trip.
 * @run main DefaultListSelectionModelTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class DefaultListSelectionModelTest {
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

    static class L implements ListSelectionListener {
        int count;
        ListSelectionEvent last;
        public void valueChanged(ListSelectionEvent e) { count++; last = e; }
    }

    static String selstr(ListSelectionModel m) {
        if (m.isSelectionEmpty()) return "empty";
        StringBuilder sb = new StringBuilder();
        for (int i = m.getMinSelectionIndex(); i <= m.getMaxSelectionIndex(); i++)
            if (m.isSelectedIndex(i)) sb.append(i).append(",");
        return sb.toString();
    }

    static int selCount(ListSelectionModel m) {
        int n = 0;
        for (int i = m.getMinSelectionIndex(); i >= 0 && i <= m.getMaxSelectionIndex(); i++)
            if (m.isSelectedIndex(i)) n++;
        return n;
    }

    public static void main(String[] args) throws Exception {
        testDefaultsAndIntervals();
        testModes();
        testAnchorLead();
        testAdjusting();
        testInsertRemoveIntervals();
        testEventsAndExceptions();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testDefaultsAndIntervals() {
        DefaultListSelectionModel m = new DefaultListSelectionModel();
        check(m.getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, "default mode multiple interval");
        check(m.isSelectionEmpty(), "empty by default");
        check(m.getMinSelectionIndex() == -1 && m.getMaxSelectionIndex() == -1, "bounds -1 when empty");
        check(m.getAnchorSelectionIndex() == -1 && m.getLeadSelectionIndex() == -1, "anchor/lead -1 when empty");
        check(!m.getValueIsAdjusting(), "not adjusting by default");
        check(!m.isSelectedIndex(0) && !m.isSelectedIndex(-1), "nothing selected, negative is false");
        check(m.getListSelectionListeners().length == 0, "no listeners");

        m.setSelectionInterval(2, 4);
        check(m.getMinSelectionIndex() == 2 && m.getMaxSelectionIndex() == 4, "interval bounds");
        check(m.isSelectedIndex(2) && m.isSelectedIndex(3) && m.isSelectedIndex(4), "inclusive interval");
        check(!m.isSelectedIndex(1) && !m.isSelectedIndex(5), "outside interval not selected");
        check(m.isSelectedIndex(-1) == false, "negative index false");
        // set replaces, does not accumulate.
        m.setSelectionInterval(6, 6);
        check(selstr(m).equals("6,"), "setSelectionInterval replaces");

        m.clearSelection();
        check(m.isSelectionEmpty(), "clearSelection");
        m.setSelectionInterval(0, 0);
        m.clearSelection();
        check(m.getMinSelectionIndex() == -1, "bounds reset after clear");

        // Inverted interval is normalized.
        m.clearSelection();
        m.setSelectionInterval(5, 2);
        check(m.getMinSelectionIndex() == 2 && m.getMaxSelectionIndex() == 5, "inverted set normalized");
        check(selCount(m) == 4, "inverted set selects 4");

        // add accumulates; remove punches holes.
        DefaultListSelectionModel m2 = new DefaultListSelectionModel();
        m2.setSelectionInterval(2, 4);
        m2.addSelectionInterval(6, 8);
        check(m2.getMinSelectionIndex() == 2 && m2.getMaxSelectionIndex() == 8, "add extends bounds");
        check(selCount(m2) == 6, "add accumulates");
        m2.removeSelectionInterval(3, 7);
        check(selstr(m2).equals("2,8,"), "remove punches a hole: " + selstr(m2));
        // removing outside the selection is a no-op for the selection
        m2.removeSelectionInterval(20, 25);
        check(selstr(m2).equals("2,8,"), "remove outside selection is a no-op");
        // inverted remove interval is normalized too
        DefaultListSelectionModel m3 = new DefaultListSelectionModel();
        m3.setSelectionInterval(0, 9);
        m3.removeSelectionInterval(4, 2);
        check(selstr(m3).equals("0,1,5,6,7,8,9,"), "inverted remove normalized: " + selstr(m3));
    }

    static void testModes() {
        // SINGLE_SELECTION: only the second endpoint survives.
        DefaultListSelectionModel s = new DefaultListSelectionModel();
        s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        check(s.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION, "single mode stored");
        s.setSelectionInterval(1, 3);
        check(selstr(s).equals("3,"), "SINGLE: interval set keeps one index: " + selstr(s));
        s.setSelectionInterval(6, 8);
        check(selstr(s).equals("8,"), "SINGLE: reset keeps one index");
        s.addSelectionInterval(1, 2);
        check(selstr(s).equals("2,"), "SINGLE: add replaces with one index");

        // SINGLE_INTERVAL_SELECTION: gaps are never created.
        DefaultListSelectionModel si = new DefaultListSelectionModel();
        si.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        si.setSelectionInterval(2, 3);
        si.addSelectionInterval(6, 7);
        check(selstr(si).equals("6,7,"), "SINGLE_INTERVAL: non-adjacent add discards old: " + selstr(si));
        si.setSelectionInterval(2, 3);
        si.addSelectionInterval(4, 5);
        check(selstr(si).equals("2,3,4,5,"), "SINGLE_INTERVAL: adjacent add extends");

        checkThrows(IllegalArgumentException.class, "setSelectionMode(99)",
                new ThrowingRunnable() { public void run() {
                    new DefaultListSelectionModel().setSelectionMode(99); } });
    }

    static void testAnchorLead() {
        DefaultListSelectionModel m = new DefaultListSelectionModel();
        m.setSelectionInterval(1, 3);
        check(m.getAnchorSelectionIndex() == 1 && m.getLeadSelectionIndex() == 3, "set stores anchor/lead");
        m.addSelectionInterval(6, 8);
        check(m.getAnchorSelectionIndex() == 6 && m.getLeadSelectionIndex() == 8, "add moves anchor/lead");
        m.setAnchorSelectionIndex(0);
        check(m.getAnchorSelectionIndex() == 0, "setAnchorSelectionIndex");
        check(m.getLeadSelectionIndex() == 8, "setAnchor leaves lead");
        m.setLeadSelectionIndex(2);
        check(m.getLeadSelectionIndex() == 2, "setLeadSelectionIndex");
        // setLead performs a deselect gesture over [anchor..lead]: 1 and 2 drop out.
        check(!m.isSelectedIndex(1) && !m.isSelectedIndex(2), "anchor..lead deselected by gesture: " + selstr(m));
        check(m.isSelectedIndex(6) && m.isSelectedIndex(8), "selection beyond the gesture survives");
    }

    static void testAdjusting() {
        DefaultListSelectionModel m = new DefaultListSelectionModel();
        L l = new L();
        m.addListSelectionListener(l);
        m.setValueIsAdjusting(true);
        check(m.getValueIsAdjusting(), "adjusting flag round-trip");
        check(l.count == 0, "toggling adjusting alone fires nothing");
        m.setSelectionInterval(0, 1);
        check(l.count == 1 && l.last.getValueIsAdjusting(), "events during gesture carry adjusting=true");
        m.setValueIsAdjusting(false);
        check(!m.getValueIsAdjusting(), "gesture ends");
        m.setSelectionInterval(3, 3);
        check(!l.last.getValueIsAdjusting(), "events outside gesture carry adjusting=false");
    }

    static void testInsertRemoveIntervals() {
        // insertIndexInterval(index, length, before): selection at/after the
        // insertion point is shifted right; inserted indices become selected
        // when the insertion point was selected.
        DefaultListSelectionModel a = new DefaultListSelectionModel();
        a.setSelectionInterval(3, 5);
        a.insertIndexInterval(3, 2, true);
        check(selstr(a).equals("3,4,5,6,7,"), "insert before selected start grows selection: " + selstr(a));
        check(a.getLeadSelectionIndex() == 7, "lead shifted by insert");
        DefaultListSelectionModel b = new DefaultListSelectionModel();
        b.setSelectionInterval(3, 5);
        b.insertIndexInterval(3, 2, false);
        check(selstr(b).equals("3,4,5,6,7,"), "insert after selected start grows selection too: " + selstr(b));
        check(b.getAnchorSelectionIndex() == 3, "anchor after insert-after");

        // Inserting before the selection purely shifts it.
        DefaultListSelectionModel c = new DefaultListSelectionModel();
        c.setSelectionInterval(5, 5);
        c.insertIndexInterval(0, 2, true);
        check(selstr(c).equals("7,"), "insert before selection shifts it: " + selstr(c));

        // removeIndexInterval(index0, index1) removes the model interval and
        // shifts later indices down.
        DefaultListSelectionModel d = new DefaultListSelectionModel();
        d.setSelectionInterval(3, 5);
        d.removeIndexInterval(4, 2);
        check(selstr(d).equals("2,"), "remove interval shifts tail down: " + selstr(d));
        check(d.getMaxSelectionIndex() == 2, "max after removal");
    }

    static void testEventsAndExceptions() {
        DefaultListSelectionModel m = new DefaultListSelectionModel();
        L l = new L();
        m.addListSelectionListener(l);
        m.setSelectionInterval(2, 4);
        check(l.count == 1, "one event per set");
        check(l.last.getFirstIndex() == 2 && l.last.getLastIndex() == 4, "event bounds match interval");
        check(l.last.getSource() == m, "event source is the model");
        m.addSelectionInterval(6, 8);
        check(l.count == 2 && l.last.getFirstIndex() == 2 && l.last.getLastIndex() == 8,
                "add event spans old and new bounds");
        // Re-setting the same interval fires nothing.
        m.setSelectionInterval(6, 8);
        check(l.count == 3, "replacing selection fires");
        m.setSelectionInterval(6, 8);
        check(l.count == 3, "identical re-selection fires nothing");
        m.clearSelection();
        check(l.count == 4, "clearing non-empty fires");
        m.clearSelection();
        check(l.count == 4, "clearing empty fires nothing");

        check(m.getListSelectionListeners().length == 1, "one listener registered");
        m.removeListSelectionListener(l);
        check(m.getListSelectionListeners().length == 0, "listener removed");
        m.setSelectionInterval(0, 1);
        check(l.count == 4, "removed listener not notified");
        check(m.getListeners(ListSelectionListener.class).length == 0, "getListeners empty");

        // Out-of-range indices.
        final DefaultListSelectionModel m2 = new DefaultListSelectionModel();
        checkThrows(IndexOutOfBoundsException.class, "insertIndexInterval negative index",
                new ThrowingRunnable() { public void run() { m2.insertIndexInterval(-4, 2, true); } });
        checkThrows(IndexOutOfBoundsException.class, "removeIndexInterval negative index",
                new ThrowingRunnable() { public void run() { m2.removeIndexInterval(-4, 2); } });
        // Negative interval bounds are not clamped: BitSet rejects them.
        checkThrows(IndexOutOfBoundsException.class, "setSelectionInterval(-5, 3)",
                new ThrowingRunnable() { public void run() { m2.setSelectionInterval(-5, 3); } });
        // Queries with out-of-range arguments just report false.
        check(!m2.isSelectedIndex(-5), "isSelectedIndex negative false");
        check(!m2.isSelectedIndex(Integer.MAX_VALUE), "isSelectedIndex huge false");
    }

    static void testSerialization() throws Exception {
        DefaultListSelectionModel m = new DefaultListSelectionModel();
        m.setSelectionInterval(1, 3);
        m.addSelectionInterval(7, 8);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(m);
        oos.close();
        DefaultListSelectionModel back = (DefaultListSelectionModel)
                new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(selstr(back).equals("1,2,3,7,8,"), "round-trip selection: " + selstr(back));
        check(back.getLeadSelectionIndex() == 8 && back.getAnchorSelectionIndex() == 7, "round-trip anchor/lead");
        check(back.getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, "round-trip mode");
        back.setSelectionInterval(0, 0);
        check(selstr(back).equals("0,"), "deserialized model still mutable");
        check(selstr(m).equals("1,2,3,7,8,"), "original untouched");
    }
}
