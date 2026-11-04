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
 *          Deepening: serialization round-trip (serializable listeners
 *          survive as fresh copies, non-serializable ones dropped,
 *          non-serializable source -> NotSerializableException), duplicate
 *          registration of the same listener (fires twice, each remove
 *          removes one registration), registration-order notification
 *          (globals first then named), listener RuntimeException propagates
 *          and skips later listeners, identity-same-but-equals-false values
 *          still fire, null property name reaches globals only,
 *          firePropertyChange(null event) -> NPE, hasListeners(null) counts
 *          global listeners only, proxy details, and remove no-ops.
 * @run main PropertyChangeSupportTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // ===== appended: contract deepening (round A) =====

    /** Serializable listener that survives PropertyChangeSupport serialization. */
    static class SerialListener implements PropertyChangeListener, Serializable {
        private static final long serialVersionUID = 1L;
        int count;
        PropertyChangeEvent last;
        public void propertyChange(PropertyChangeEvent e) { count++; last = e; }
    }

    /** Non-serializable listener: silently dropped by serialization. */
    static class PlainListener implements PropertyChangeListener {
        int count;
        public void propertyChange(PropertyChangeEvent e) { count++; }
    }

    /** Serializable source bean with value equality. */
    public static class SerialSource implements Serializable {
        private static final long serialVersionUID = 1L;
        int v;
        SerialSource(int v) { this.v = v; }
        public boolean equals(Object o) { return o instanceof SerialSource && ((SerialSource) o).v == v; }
        public int hashCode() { return v; }
    }

    static Object roundtrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object r = ois.readObject();
        ois.close();
        return r;
    }

    static void testSerializationRoundTrip() throws Exception {
        SerialSource src = new SerialSource(7);
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        SerialListener serGlobal = new SerialListener();
        PlainListener plainGlobal = new PlainListener();
        SerialListener serNamed = new SerialListener();
        PlainListener plainNamed = new PlainListener();
        pcs.addPropertyChangeListener(serGlobal);
        pcs.addPropertyChangeListener(plainGlobal);
        pcs.addPropertyChangeListener("foo", serNamed);
        pcs.addPropertyChangeListener("foo", plainNamed);

        PropertyChangeSupport rt = (PropertyChangeSupport) roundtrip(pcs);
        check(rt.getPropertyChangeListeners().length == 2,
                "serialization keeps serializable listeners and drops non-serializable ones");
        check(rt.getPropertyChangeListeners("foo").length == 1,
                "only the serializable named listener survives serialization");
        PropertyChangeListener rtGlobal = null;
        boolean sawProxy = false;
        for (PropertyChangeListener l : rt.getPropertyChangeListeners()) {
            if (l instanceof PropertyChangeListenerProxy) {
                check("foo".equals(((PropertyChangeListenerProxy) l).getPropertyName()),
                        "round-tripped named listener still wrapped in a proxy");
                sawProxy = true;
            } else {
                rtGlobal = l;
            }
        }
        check(sawProxy && rtGlobal != null, "round-trip keeps one global listener and one named proxy");
        check(rtGlobal instanceof SerialListener,
                "round-tripped global listener is a fresh copy of the same listener class");

        SerialListener rtNamed = (SerialListener) rt.getPropertyChangeListeners("foo")[0];
        rt.firePropertyChange("foo", "a", "b");
        check(((SerialListener) rtGlobal).count == 1, "round-tripped global listener still notified");
        check(rtNamed.count == 1, "round-tripped named listener still notified");
        check(((SerialListener) rtGlobal).last.getSource() instanceof SerialSource,
                "events fired after round-trip carry the deserialized source");
        check(((SerialListener) rtGlobal).last.getSource().equals(src),
                "deserialized source is a value-equal copy of the original source");
        check(serGlobal.count == 0 && plainGlobal.count == 0,
                "original listener instances are not notified by the round-tripped support");

        rt.firePropertyChange("unrelated", 1, 2);
        check(((SerialListener) rtGlobal).count == 2 && rtNamed.count == 1,
                "named filtering still applies after round-trip");

        checkThrows(NotSerializableException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    roundtrip(new PropertyChangeSupport(new Object()));
                }}, "serializing a support with non-serializable source -> NotSerializableException");
    }

    static void testDuplicateRegistration() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        Counting dup = new Counting();
        pcs.addPropertyChangeListener(dup);
        pcs.addPropertyChangeListener(dup);
        check(pcs.getPropertyChangeListeners().length == 2,
                "registering the same listener twice keeps two registrations");
        pcs.firePropertyChange("p", 1, 2);
        check(dup.count == 2, "duplicated listener notified twice per fire");
        pcs.removePropertyChangeListener(dup);
        check(pcs.getPropertyChangeListeners().length == 1, "one remove removes exactly one registration");
        pcs.firePropertyChange("p", 1, 2);
        check(dup.count == 3, "remaining registration still notified once");
        pcs.removePropertyChangeListener(dup);
        pcs.firePropertyChange("p", 1, 2);
        check(dup.count == 3 && pcs.getPropertyChangeListeners().length == 0,
                "second remove removes the last registration");

        Counting namedDup = new Counting();
        pcs.addPropertyChangeListener("foo", namedDup);
        pcs.addPropertyChangeListener("foo", namedDup);
        pcs.firePropertyChange("foo", 1, 2);
        check(namedDup.count == 2, "duplicated named listener notified twice per fire");
    }

    static void testNotificationOrder() {
        Object src = new Object();
        final List<String> order = new ArrayList<String>();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        for (String tag : new String[]{"A", "B", "C"}) {
            final String t = tag;
            pcs.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) { order.add(t); }
            });
        }
        pcs.firePropertyChange("p", "a", "b");
        check(order.size() == 3 && "A".equals(order.get(0)) && "B".equals(order.get(1)) && "C".equals(order.get(2)),
                "global listeners notified in registration order");

        final List<String> mixed = new ArrayList<String>();
        PropertyChangeSupport pcs2 = new PropertyChangeSupport(src);
        pcs2.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { mixed.add("G1"); }
        });
        pcs2.addPropertyChangeListener("foo", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { mixed.add("N"); }
        });
        pcs2.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { mixed.add("G2"); }
        });
        pcs2.firePropertyChange("foo", "a", "b");
        check(mixed.size() == 3 && "G1".equals(mixed.get(0)) && "G2".equals(mixed.get(1)) && "N".equals(mixed.get(2)),
                "globals are notified first (registration order), then named listeners");
    }

    static void testListenerException() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        final Counting second = new Counting();
        PropertyChangeListener throwing = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { throw new IllegalStateException("boom"); }
        };
        pcs.addPropertyChangeListener(throwing);
        pcs.addPropertyChangeListener(second);
        try {
            pcs.firePropertyChange("p", "a", "b");
            check(false, "listener RuntimeException must propagate to the caller");
        } catch (IllegalStateException expected) {
            check("boom".equals(expected.getMessage()),
                    "the exact listener RuntimeException object propagates unmodified");
        }
        check(second.count == 0, "listeners registered after a throwing listener are not notified");
        pcs.removePropertyChangeListener(throwing);
        pcs.firePropertyChange("p", "x", "y");
        check(second.count == 1, "delivery resumes once the throwing listener is removed");
    }

    static void testIdentityVsEqualsFire() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        Counting c = new Counting();
        pcs.addPropertyChangeListener(c);
        // one shared reference used as both old and new whose equals() is always false
        Object antiEquals = new Object() {
            @Override public boolean equals(Object o) { return false; }
            @Override public int hashCode() { return 1; }
        };
        pcs.firePropertyChange("p", antiEquals, antiEquals);
        check(c.count == 1, "identity-same old/new with equals()==false still fires");
        check(c.last.getOldValue() == antiEquals && c.last.getNewValue() == antiEquals,
                "event carries the exact same reference as old and new");
        String shared = "same";
        pcs.firePropertyChange("p", shared, shared);
        check(c.count == 1, "same reference with equal content does not fire");
    }

    static void testNullNameFire() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        Counting global = new Counting();
        Counting named = new Counting();
        pcs.addPropertyChangeListener(global);
        pcs.addPropertyChangeListener("foo", named);
        pcs.firePropertyChange(null, "a", "b");
        check(global.count == 1, "fire with null property name reaches global listeners");
        check(global.last.getPropertyName() == null, "delivered event keeps the null property name");
        check(named.count == 0, "named listeners are not reached by a null-name fire");
    }

    static void testNullEventFire() {
        final PropertyChangeSupport pcs = new PropertyChangeSupport(new Object());
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() {
                    pcs.firePropertyChange((PropertyChangeEvent) null);
                }}, "firePropertyChange(null event) -> NPE");
    }

    static void testHasListenersNull() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        pcs.addPropertyChangeListener("foo", new Counting());
        check(!pcs.hasListeners(null), "hasListeners(null) false while only named listeners are registered");
        pcs.addPropertyChangeListener(new Counting());
        check(pcs.hasListeners(null), "hasListeners(null) true once a global listener is registered");
    }

    static void testProxyDetails() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        Counting namedFoo = new Counting();
        Counting namedBar = new Counting();
        pcs.addPropertyChangeListener("foo", namedFoo);
        pcs.addPropertyChangeListener("bar", namedBar);
        PropertyChangeListener[] all = pcs.getPropertyChangeListeners();
        check(all.length == 2, "two named listeners yield two proxies");
        Set<String> names = new HashSet<String>();
        for (PropertyChangeListener l : all) {
            check(l instanceof PropertyChangeListenerProxy, "each named listener is returned as a proxy");
            names.add(((PropertyChangeListenerProxy) l).getPropertyName());
        }
        check(names.contains("foo") && names.contains("bar"), "proxy property names preserved");
        check(pcs.getPropertyChangeListeners("foo")[0] == namedFoo,
                "getPropertyChangeListeners(name) returns the raw listener identity (unwrapped)");
        check(pcs.getPropertyChangeListeners("foo").length == 1,
                "name-filtered array contains only that property's listeners");
    }

    static void testRemoveNoOps() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        pcs.removePropertyChangeListener(new Counting());
        pcs.removePropertyChangeListener("nope", new Counting());
        pcs.removePropertyChangeListener(null);
        pcs.removePropertyChangeListener("foo", null);
        pcs.removePropertyChangeListener((String) null, new Counting());
        check(pcs.getPropertyChangeListeners().length == 0,
                "removing never-added, null or null-named listeners is a no-op");
    }

    static void testIndexedFireNamed() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        Counting named = new Counting();
        pcs.addPropertyChangeListener("arr", named);
        pcs.fireIndexedPropertyChange("arr", 2, "x", "y");
        check(named.count == 1, "indexed fire reaches the named listener of that property");
        check(named.last instanceof IndexedPropertyChangeEvent,
                "named listener receives an IndexedPropertyChangeEvent");
        check(((IndexedPropertyChangeEvent) named.last).getIndex() == 2,
                "index preserved in delivery to a named listener");
    }

    static void testToStringMisc() {
        PropertyChangeSupport pcs = new PropertyChangeSupport(new Object());
        check(pcs.toString() != null && pcs.toString().contains("PropertyChangeSupport"),
                "toString is non-null and mentions the class name");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testConstructorAndNoOp();
        testGlobalVsNamed();
        testFireConditions();
        testFireEvent();
        testIndexedFire();
        testDefensiveArrays();
        testSerializationRoundTrip();
        testDuplicateRegistration();
        testNotificationOrder();
        testListenerException();
        testIdentityVsEqualsFire();
        testNullNameFire();
        testNullEventFire();
        testHasListenersNull();
        testProxyDetails();
        testRemoveNoOps();
        testIndexedFireNamed();
        testToStringMisc();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
