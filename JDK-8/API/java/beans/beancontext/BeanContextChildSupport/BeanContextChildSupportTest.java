/*
 * @test
 * @summary java.beans.beancontext.BeanContextChildSupport conformance: white-box structure + black-box behavior.
 *          beanContext property semantics (exact events, same-value no fire, detach,
 *          fresh-null no event), veto paths (listener veto and validatePendingSetBeanContext
 *          rejection both -> PropertyVetoException with property unchanged), the
 *          rejectedSetBCOnce one-shot bypass quirk, initialize/releaseBeanContextResources
 *          hook call sites, firePropertyChange/fireVetoableChange full contract (named
 *          selectivity, equal non-null no fire, veto reversal event, duplicate
 *          registration), peer delegation (null peer -> self, delegated event source is
 *          the peer), null-name/null-listener silent no-ops, service hooks, and
 *          serialization round-trip (transient beanContext, serializable listeners
 *          survive, non-serializable delegated peer -> IOException).
 * @run main BeanContextChildSupportTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextServiceAvailableEvent;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServicesListener;
import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BeanContextChildSupportTest {
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
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static class SerialListener implements PropertyChangeListener, Serializable {
        int n;
        public void propertyChange(PropertyChangeEvent e) { n++; }
    }

    /** Exposes the protected pcSupport to inspect listener survival. */
    static class Exposed extends BeanContextChildSupport {
        PropertyChangeListener[] named(String name) { return pcSupport.getPropertyChangeListeners(name); }
    }

    static class Holder extends BeanContextChildSupport {
        int releaseCalls, initCalls;
        protected void releaseBeanContextResources() { releaseCalls++; }
        protected void initializeBeanContextResources() { initCalls++; }
    }

    static class RejectAll extends BeanContextChildSupport {
        public boolean validatePendingSetBeanContext(BeanContext newValue) { return false; }
    }

    static class Vetoer implements VetoableChangeListener {
        boolean veto;
        int calls;
        public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
            calls++;
            if (veto && e.getNewValue() != null) throw new PropertyVetoException("vetoed", e);
        }
    }

    static class NonSerChild implements BeanContextChild {
        public void setBeanContext(BeanContext bc) { }
        public BeanContext getBeanContext() { return null; }
        public void addPropertyChangeListener(String n, PropertyChangeListener l) { }
        public void removePropertyChangeListener(String n, PropertyChangeListener l) { }
        public void addVetoableChangeListener(String n, VetoableChangeListener l) { }
        public void removeVetoableChangeListener(String n, VetoableChangeListener l) { }
    }

    static class ServiceRecorder extends BeanContextChildSupport {
        int avail, revoked;
        public void serviceAvailable(BeanContextServiceAvailableEvent e) { avail++; }
        public void serviceRevoked(BeanContextServiceRevokedEvent e) { revoked++; }
    }

    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return bos.toByteArray();
    }
    static <T> T deser(byte[] b) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
        @SuppressWarnings("unchecked") T t = (T) ois.readObject();
        ois.close();
        return t;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextChildSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(BeanContextChild.class.isAssignableFrom(c), "implements BeanContextChild");
        check(BeanContextServicesListener.class.isAssignableFrom(c), "implements BeanContextServicesListener");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(hasMethod(c, "addPropertyChangeListener"), "declares addPropertyChangeListener");
        check(hasMethod(c, "addVetoableChangeListener"), "declares addVetoableChangeListener");
        check(hasMethod(c, "firePropertyChange"), "declares firePropertyChange");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 6328947014421475877L, "serialVersionUID stable");
    }

    static void testBlackBox() throws Exception {
        BeanContextChildSupport cs = new BeanContextChildSupport();
        check(cs.getBeanContext() == null, "beanContext null default");
        check(cs.getBeanContextChildPeer() == cs, "peer is self");
        check(!cs.isDelegated(), "not delegated default");

        BeanContextSupport bc = new BeanContextSupport();
        cs.setBeanContext(bc);
        check(cs.getBeanContext() == bc, "setBeanContext");
        check(cs.validatePendingSetBeanContext(bc), "validatePendingSetBeanContext true");

        final int[] p = new int[1];
        cs.addPropertyChangeListener("x", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { p[0]++; }
        });
        cs.firePropertyChange("x", null, null);
        check(p[0] == 1, "firePropertyChange");

        cs.addVetoableChangeListener("y", new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) {}
        });
        cs.fireVetoableChange("y", null, null);
        check(true, "fireVetoableChange no throw");

        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        cs.serviceRevoked(new BeanContextServiceRevokedEvent(bcss, Object.class, true));
        cs.serviceAvailable(new BeanContextServiceAvailableEvent(bcss, Object.class));
        check(true, "serviceRevoked/serviceAvailable no throw");

        BeanContextChildSupport peer = new BeanContextChildSupport();
        BeanContextChildSupport cs2 = new BeanContextChildSupport(peer);
        check(cs2.getBeanContextChildPeer() == peer, "peer ctor");
        check(cs2.isDelegated(), "delegated ctor");
    }

    static void testSetBeanContextEvents() throws Exception {
        BeanContextChildSupport cs = new BeanContextChildSupport();
        final List<PropertyChangeEvent> evs = new ArrayList<PropertyChangeEvent>();
        cs.addPropertyChangeListener("beanContext", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { evs.add(e); }
        });
        // fresh null -> null is a no-op (no event)
        cs.setBeanContext(null);
        check(evs.isEmpty(), "setBeanContext(null) on fresh child fires nothing");
        check(cs.getBeanContext() == null, "still null");
        // attach fires exact event
        BeanContextSupport bc = new BeanContextSupport();
        cs.setBeanContext(bc);
        check(evs.size() == 1, "attach fires once");
        check("beanContext".equals(evs.get(0).getPropertyName()), "event property name");
        check(evs.get(0).getOldValue() == null && evs.get(0).getNewValue() == bc, "event old null new bc");
        check(evs.get(0).getSource() == cs, "event source is the child");
        // same value: silent
        evs.clear();
        cs.setBeanContext(bc);
        check(evs.isEmpty() && cs.getBeanContext() == bc, "same value no event");
        // detach fires old=bc new=null
        cs.setBeanContext(null);
        check(evs.size() == 1 && evs.get(0).getOldValue() == bc && evs.get(0).getNewValue() == null,
              "detach event old bc new null");
        check(cs.getBeanContext() == null, "detached");
        // re-attach to another context
        BeanContextSupport bc2 = new BeanContextSupport();
        cs.setBeanContext(bc2);
        check(cs.getBeanContext() == bc2, "re-attach to another context");
    }

    static void testVetoPaths() throws Exception {
        // listener veto
        BeanContextChildSupport cs = new BeanContextChildSupport();
        Vetoer v = new Vetoer();
        v.veto = true;
        cs.addVetoableChangeListener("beanContext", v);
        BeanContextSupport bc = new BeanContextSupport();
        try {
            cs.setBeanContext(bc);
            check(false, "vetoing listener must cause PropertyVetoException");
        } catch (PropertyVetoException pve) {
            check(true, "listener veto -> PropertyVetoException");
            PropertyChangeEvent pce = pve.getPropertyChangeEvent();
            check(pce != null && "beanContext".equals(pce.getPropertyName()), "PVE carries beanContext PropertyChangeEvent");
            check(pce.getOldValue() == null && pce.getNewValue() == bc, "PVE event old/new exact");
        }
        check(cs.getBeanContext() == null, "property unchanged after listener veto");
        check(v.calls == 1, "vetoing listener consulted once");
        // rejectedSetBCOnce quirk: the next setBeanContext bypasses veto entirely
        v.calls = 0;
        cs.setBeanContext(bc);
        check(cs.getBeanContext() == bc, "set right after a veto bypasses the veto (one-shot)");
        check(v.calls == 0, "vetoing listener not consulted during bypass");
        // flag reset after a successful set: veto is honored again
        cs.setBeanContext(null);
        try {
            cs.setBeanContext(bc);
            check(false, "veto must be honored again after a successful set");
        } catch (PropertyVetoException pve) {
            check(true, "veto honored after reset");
        }
        check(cs.getBeanContext() == null, "unchanged after second veto");
        // validatePendingSetBeanContext rejection
        RejectAll r = new RejectAll();
        try {
            r.setBeanContext(bc);
            check(false, "validate false must cause PropertyVetoException");
        } catch (PropertyVetoException pve) {
            check(true, "validatePendingSetBeanContext false -> PropertyVetoException");
        }
        check(r.getBeanContext() == null, "unchanged after validation rejection");
        // bypass applies after validation rejection too
        r.setBeanContext(bc);
        check(r.getBeanContext() == bc, "bypass after validation rejection");
        // and resets again: the next different value is validated (and rejected)
        try {
            r.setBeanContext(new BeanContextSupport());
            check(false, "validation rejection recurs after reset");
        } catch (PropertyVetoException pve) {
            check(true, "validation rejection recurs after reset");
        }
        check(r.getBeanContext() == bc, "still attached to the bypassed context");
    }

    static void testResourceHooks() throws Exception {
        Holder h = new Holder();
        BeanContextSupport bc = new BeanContextSupport();
        h.setBeanContext(bc);
        check(h.initCalls == 1 && h.releaseCalls == 0, "initializeBeanContextResources called on attach");
        h.setBeanContext(null);
        check(h.releaseCalls == 1, "releaseBeanContextResources called on detach");
        h.setBeanContext(null);
        check(h.releaseCalls == 1, "null-to-null transition calls no hooks");
        h.setBeanContext(bc);
        h.setBeanContext(bc);
        check(h.initCalls == 2, "same-value set does not re-run hooks");
    }

    static void testFirePropertyChange() throws Exception {
        BeanContextChildSupport cs = new BeanContextChildSupport();
        final List<PropertyChangeEvent> evs = new ArrayList<PropertyChangeEvent>();
        cs.addPropertyChangeListener("p", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { evs.add(e); }
        });
        cs.firePropertyChange("p", "old", "new");
        check(evs.size() == 1, "named fire reaches named listener");
        check("p".equals(evs.get(0).getPropertyName()) && "old".equals(evs.get(0).getOldValue())
              && "new".equals(evs.get(0).getNewValue()), "event fields exact");
        check(evs.get(0).getSource() == cs, "event source is the child");
        // equal non-null old/new: no fire
        evs.clear();
        cs.firePropertyChange("p", "same", "same");
        check(evs.isEmpty(), "equal non-null old/new fires nothing");
        // null old fires
        cs.firePropertyChange("p", null, "v");
        check(evs.size() == 1 && evs.get(0).getOldValue() == null, "null old fires");
        // other property not delivered
        evs.clear();
        cs.firePropertyChange("other", "a", "b");
        check(evs.isEmpty(), "other property not delivered to p listener");
        // duplicate registration doubles delivery; one remove halves it
        BeanContextChildSupport dup = new BeanContextChildSupport();
        final int[] n = {0};
        PropertyChangeListener l = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { n[0]++; }
        };
        dup.addPropertyChangeListener("p", l);
        dup.addPropertyChangeListener("p", l);
        dup.firePropertyChange("p", "a", "b");
        check(n[0] == 2, "duplicate registration fires twice");
        dup.removePropertyChangeListener("p", l);
        n[0] = 0;
        dup.firePropertyChange("p", "a", "b");
        check(n[0] == 1, "one remove leaves one delivery");
        // remove never-added listener: no throw, no effect
        dup.removePropertyChangeListener("p", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { }
        });
        n[0] = 0;
        dup.firePropertyChange("p", "a", "b");
        check(n[0] == 1, "removing a stranger changes nothing");
    }

    static void testFireVetoableChange() throws Exception {
        // no listeners: no throw
        BeanContextChildSupport cs0 = new BeanContextChildSupport();
        cs0.fireVetoableChange("q", "a", "b");
        check(true, "fireVetoableChange without listeners completes");
        // non-vetoing listener notified once with exact fields
        BeanContextChildSupport cs = new BeanContextChildSupport();
        final List<PropertyChangeEvent> evs = new ArrayList<PropertyChangeEvent>();
        cs.addVetoableChangeListener("q", new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) { evs.add(e); }
        });
        cs.fireVetoableChange("q", "a", "b");
        check(evs.size() == 1, "vetoable listener notified");
        check("q".equals(evs.get(0).getPropertyName()) && "a".equals(evs.get(0).getOldValue())
              && "b".equals(evs.get(0).getNewValue()), "vetoable event fields exact");
        // equal non-null: no notification
        evs.clear();
        cs.fireVetoableChange("q", "x", "x");
        check(evs.isEmpty(), "equal non-null old/new fires nothing");
        // other property not delivered
        cs.fireVetoableChange("z", "a", "b");
        check(evs.isEmpty(), "other property not delivered");
        // veto: PVE propagates and the non-throwing listener sees original + reversal event
        BeanContextChildSupport csv = new BeanContextChildSupport();
        final List<PropertyChangeEvent> seq = new ArrayList<PropertyChangeEvent>();
        final List<PropertyChangeEvent> throwerSaw = new ArrayList<PropertyChangeEvent>();
        csv.addVetoableChangeListener("q", new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) { seq.add(e); }
        });
        csv.addVetoableChangeListener("q", new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                throwerSaw.add(e);
                throw new PropertyVetoException("no", e);
            }
        });
        try {
            csv.fireVetoableChange("q", "old", "new");
            check(false, "veto must propagate out of fireVetoableChange");
        } catch (PropertyVetoException pve) {
            check(true, "veto propagates as PropertyVetoException");
        }
        check(throwerSaw.size() == 1 && "new".equals(throwerSaw.get(0).getNewValue()),
              "throwing listener saw the original proposal exactly once");
        check(seq.size() == 2, "on veto the non-throwing listener saw original + reversal events");
        check("new".equals(seq.get(0).getNewValue()), "first event is the proposed change");
        check("old".equals(seq.get(1).getNewValue()), "second event reverts to the old value");
        // removed listener no longer notified
        Vetoer v = new Vetoer();
        BeanContextChildSupport csr = new BeanContextChildSupport();
        csr.addVetoableChangeListener("q", v);
        csr.removeVetoableChangeListener("q", v);
        csr.fireVetoableChange("q", "a", "b");
        check(v.calls == 0, "removed vetoable listener not notified");
    }

    static void testPeerDelegation() throws Exception {
        // null peer -> self
        BeanContextChildSupport nullPeer = new BeanContextChildSupport(null);
        check(nullPeer.getBeanContextChildPeer() == nullPeer, "null peer ctor yields self peer");
        check(!nullPeer.isDelegated(), "null peer not delegated");
        // public field mirrors the accessor
        BeanContextChildSupport peer = new BeanContextChildSupport();
        BeanContextChildSupport cs = new BeanContextChildSupport(peer);
        check(cs.beanContextChildPeer == peer, "public beanContextChildPeer field");
        check(cs.getBeanContextChildPeer() == peer, "peer ctor peer identity");
        check(cs.isDelegated(), "delegated via ctor");
        // events fired by the delegate carry the peer as source
        final Object[] src = new Object[1];
        cs.addPropertyChangeListener("d", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { src[0] = e.getSource(); }
        });
        cs.firePropertyChange("d", "a", "b");
        check(src[0] == peer, "delegated event source is the peer");
        // setBeanContext on the delegate affects the delegate, not the peer
        BeanContextSupport bc = new BeanContextSupport();
        cs.setBeanContext(bc);
        check(cs.getBeanContext() == bc, "delegated setBeanContext sets own property");
        check(peer.getBeanContext() == null, "peer property untouched");
    }

    static void testNullRegistrations() throws Exception {
        BeanContextChildSupport cs = new BeanContextChildSupport();
        final int[] n = {0};
        PropertyChangeListener l = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { n[0]++; }
        };
        cs.addPropertyChangeListener(null, l);   // null name: silent no-op
        cs.addPropertyChangeListener("p", null); // null listener: silent no-op
        cs.addVetoableChangeListener(null, new Vetoer());
        cs.addVetoableChangeListener("p", null);
        cs.removePropertyChangeListener("p", l); // never added: no-op
        cs.removeVetoableChangeListener("p", new Vetoer());
        cs.firePropertyChange("p", "a", "b");
        cs.fireVetoableChange("p", "a", "b");
        check(n[0] == 0, "null-name/null-listener registrations deliver nothing");
        check(true, "null registrations never throw");
    }

    static void testServiceHooks() throws Exception {
        ServiceRecorder sr = new ServiceRecorder();
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        sr.serviceAvailable(new BeanContextServiceAvailableEvent(bcss, Object.class));
        sr.serviceRevoked(new BeanContextServiceRevokedEvent(bcss, Object.class, true));
        check(sr.avail == 1 && sr.revoked == 1, "service hooks overridable and invoked");
    }

    static void testSerialization() throws Exception {
        // subclass state survives; transient beanContext does not
        Holder h = new Holder();
        BeanContextSupport bc = new BeanContextSupport();
        h.setBeanContext(bc);
        h.initCalls = 42;
        Holder back = deser(ser(h));
        check(back.initCalls == 42, "subclass state survives round-trip");
        check(back.getBeanContext() == null, "beanContext is transient: null after round-trip");
        check(back.getBeanContextChildPeer() == back, "self peer preserved after round-trip");
        check(!back.isDelegated(), "not delegated after round-trip");
        // serializable named listener survives and still fires
        Exposed e = new Exposed();
        SerialListener sl = new SerialListener();
        e.addPropertyChangeListener("q", sl);
        e.firePropertyChange("q", "1", "2");
        check(sl.n == 1, "listener armed before serialization");
        Exposed eBack = deser(ser(e));
        check(eBack.named("q").length == 1, "serializable named listener survives round-trip");
        SerialListener slBack = (SerialListener) eBack.named("q")[0];
        check(slBack.n == 1, "listener counter state traveled");
        eBack.firePropertyChange("q", "2", "3");
        check(slBack.n == 2, "deserialized listener still fires");
        // non-serializable delegated peer -> IOException on write
        NonSerChild nsPeer = new NonSerChild();
        BeanContextChildSupport del = new BeanContextChildSupport(nsPeer);
        try {
            ser(del);
            check(false, "non-serializable delegated peer must fail");
        } catch (IOException ioe) {
            check(true, "non-serializable delegated peer -> IOException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSetBeanContextEvents();
        testVetoPaths();
        testResourceHooks();
        testFirePropertyChange();
        testFireVetoableChange();
        testPeerDelegation();
        testNullRegistrations();
        testServiceHooks();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
