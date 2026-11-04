/*
 * @test
 * @summary java.beans.PropertyChangeSupport conformance: full contract.
 *          null source -> NPE, null-listener / null-name no-op semantics,
 *          global vs named listener registration (named wrapped in proxy),
 *          exact fire/no-fire conditions for Object (old==new non-null not
 *          fired; any null fires), int/boolean (only when different),
 *          firePropertyChange(PropertyChangeEvent) condition, indexed
 *          fire methods (Object/int/boolean) and IndexedPropertyChangeEvent
 *          type, source propagation, remove stops delivery, and returned
 *          listener arrays are defensive copies.
 * @run main PropertyChangeSupportTest
 */

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.beans.PropertyChangeSupport;

public class PropertyChangeSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static class Counting implements PropertyChangeListener {
        int count;
        PropertyChangeEvent last;
        public void propertyChange(PropertyChangeEvent e) { count++; last = e; }
    }

    static void testConstructorAndNoOp() {
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() {
                    new PropertyChangeSupport(null);
                }}, "null source -> NPE");

        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        check(!pcs.hasListeners("foo") && !pcs.hasListeners(null), "no listeners initially");
        pcs.addPropertyChangeListener((PropertyChangeListener) null);
        check(pcs.getPropertyChangeListeners().length == 0, "addPropertyChangeListener(null) no-op");
        pcs.addPropertyChangeListener("foo", (PropertyChangeListener) null);
        check(pcs.getPropertyChangeListeners("foo").length == 0, "addPropertyChangeListener(foo,null) no-op");
        pcs.addPropertyChangeListener((String) null, new Counting());
        check(pcs.getPropertyChangeListeners().length == 0, "addPropertyChangeListener(null name) no-op");
        check(pcs.getPropertyChangeListeners(null).length == 0, "getPropertyChangeListeners(null) empty");
    }

    static void testGlobalVsNamed() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        Counting global = new Counting();
        Counting named = new Counting();
        pcs.addPropertyChangeListener("foo", named);
        check(pcs.hasListeners("foo"), "hasListeners foo after named add");
        check(!pcs.hasListeners("bar"), "hasListeners bar false with only named foo listener");

        pcs.addPropertyChangeListener(global);
        check(pcs.hasListeners("bar"), "global listener counts for any name");
        check(pcs.getPropertyChangeListeners("foo").length == 1, "named listener count");
        PropertyChangeListener[] all = pcs.getPropertyChangeListeners();
        check(all.length == 2, "global + named-proxy count");
        boolean sawProxy = false;
        for (PropertyChangeListener l : all) {
            if (l instanceof PropertyChangeListenerProxy) {
                PropertyChangeListenerProxy p = (PropertyChangeListenerProxy) l;
                if ("foo".equals(p.getPropertyName())) sawProxy = true;
            }
        }
        check(sawProxy, "named listener wrapped in proxy with property name");

        pcs.firePropertyChange("foo", "a", "b");
        check(global.count == 1 && named.count == 1, "fire foo hits global + named");
        check("foo".equals(global.last.getPropertyName()), "event property name");
        check(src == global.last.getSource(), "event source is constructor source");

        pcs.firePropertyChange("bar", 1, 2);
        check(global.count == 2 && named.count == 1, "fire bar only hits global");

        pcs.removePropertyChangeListener("foo", named);
        check(pcs.getPropertyChangeListeners("foo").length == 0, "named removed");
        pcs.firePropertyChange("foo", "x", "y");
        check(global.count == 3 && named.count == 1, "removed named no longer fired");

        pcs.removePropertyChangeListener(global);
        pcs.firePropertyChange("foo", "x", "y");
        check(global.count == 3, "removed global no longer fired");
    }

    static void testFireConditions() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        Counting c = new Counting();
        pcs.addPropertyChangeListener(c);

        pcs.firePropertyChange("p", "a", "a"); // equal non-null -> no fire
        check(c.count == 0, "equal non-null objects not fired");
        pcs.firePropertyChange("p", null, null); // both null -> fires
        check(c.count == 1, "both null fires");
        pcs.firePropertyChange("p", null, "a");
        check(c.count == 2, "old null fires");
        pcs.firePropertyChange("p", "a", null);
        check(c.count == 3, "new null fires");

        pcs.firePropertyChange("p", 5, 5); // equal int -> no fire
        check(c.count == 3, "equal int not fired");
        pcs.firePropertyChange("p", 5, 6);
        check(c.count == 4, "different int fired");
        check(Integer.valueOf(5).equals(c.last.getOldValue()) && Integer.valueOf(6).equals(c.last.getNewValue()),
                "int boxed old/new values");

        pcs.firePropertyChange("p", true, true); // equal boolean -> no fire
        check(c.count == 4, "equal boolean not fired");
        pcs.firePropertyChange("p", false, true);
        check(c.count == 5, "different boolean fired");
    }

    static void testFireEvent() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        Counting c = new Counting();
        pcs.addPropertyChangeListener(c);

        pcs.firePropertyChange(new PropertyChangeEvent(src, "z", "old", "new"));
        check(c.count == 1, "firePropertyChange(event) fires");
        check("z".equals(c.last.getPropertyName()), "event name");

        pcs.firePropertyChange(new PropertyChangeEvent(src, "z", "same", "same"));
        check(c.count == 1, "equal non-null event not fired");

        pcs.firePropertyChange(new PropertyChangeEvent(src, "z", null, "v"));
        check(c.count == 2, "null-old event fires");
    }

    static void testIndexedFire() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        Counting c = new Counting();
        pcs.addPropertyChangeListener(c);

        pcs.fireIndexedPropertyChange("p", 3, "x", "y");
        check(c.count == 1, "indexed object fired");
        check(c.last instanceof IndexedPropertyChangeEvent, "event is IndexedPropertyChangeEvent");
        check(((IndexedPropertyChangeEvent) c.last).getIndex() == 3, "index value");
        check("x".equals(c.last.getOldValue()) && "y".equals(c.last.getNewValue()), "indexed old/new");

        pcs.fireIndexedPropertyChange("p", 0, 1, 1); // equal int -> no fire
        check(c.count == 1, "equal indexed int not fired");
        pcs.fireIndexedPropertyChange("p", 0, 1, 2);
        check(c.count == 2, "different indexed int fired");

        pcs.fireIndexedPropertyChange("p", 0, true, true); // equal boolean -> no fire
        check(c.count == 2, "equal indexed boolean not fired");
        pcs.fireIndexedPropertyChange("p", 0, false, true);
        check(c.count == 3, "different indexed boolean fired");
    }

    static void testDefensiveArrays() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        pcs.addPropertyChangeListener(new Counting());
        PropertyChangeListener[] a = pcs.getPropertyChangeListeners();
        a[0] = null;
        check(pcs.getPropertyChangeListeners().length == 1, "getPropertyChangeListeners returns copy");
    }


    static void testStructure() {
        check(java.io.Serializable.class.isAssignableFrom(PropertyChangeSupport.class), "implements Serializable");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testConstructorAndNoOp();
        testGlobalVsNamed();
        testFireConditions();
        testFireEvent();
        testIndexedFire();
        testDefensiveArrays();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
