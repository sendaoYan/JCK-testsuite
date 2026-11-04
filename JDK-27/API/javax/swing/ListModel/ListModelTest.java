/*
 * @test
 * @summary javax.swing.ListModel conformance: white-box structure + black-box behavior.
 * @run main ListModelTest
 */

import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class ListModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ListModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        // supplemented structural assertions
        String[] mnames = {"getSize", "getElementAt", "addListDataListener", "removeListDataListener"};
        for (String m : mnames) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getMethods())
                if (mm.getName().equals(m)) { found = true; break; }
            check(found, "interface declares " + m);
        }
        check(c.getTypeParameters().length == 1, "generic parameter E");
        Class<?> def = Class.forName("javax.swing.DefaultListModel");
        check(def.getSuperclass().getName().equals("javax.swing.AbstractListModel"),
              "DefaultListModel extends AbstractListModel");
        check(!Modifier.isAbstract(def.getModifiers()), "DefaultListModel concrete");
    }

    // ---- recording listener ----
    static class Rec implements ListDataListener {
        int added, removed, changed;
        ListDataEvent lastAdded, lastRemoved, lastChanged;
        public void intervalAdded(ListDataEvent e) { added++; lastAdded = e; }
        public void intervalRemoved(ListDataEvent e) { removed++; lastRemoved = e; }
        public void contentsChanged(ListDataEvent e) { changed++; lastChanged = e; }
        void reset() { added = removed = changed = 0; lastAdded = lastRemoved = lastChanged = null; }
        static String type(ListDataEvent e) { return e == null ? "none" : String.valueOf(e.getType()); }
    }

    static void expectExact(Class<? extends Throwable> ex, String why, Runnable r) {
        try { r.run(); throw new AssertionError("expected " + ex.getSimpleName() + ": " + why); }
        catch (RuntimeException e) {
            if (ex.isInstance(e)) { passed++; return; }
            throw new AssertionError(why + ": wrong exception " + e);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // drive the interface through its reference implementation
        ListModel<String> lm = new DefaultListModel<>();
        DefaultListModel<String> m = (DefaultListModel<String>) lm;
        check(lm.getSize() == 0 && m.isEmpty(), "fresh model empty via both APIs");
        Rec rec = new Rec();
        ((DefaultListModel<String>) m).addListDataListener(rec);

        // build: addElement -> INTERVAL_ADDED(n, n) each time
        m.addElement("a");
        m.addElement("b");
        m.addElement("c");
        check(rec.added == 3 && rec.removed == 0 && rec.changed == 0, "3 addElement -> 3 intervalAdded");
        check(rec.lastAdded.getType() == ListDataEvent.INTERVAL_ADDED
              && rec.lastAdded.getIndex0() == 2 && rec.lastAdded.getIndex1() == 2,
              "addElement event INTERVAL_ADDED(2,2)");
        check(lm.getSize() == 3 && m.size() == 3, "size via interface and impl");
        check(lm.getElementAt(0).equals("a") && lm.getElementAt(2).equals("c"), "getElementAt order");

        // insert in the middle
        m.insertElementAt("x", 1);
        check(m.getElementAt(1).equals("x") && m.getElementAt(2).equals("b"), "insertElementAt shifts tail");
        check(rec.added == 4 && rec.lastAdded.getIndex0() == 1 && rec.lastAdded.getIndex1() == 1,
              "insert event INTERVAL_ADDED(1,1)");

        // set -> CONTENTS_CHANGED(i, i)
        String old = m.set(1, "y");
        check(old.equals("x") && m.getElementAt(1).equals("y"), "set returns old and stores new");
        check(rec.changed == 1 && rec.lastChanged.getType() == ListDataEvent.CONTENTS_CHANGED
              && rec.lastChanged.getIndex0() == 1 && rec.lastChanged.getIndex1() == 1,
              "set event CONTENTS_CHANGED(1,1)");
        m.setElementAt("z", 3);
        check(m.getElementAt(3).equals("z") && rec.changed == 2
              && rec.lastChanged.getIndex0() == 3, "setElementAt CONTENTS_CHANGED(3,3)");

        // queries
        check(m.firstElement().equals("a") && m.lastElement().equals("z"), "first/last element");
        check(m.indexOf("y") == 1 && m.indexOf("nope") == -1, "indexOf hit/miss");
        m.addElement("a");
        check(m.lastIndexOf("a") == 4 && m.indexOf("a") == 0, "lastIndexOf vs indexOf");
        check(m.indexOf("a", 1) == 4, "indexOf from index");
        check(m.contains("z") && !m.contains("q"), "contains");
        check(!m.isEmpty() && m.getSize() == 5, "size 5");
        Object[] arr = m.toArray();
        check(arr.length == 5 && arr[0].equals("a") && arr[4].equals("a"), "toArray content order");
        StringBuilder order = new StringBuilder();
        for (Enumeration<String> e = m.elements(); e.hasMoreElements(); ) order.append(e.nextElement());
        check(order.toString().equals("aybza"), "elements() enumeration in insertion order");

        // remove single: INTERVAL_REMOVED(i, i)
        check(m.removeElement("y"), "removeElement returns true when present");
        check(rec.removed == 1 && rec.lastRemoved.getType() == ListDataEvent.INTERVAL_REMOVED
              && rec.lastRemoved.getIndex0() == 1 && rec.lastRemoved.getIndex1() == 1,
              "removeElement event INTERVAL_REMOVED(1,1)");
        check(!m.removeElement("nope") && rec.removed == 1, "removeElement absent: false, no event");
        String got = m.remove(2);
        // content now [a, b, a] after removing "y" then index 2 ("z")
        check(got.equals("z"), "remove(int) returns element");
        check(rec.removed == 2 && rec.lastRemoved.getIndex0() == 2, "remove(int) event (2,2)");

        // removeRange inclusive
        rec.reset();
        m.addElement("p0");
        m.addElement("p1"); // [a, b, a, p0, p1]
        check(m.size() == 5, "size 5 before removeRange");
        m.removeRange(1, 3);
        check(m.size() == 2 && m.getElementAt(0).equals("a") && m.getElementAt(1).equals("p1"),
              "removeRange(1,3) inclusive");
        check(rec.removed == 1 && rec.lastRemoved.getIndex0() == 1 && rec.lastRemoved.getIndex1() == 3,
              "removeRange event INTERVAL_REMOVED(1,3)");
        expectExact(IllegalArgumentException.class, "removeRange from>to", () -> m.removeRange(3, 1));

        // clear / removeAllElements -> INTERVAL_REMOVED(0, size-1)
        rec.reset();
        m.removeAllElements();
        check(m.size() == 0 && m.isEmpty() && lm.getSize() == 0, "removeAllElements empties");
        check(rec.removed == 1 && rec.lastRemoved.getIndex0() == 0 && rec.lastRemoved.getIndex1() == 1,
              "removeAllElements event INTERVAL_REMOVED(0,1)");
        m.addElement("t1"); m.addElement("t2");
        m.clear();
        check(m.size() == 0, "clear empties");
        check(rec.removed == 2, "removeAllElements + clear each fire one INTERVAL_REMOVED");

        // capacity growth keeps behavior; ensureCapacity explicit
        DefaultListModel<Integer> big = new DefaultListModel<>();
        int cap0 = big.capacity();
        big.ensureCapacity(100);
        check(big.capacity() >= 100 && big.capacity() >= cap0, "ensureCapacity grows");
        for (int i = 0; i < 250; i++) big.addElement(i);
        check(big.size() == 250 && big.capacity() >= 250, "capacity grows past default while iterating value chain");
        check(big.getElementAt(249) == 249, "values intact after growth");

        // precise exception types on empty/out-of-range
        DefaultListModel<String> empty = new DefaultListModel<>();
        expectExact(ArrayIndexOutOfBoundsException.class, "getElementAt(0) on empty", () -> empty.getElementAt(0));
        expectExact(ArrayIndexOutOfBoundsException.class, "elementAt(-1)", () -> empty.elementAt(-1));
        expectExact(ArrayIndexOutOfBoundsException.class, "removeElementAt(0) on empty", () -> empty.removeElementAt(0));
        expectExact(ArrayIndexOutOfBoundsException.class, "insertElementAt beyond size", () -> empty.insertElementAt("e", 1));
        expectExact(ArrayIndexOutOfBoundsException.class, "get(size)", () -> m.get(0));
        expectExact(ArrayIndexOutOfBoundsException.class, "set(size,..)", () -> m.set(0, "v"));
        expectExact(NoSuchElementException.class, "firstElement empty", () -> empty.firstElement());
        expectExact(NoSuchElementException.class, "lastElement empty", () -> empty.lastElement());
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
