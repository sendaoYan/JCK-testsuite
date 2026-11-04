/*
 * @test
 * @summary java.beans.VetoableChangeListenerProxy conformance: full contract.
 *          extends EventListenerProxy and implements VetoableChangeListener;
 *          getPropertyName / getListener identity; direct delegation;
 *          global-registration dispatch through VetoableChangeSupport with
 *          name filtering; veto exception propagates through the proxy
 *          with identity; unwrapping registration/removal; identity
 *          equals/hashCode; nested proxies; named registration.
 * @run main VetoableChangeListenerProxyTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeListenerProxy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventListenerProxy;
import java.util.List;

public class VetoableChangeListenerProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
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

    static class Recorder implements VetoableChangeListener {
        final List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException { events.add(e); }
    }

    static void testLegacyStructure() throws Exception {
        // preserved original assertions
        Class<?> c = Class.forName("java.beans.VetoableChangeListenerProxy");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.util.EventListenerProxy".equals(c.getSuperclass().getName()), "superclass java.util.EventListenerProxy");
        check(Class.forName("java.beans.VetoableChangeListener").isAssignableFrom(c), "implements/extends java.beans.VetoableChangeListener");
        check(hasMethod(c, "getPropertyName"), "declares getPropertyName");
        check(hasMethod(c, "vetoableChange"), "declares vetoableChange");
    }

    static void testInventory() {
        Class<?> c = VetoableChangeListenerProxy.class;
        check(!c.isInterface(), "VCLP: class, not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "VCLP: concrete (instantiable)");
        check(Modifier.isPublic(c.getModifiers()), "VCLP: public class");
        check(c.getSuperclass() == EventListenerProxy.class, "VCLP: raw superclass is java.util.EventListenerProxy");
        check(VetoableChangeListener.class.isAssignableFrom(c), "VCLP: implements VetoableChangeListener");
        check(EventListener.class.isAssignableFrom(c), "VCLP: assignable to EventListener");
        int found = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (!Modifier.isPublic(m.getModifiers())) continue;
            if ("vetoableChange".equals(m.getName())) {
                check(m.getReturnType() == void.class, "VCLP: vetoableChange returns void");
                check(m.getParameterTypes().length == 1
                        && m.getParameterTypes()[0] == PropertyChangeEvent.class,
                        "VCLP: vetoableChange(PropertyChangeEvent)");
                found |= 1;
            } else if ("getPropertyName".equals(m.getName())) {
                check(m.getReturnType() == String.class, "VCLP: getPropertyName returns String");
                check(m.getParameterTypes().length == 0, "VCLP: getPropertyName takes no arguments");
                found |= 2;
            }
        }
        check(found == 3, "VCLP: public API is exactly vetoableChange + getPropertyName");
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 1, "VCLP: exactly one public constructor");
        Class<?>[] ps = ctors[0].getParameterTypes();
        check(ps.length == 2 && ps[0] == String.class && ps[1] == VetoableChangeListener.class,
                "VCLP: constructor (String, VetoableChangeListener)");
        check(!java.io.Serializable.class.isAssignableFrom(c), "VCLP: not Serializable");
    }

    static void testNameSemantics() {
        Recorder r = new Recorder();
        check("pay".equals(new VetoableChangeListenerProxy("pay", r).getPropertyName()),
                "VCLP: property name preserved");
        check(new VetoableChangeListenerProxy("pay", r).getListener() == r,
                "VCLP: getListener returns the exact object given");
        check("".equals(new VetoableChangeListenerProxy("", r).getPropertyName()),
                "VCLP: empty property name preserved");
        VetoableChangeListenerProxy nullName = new VetoableChangeListenerProxy(null, r);
        check(nullName.getPropertyName() == null, "VCLP: null property name accepted");
        check(nullName.getListener() == r, "VCLP: getListener identity with null name");
        check(new VetoableChangeListenerProxy("foo", null).getListener() == null,
                "VCLP: null listener accepted at construction");
        VetoableChangeListenerProxy px = new VetoableChangeListenerProxy("x", r);
        check(px instanceof EventListenerProxy, "VCLP: instanceof EventListenerProxy");
        check(px instanceof VetoableChangeListener, "VCLP: instanceof VetoableChangeListener");
        check(px instanceof EventListener, "VCLP: instanceof EventListener");
    }

    static void testGlobalRegistrationDispatch() throws Exception {
        Object src = new Object();
        Recorder inner = new Recorder();
        VetoableChangeListenerProxy proxy = new VetoableChangeListenerProxy("pay", inner);
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        vcs.addVetoableChangeListener(proxy);
        check(vcs.hasListeners("pay"), "global proxy add registers under the proxy's name");
        check(!vcs.hasListeners("other"), "hasListeners false for a different name");
        VetoableChangeListener[] arr = vcs.getVetoableChangeListeners();
        check(arr.length == 1, "single listener after global proxy registration");
        check(arr[0] instanceof VetoableChangeListenerProxy, "returned listener is (re-)wrapped in a proxy");
        check(arr[0] != proxy, "returned proxy is a fresh wrapper, not the registered object");
        VetoableChangeListenerProxy back = (VetoableChangeListenerProxy) arr[0];
        check("pay".equals(back.getPropertyName()), "rewrapped proxy keeps the property name");
        check(back.getListener() == inner, "rewrapped proxy keeps the inner listener identity");
        PropertyChangeEvent ev = new PropertyChangeEvent(src, "pay", "o", "n");
        vcs.fireVetoableChange(ev);
        check(inner.events.size() == 1, "matching property name forwarded to inner listener");
        check(inner.events.get(0) == ev, "forwarded event is the SAME object that was fired");
        vcs.fireVetoableChange("other", "o", "n");
        check(inner.events.size() == 1, "non-matching property name NOT forwarded");
        vcs.fireVetoableChange(new PropertyChangeEvent(src, null, "o", "n"));
        check(inner.events.size() == 1, "null property name NOT forwarded to named proxy");
        VetoableChangeListenerProxy otherSameInner = new VetoableChangeListenerProxy("pay", inner);
        vcs.removeVetoableChangeListener(otherSameInner);
        check(vcs.getVetoableChangeListeners().length == 0,
                "remove via a different proxy wrapping the same inner removes (unwrapping)");
        vcs.removeVetoableChangeListener(proxy);
        check(vcs.getVetoableChangeListeners().length == 0, "repeated remove is a no-op");
        check(!vcs.hasListeners("pay"), "hasListeners false after removal");
    }

    static void testVetoThroughProxy() throws Exception {
        Object src = new Object();
        final PropertyVetoException veto =
                new PropertyVetoException("no", new PropertyChangeEvent(src, "pay", "o", "n"));
        Recorder vetoInner = new Recorder() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                super.vetoableChange(e);
                throw veto;
            }
        };
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        vcs.addVetoableChangeListener(new VetoableChangeListenerProxy("pay", vetoInner));
        PropertyVetoException caught = null;
        try {
            vcs.fireVetoableChange("pay", "o", "n");
        } catch (PropertyVetoException pve) {
            caught = pve;
        }
        check(caught == veto, "veto propagates through the proxy unchanged (identity)");
        check(vetoInner.events.size() == 1, "vetoing inner listener invoked exactly once");
        // direct call: the proxy rethrows the very same exception object
        VetoableChangeListenerProxy direct = new VetoableChangeListenerProxy("pay", vetoInner);
        PropertyVetoException caught2 = null;
        try {
            direct.vetoableChange(new PropertyChangeEvent(src, "pay", "o", "n"));
        } catch (PropertyVetoException pve) {
            caught2 = pve;
        }
        check(caught2 == veto, "direct proxy call rethrows the same veto exception");
        check(vetoInner.events.size() == 2, "direct call reached the inner listener");
    }

    static void testDirectDelegation() throws Exception {
        Recorder inner = new Recorder();
        VetoableChangeListenerProxy proxy = new VetoableChangeListenerProxy("n", inner);
        PropertyChangeEvent any = new PropertyChangeEvent("s", "whatever", Integer.valueOf(1), Integer.valueOf(2));
        proxy.vetoableChange(any);
        check(inner.events.size() == 1 && inner.events.get(0) == any,
                "direct call forwards unconditionally (name is not checked)");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new VetoableChangeListenerProxy("n", null).vetoableChange(
                            new PropertyChangeEvent("s", "n", null, null)); } },
                "null listener -> NPE on dispatch");
        final RuntimeException boom = new IllegalStateException("boom");
        Recorder bad = new Recorder() {
            public void vetoableChange(PropertyChangeEvent e) { throw boom; }
        };
        VetoableChangeListenerProxy badProxy = new VetoableChangeListenerProxy("n", bad);
        RuntimeException caught = null;
        try { badProxy.vetoableChange(any); } catch (RuntimeException rte) { caught = rte; }
        check(caught == boom, "RuntimeException propagates with identity through the proxy");
    }

    static void testEqualsHashToString() {
        Recorder r = new Recorder();
        VetoableChangeListenerProxy a = new VetoableChangeListenerProxy("x", r);
        VetoableChangeListenerProxy b = new VetoableChangeListenerProxy("x", r);
        check(a.equals(a), "equals is reflexive");
        check(!a.equals(b), "equals is identity, not value equality");
        check(!a.equals(null), "equals(null) is false");
        check(!a.equals(r), "equals an unrelated object is false");
        check(a.hashCode() == a.hashCode(), "hashCode is stable across calls");
        check(a.toString() != null && a.toString().length() > 0, "toString non-empty");
    }

    static void testNestedProxy() throws Exception {
        Recorder leaf = new Recorder();
        VetoableChangeListenerProxy innerP = new VetoableChangeListenerProxy("in", leaf);
        VetoableChangeListenerProxy outerP = new VetoableChangeListenerProxy("out", innerP);
        check(outerP.getListener() == innerP, "outer proxy wraps the inner proxy");
        check(innerP.getListener() == leaf, "inner proxy wraps the leaf listener");
        PropertyChangeEvent e1 = new PropertyChangeEvent("s", "zz", Integer.valueOf(1), Integer.valueOf(2));
        outerP.vetoableChange(e1);
        check(leaf.events.size() == 1 && leaf.events.get(0) == e1,
                "direct call double-delegates down to the leaf listener");
        VetoableChangeSupport vcs = new VetoableChangeSupport("s");
        vcs.addVetoableChangeListener(outerP);
        check(vcs.hasListeners("out"), "nested registration is keyed by the OUTER name");
        check(!vcs.hasListeners("in"), "inner name is not registered");
        vcs.fireVetoableChange("out", 1, 2);
        check(leaf.events.size() == 2, "fire(outer) reaches the leaf through both proxy levels");
        vcs.fireVetoableChange("in", 1, 2);
        check(leaf.events.size() == 2, "fire(inner) does not reach the leaf");
    }

    static void testNamedRegistration() throws Exception {
        Recorder inner = new Recorder();
        VetoableChangeListenerProxy proxy = new VetoableChangeListenerProxy("srcName", inner);
        VetoableChangeSupport vcs = new VetoableChangeSupport("s");
        vcs.addVetoableChangeListener("target", proxy);
        check(vcs.hasListeners("target"), "named add stores under the registration name");
        check(!vcs.hasListeners("srcName"), "proxy's own name is not registered by a named add");
        vcs.fireVetoableChange("target", 1, 2);
        check(inner.events.size() == 1, "fire(target) invokes the inner (proxy delegates unconditionally)");
        vcs.fireVetoableChange("srcName", 1, 2);
        check(inner.events.size() == 1, "fire(srcName) is not delivered");
    }

    static void testMixedWithPlainListener() throws Exception {
        Object src = new Object();
        Recorder inner = new Recorder();
        Recorder plain = new Recorder();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        vcs.addVetoableChangeListener(new VetoableChangeListenerProxy("a", inner));
        vcs.addVetoableChangeListener(plain);
        vcs.fireVetoableChange("a", 1, 2);
        check(inner.events.size() == 1, "proxy receives the matching event");
        check(plain.events.size() == 1, "plain global listener receives every event");
        vcs.fireVetoableChange("b", 1, 2);
        check(inner.events.size() == 1, "proxy filters out the non-matching event");
        check(plain.events.size() == 2, "plain listener still notified for the other name");
        Recorder i2 = new Recorder();
        vcs.addVetoableChangeListener(new VetoableChangeListenerProxy("b", i2));
        vcs.fireVetoableChange("b", 2, 3);
        check(inner.events.size() == 1, "first proxy still filtered");
        check(i2.events.size() == 1, "second proxy receives its own name");
        check(plain.events.size() == 3, "plain listener notified once more");
    }

    public static void main(String[] args) throws Exception {
        testLegacyStructure();
        testInventory();
        testNameSemantics();
        testGlobalRegistrationDispatch();
        testVetoThroughProxy();
        testDirectDelegation();
        testEqualsHashToString();
        testNestedProxy();
        testNamedRegistration();
        testMixedWithPlainListener();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
