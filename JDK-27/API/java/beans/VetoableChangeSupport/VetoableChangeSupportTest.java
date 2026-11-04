/*
 * @test
 * @summary java.beans.VetoableChangeSupport conformance: full contract.
 *          null source -> NPE, null listener / null name no-op semantics,
 *          global vs named listener registration, exact fire/no-fire
 *          conditions for Object/int/boolean, fireVetoableChange(event)
 *          condition, and PropertyVetoException propagation (a vetoing
 *          listener aborts delivery and surfaces the event).
 *          Deepening: veto rollback (already-notified listeners are
 *          re-notified in reverse with a new swapped old/new event, vetoes
 *          raised against the rollback are ignored, the vetoer itself is not
 *          rolled back, original veto exception and exact event identity
 *          surface to the caller), event overload delivers the identical
 *          event object, listener RuntimeException propagates unwrapped with
 *          no rollback, fireVetoableChange(null event) -> NPE, serialization
 *          round-trip (serializable listeners survive, non-serializable
 *          dropped, non-serializable source -> NotSerializableException),
 *          VetoableChangeListenerProxy details, defensive arrays, and
 *          remove no-ops.
 * @run main VetoableChangeSupportTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeListenerProxy;
import java.beans.VetoableChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VetoableChangeSupportTest {
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

    static class Counting implements VetoableChangeListener {
        int count;
        PropertyChangeEvent last;
        public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException { count++; last = e; }
    }

    static void testConstructorAndNoOp() {
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() {
                    new VetoableChangeSupport(null);
                }}, "null source -> NPE");

        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        check(!vcs.hasListeners("foo"), "no listeners initially");
        vcs.addVetoableChangeListener((VetoableChangeListener) null);
        check(vcs.getVetoableChangeListeners().length == 0, "add(null) no-op");
        vcs.addVetoableChangeListener("foo", (VetoableChangeListener) null);
        check(vcs.getVetoableChangeListeners("foo").length == 0, "add(foo,null) no-op");
        vcs.addVetoableChangeListener((String) null, new Counting());
        check(vcs.getVetoableChangeListeners().length == 0, "add(null name) no-op");
        check(vcs.getVetoableChangeListeners(null).length == 0, "getListeners(null) empty");
    }

    static void testGlobalVsNamed() throws Exception {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        Counting global = new Counting();
        Counting named = new Counting();
        vcs.addVetoableChangeListener("foo", named);
        check(vcs.hasListeners("foo"), "hasListeners foo after named add");
        check(!vcs.hasListeners("bar"), "hasListeners bar false with only named foo listener");

        vcs.addVetoableChangeListener(global);
        check(vcs.hasListeners("bar"), "global listener counts for any name");
        check(vcs.getVetoableChangeListeners().length == 2, "global + named count");
        check(vcs.getVetoableChangeListeners("foo").length == 1, "named count");

        vcs.fireVetoableChange("foo", "a", "b");
        check(global.count == 1 && named.count == 1, "fire foo hits global + named");
        check(src == global.last.getSource(), "event source");
        check("foo".equals(global.last.getPropertyName()), "event property");

        vcs.fireVetoableChange("bar", 1, 2);
        check(global.count == 2 && named.count == 1, "fire bar only global");

        vcs.removeVetoableChangeListener("foo", named);
        vcs.fireVetoableChange("foo", "x", "y");
        check(global.count == 3 && named.count == 1, "removed named not fired");
    }

    static void testFireConditions() throws Exception {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        Counting c = new Counting();
        vcs.addVetoableChangeListener(c);

        vcs.fireVetoableChange("p", "a", "a");
        check(c.count == 0, "equal non-null objects not fired");
        vcs.fireVetoableChange("p", null, null);
        check(c.count == 1, "both null fires");
        vcs.fireVetoableChange("p", 5, 5);
        check(c.count == 1, "equal int not fired");
        vcs.fireVetoableChange("p", 5, 6);
        check(c.count == 2, "different int fired");
        vcs.fireVetoableChange("p", true, true);
        check(c.count == 2, "equal boolean not fired");
        vcs.fireVetoableChange("p", false, true);
        check(c.count == 3, "different boolean fired");

        vcs.fireVetoableChange(new PropertyChangeEvent(src, "p", "same", "same"));
        check(c.count == 3, "equal non-null event not fired");
        vcs.fireVetoableChange(new PropertyChangeEvent(src, "p", null, "v"));
        check(c.count == 4, "null-old event fired");
    }

    static void testVetoPropagation() {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        final Counting after = new Counting();
        vcs.addVetoableChangeListener(new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                throw new PropertyVetoException("veto", e);
            }
        });
        vcs.addVetoableChangeListener(after);
        try {
            vcs.fireVetoableChange("bar", "x", "y");
            check(false, "veto should throw");
        } catch (PropertyVetoException pve) {
            passed++;
            check("veto".equals(pve.getMessage()), "veto message");
            check("bar".equals(pve.getPropertyChangeEvent().getPropertyName()), "veto event property");
            check("x".equals(pve.getPropertyChangeEvent().getOldValue()), "veto event old value");
        }
        check(after.count == 0, "veto aborts subsequent listeners");
    }


    static void testStructure() {
        check(java.io.Serializable.class.isAssignableFrom(VetoableChangeSupport.class), "implements Serializable");
    }

    // ===== appended: contract deepening (round A) =====

    /** Serializable listener that survives VetoableChangeSupport serialization. */
    static class SerialVeto implements VetoableChangeListener, Serializable {
        private static final long serialVersionUID = 1L;
        int count;
        public void vetoableChange(PropertyChangeEvent e) { count++; }
    }

    /** Non-serializable listener: silently dropped by serialization. */
    static class PlainVeto implements VetoableChangeListener {
        int count;
        public void vetoableChange(PropertyChangeEvent e) { count++; }
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

    static void testVetoRollback() throws Exception {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        final List<String> log = new ArrayList<String>();
        vcs.addVetoableChangeListener(new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) {
                log.add("first:" + e.getOldValue() + "->" + e.getNewValue());
            }
        });
        vcs.addVetoableChangeListener(new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                log.add("vetoer");
                throw new PropertyVetoException("no", e);
            }
        });
        final Counting after = new Counting();
        vcs.addVetoableChangeListener(after);
        try {
            vcs.fireVetoableChange("p", "old", "new");
            check(false, "a veto must surface as PropertyVetoException");
        } catch (PropertyVetoException pve) {
            check("no".equals(pve.getMessage()), "the vetoer's own exception object propagates");
            check("p".equals(pve.getPropertyChangeEvent().getPropertyName()),
                    "PropertyVetoException carries the vetoed event");
            check("old".equals(pve.getPropertyChangeEvent().getOldValue())
                    && "new".equals(pve.getPropertyChangeEvent().getNewValue()),
                    "vetoed event keeps the original (not swapped) old/new values");
        }
        check(log.size() == 3, "veto triggers a rollback pass: forward, veto, then revert");
        check("first:old->new".equals(log.get(0)), "listener before the vetoer saw the original event first");
        check("vetoer".equals(log.get(1)), "the vetoer itself is notified exactly once (never rolled back)");
        check("first:new->old".equals(log.get(2)),
                "rollback re-notifies already-notified listeners with old/new swapped");
        check(after.count == 0, "listeners registered after the vetoer are never notified");

        // vetoer registered FIRST: nothing to roll back
        VetoableChangeSupport vcsFirst = new VetoableChangeSupport(src);
        final int[] vetoerCalls = {0};
        vcsFirst.addVetoableChangeListener(new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                vetoerCalls[0]++;
                throw new PropertyVetoException("first", e);
            }
        });
        final Counting tail = new Counting();
        vcsFirst.addVetoableChangeListener(tail);
        try {
            vcsFirst.fireVetoableChange("q", "a", "b");
            check(false, "first-position veto must also throw");
        } catch (PropertyVetoException pve) {
            check("first".equals(pve.getMessage()), "first-position veto propagates");
        }
        check(vetoerCalls[0] == 1 && tail.count == 0,
                "vetoer first: exactly one call to the vetoer, none to later listeners");
    }

    static void testRollbackVetoIgnored() throws Exception {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        // first listener accepts the forward event but vetoes the rollback event
        vcs.addVetoableChangeListener(new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                if ("old".equals(e.getNewValue())) throw new PropertyVetoException("revert-veto", e);
            }
        });
        vcs.addVetoableChangeListener(new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                throw new PropertyVetoException("main-veto", e);
            }
        });
        try {
            vcs.fireVetoableChange("p", "old", "new");
            check(false, "veto must throw");
        } catch (PropertyVetoException pve) {
            check("main-veto".equals(pve.getMessage()),
                    "a veto raised against the rollback event is ignored; the original veto propagates");
        }
    }

    static void testEventOverloadIdentity() throws Exception {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        final List<PropertyChangeEvent> got = new ArrayList<PropertyChangeEvent>();
        vcs.addVetoableChangeListener(new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) { got.add(e); }
        });
        final PropertyChangeEvent[] vetoerSaw = {null};
        vcs.addVetoableChangeListener(new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                vetoerSaw[0] = e;
                throw new PropertyVetoException("v", e);
            }
        });
        PropertyChangeEvent passed = new PropertyChangeEvent(src, "pp", "1", "2");
        try {
            vcs.fireVetoableChange(passed);
            check(false, "veto must throw");
        } catch (PropertyVetoException pve) {
            check(pve.getPropertyChangeEvent() == passed,
                    "PropertyVetoException carries the exact event object passed to fire");
        }
        check(got.size() == 2, "earlier listener sees forward event plus rollback event");
        check(got.get(0) == passed, "fireVetoableChange(event) delivers the identical event object");
        check("2".equals(got.get(1).getOldValue()) && "1".equals(got.get(1).getNewValue()),
                "the rollback event is a new event with old/new swapped");
        check(vetoerSaw[0] == passed, "the vetoer received the exact passed event object");
    }

    static void testListenerRuntimeException() throws Exception {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        final Counting before = new Counting();
        final Counting after = new Counting();
        vcs.addVetoableChangeListener(before);
        vcs.addVetoableChangeListener(new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) { throw new IllegalArgumentException("rt"); }
        });
        vcs.addVetoableChangeListener(after);
        try {
            vcs.fireVetoableChange("p", "a", "b");
            check(false, "listener RuntimeException must propagate");
        } catch (IllegalArgumentException expected) {
            check("rt".equals(expected.getMessage()),
                    "listener RuntimeException propagates unwrapped (not converted to PropertyVetoException)");
        }
        check(before.count == 1, "no rollback pass for a RuntimeException veto failure");
        check(after.count == 0, "listeners after the throwing listener are skipped");
    }

    static void testNullEventFire() throws Exception {
        final VetoableChangeSupport vcs = new VetoableChangeSupport(new Object());
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    vcs.fireVetoableChange((PropertyChangeEvent) null);
                }}, "fireVetoableChange(null event) -> NPE");
    }

    static void testSerializationRoundTrip() throws Exception {
        SerialSource src = new SerialSource(11);
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        SerialVeto serGlobal = new SerialVeto();
        PlainVeto plainGlobal = new PlainVeto();
        SerialVeto serNamed = new SerialVeto();
        vcs.addVetoableChangeListener(serGlobal);
        vcs.addVetoableChangeListener(plainGlobal);
        vcs.addVetoableChangeListener("foo", serNamed);
        vcs.addVetoableChangeListener("foo", new PlainVeto());

        VetoableChangeSupport rt = (VetoableChangeSupport) roundtrip(vcs);
        check(rt.getVetoableChangeListeners().length == 2,
                "serialization keeps serializable listeners and drops non-serializable ones");
        check(rt.getVetoableChangeListeners("foo").length == 1,
                "only the serializable named listener survives serialization");
        boolean sawProxy = false;
        for (VetoableChangeListener l : rt.getVetoableChangeListeners()) {
            if (l instanceof VetoableChangeListenerProxy) sawProxy = true;
        }
        check(sawProxy, "round-tripped named listener still wrapped in a VetoableChangeListenerProxy");
        SerialVeto rtGlobal = (SerialVeto) rt.getVetoableChangeListeners()[0];
        SerialVeto rtNamed = (SerialVeto) rt.getVetoableChangeListeners("foo")[0];
        rt.fireVetoableChange("foo", "a", "b");
        rt.fireVetoableChange("bar", 1, 2);
        check(rtGlobal.count == 2, "round-tripped global listener notified for every fire");
        check(rtNamed.count == 1, "round-tripped named listener notified only for its property");
        check(serGlobal.count == 0, "original listener instances are not notified by the round-tripped support");

        checkThrows(NotSerializableException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    roundtrip(new VetoableChangeSupport(new Object()));
                }}, "serializing a support with non-serializable source -> NotSerializableException");
    }

    static void testProxyDetails() {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        Counting named = new Counting();
        vcs.addVetoableChangeListener("foo", named);
        VetoableChangeListener[] all = vcs.getVetoableChangeListeners();
        check(all.length == 1 && all[0] instanceof VetoableChangeListenerProxy,
                "named listener returned wrapped in a VetoableChangeListenerProxy");
        check("foo".equals(((VetoableChangeListenerProxy) all[0]).getPropertyName()),
                "proxy carries the property name");
        check(vcs.getVetoableChangeListeners("foo")[0] == named,
                "getVetoableChangeListeners(name) returns the raw listener identity (unwrapped)");
        check(!vcs.hasListeners(null), "hasListeners(null) false while only named listeners are registered");
        vcs.addVetoableChangeListener(new Counting());
        check(vcs.hasListeners(null), "hasListeners(null) true once a global listener is registered");
    }

    static void testRemoveNoOps() {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        Counting c = new Counting();
        vcs.addVetoableChangeListener(c);
        vcs.removeVetoableChangeListener(c);
        vcs.removeVetoableChangeListener(c);
        check(vcs.getVetoableChangeListeners().length == 0,
                "first remove unregisters; repeated remove of the same listener is a no-op");
        vcs.removeVetoableChangeListener(null);
        vcs.removeVetoableChangeListener((String) null, c);
        vcs.removeVetoableChangeListener("x", c);
        check(vcs.getVetoableChangeListeners().length == 0,
                "removing null or never-added listeners is a no-op");
    }

    static void testDefensiveArrays() {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        vcs.addVetoableChangeListener(new Counting());
        VetoableChangeListener[] a = vcs.getVetoableChangeListeners();
        a[0] = null;
        check(vcs.getVetoableChangeListeners().length == 1,
                "getVetoableChangeListeners returns a defensive copy");
    }

    static void testNamedFireScope() throws Exception {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        Counting global = new Counting();
        Counting named = new Counting();
        vcs.addVetoableChangeListener(global);
        vcs.addVetoableChangeListener("foo", named);
        vcs.fireVetoableChange("bar", "a", "b");
        check(global.count == 1 && named.count == 0, "named listener misses other properties");
        vcs.fireVetoableChange("foo", "a", "b");
        check(global.count == 2 && named.count == 1, "global sees all fires, named sees only its property");
    }

    static void testToStringMisc() {
        check(new VetoableChangeSupport(new Object()).toString() != null
                && new VetoableChangeSupport(new Object()).toString().contains("VetoableChangeSupport"),
                "toString is non-null and mentions the class name");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testConstructorAndNoOp();
        testGlobalVsNamed();
        testFireConditions();
        testVetoPropagation();
        testVetoRollback();
        testRollbackVetoIgnored();
        testEventOverloadIdentity();
        testListenerRuntimeException();
        testNullEventFire();
        testSerializationRoundTrip();
        testProxyDetails();
        testRemoveNoOps();
        testDefensiveArrays();
        testNamedFireScope();
        testToStringMisc();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
