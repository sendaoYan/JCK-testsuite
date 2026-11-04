/*
 * @test
 * @summary java.beans.PropertyChangeListenerProxy conformance: full contract.
 *          extends EventListenerProxy and implements PropertyChangeListener,
 *          null listener -> NPE, getPropertyName / getListener identity, and
 *          propertyChange delegation to the wrapped listener.
 *          Deepened: exact inventory, name semantics (null/empty/verbatim),
 *          global-registration dispatch through PropertyChangeSupport with
 *          name filtering and same-event forwarding, unwrapping registration
 *          and removal, identity equals/hashCode, nested proxies, named
 *          registration, and mixing with plain listeners.
 * @run main PropertyChangeListenerProxyTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventListenerProxy;
import java.util.List;

public class PropertyChangeListenerProxyTest {
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

    static class Recorder implements PropertyChangeListener {
        final List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        public void propertyChange(PropertyChangeEvent e) { events.add(e); }
    }

    static void testStructure() {
        Class<?> c = PropertyChangeListenerProxy.class;
        check(EventListenerProxy.class.isAssignableFrom(c), "extends EventListenerProxy");
        check(PropertyChangeListener.class.isAssignableFrom(c), "implements PropertyChangeListener");
    }

    static void testBasics() {
        final String[] got = new String[1];
        PropertyChangeListener inner = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { got[0] = e.getPropertyName(); }
        };
        PropertyChangeListenerProxy proxy = new PropertyChangeListenerProxy("foo", inner);
        check("foo".equals(proxy.getPropertyName()), "getPropertyName");
        check(inner == proxy.getListener(), "getListener identity");

        proxy.propertyChange(new PropertyChangeEvent(new Object(), "bar", null, null));
        check("bar".equals(got[0]), "delegates propertyChange");

        PropertyChangeListenerProxy nullProxy = new PropertyChangeListenerProxy("foo", null);
        check(nullProxy.getListener() == null, "null listener accepted (getListener null)");
    }

    static void testInventory() throws Exception {
        Class<?> c = PropertyChangeListenerProxy.class;
        check(!c.isInterface(), "PCLP: class, not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "PCLP: concrete (instantiable)");
        check(Modifier.isPublic(c.getModifiers()), "PCLP: public class");
        check(c.getSuperclass() == EventListenerProxy.class, "PCLP: raw superclass is java.util.EventListenerProxy");
        check(PropertyChangeListener.class.isAssignableFrom(c), "PCLP: implements PropertyChangeListener");
        check(EventListener.class.isAssignableFrom(c), "PCLP: assignable to EventListener");
        int found = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (!Modifier.isPublic(m.getModifiers())) continue;
            if ("propertyChange".equals(m.getName())) {
                check(m.getReturnType() == void.class, "PCLP: propertyChange returns void");
                check(m.getParameterTypes().length == 1
                        && m.getParameterTypes()[0] == PropertyChangeEvent.class,
                        "PCLP: propertyChange(PropertyChangeEvent)");
                found |= 1;
            } else if ("getPropertyName".equals(m.getName())) {
                check(m.getReturnType() == String.class, "PCLP: getPropertyName returns String");
                check(m.getParameterTypes().length == 0, "PCLP: getPropertyName takes no arguments");
                found |= 2;
            }
        }
        check(found == 3, "PCLP: public API is exactly propertyChange + getPropertyName");
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 1, "PCLP: exactly one public constructor");
        Class<?>[] ps = ctors[0].getParameterTypes();
        check(ps.length == 2 && ps[0] == String.class && ps[1] == PropertyChangeListener.class,
                "PCLP: constructor (String, PropertyChangeListener)");
        check(!java.io.Serializable.class.isAssignableFrom(c), "PCLP: not Serializable");
    }

    static void testNameSemantics() {
        Recorder r = new Recorder();
        check("".equals(new PropertyChangeListenerProxy("", r).getPropertyName()),
                "PCLP: empty property name preserved");
        check(new PropertyChangeListenerProxy("", r).getListener() == r,
                "PCLP: getListener returns the exact object given (empty name)");
        PropertyChangeListenerProxy nullName = new PropertyChangeListenerProxy(null, r);
        check(nullName.getPropertyName() == null, "PCLP: null property name accepted");
        check(nullName.getListener() == r, "PCLP: getListener identity with null name");
        check("a b c".equals(new PropertyChangeListenerProxy("a b c", r).getPropertyName()),
                "PCLP: arbitrary property name preserved verbatim");
        PropertyChangeListenerProxy px = new PropertyChangeListenerProxy("x", r);
        check(px instanceof EventListenerProxy, "PCLP: instanceof EventListenerProxy");
        check(px instanceof PropertyChangeListener, "PCLP: instanceof PropertyChangeListener");
        check(px instanceof EventListener, "PCLP: instanceof EventListener");
    }

    static void testGlobalRegistrationDispatch() {
        Object src = new Object();
        Recorder inner = new Recorder();
        PropertyChangeListenerProxy proxy = new PropertyChangeListenerProxy("pay", inner);
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        pcs.addPropertyChangeListener(proxy);
        check(pcs.hasListeners("pay"), "global proxy add registers under the proxy's name");
        check(!pcs.hasListeners("other"), "hasListeners false for a different name");
        PropertyChangeListener[] arr = pcs.getPropertyChangeListeners();
        check(arr.length == 1, "single listener after global proxy registration");
        check(arr[0] instanceof PropertyChangeListenerProxy, "returned listener is (re-)wrapped in a proxy");
        check(arr[0] != proxy, "returned proxy is a fresh wrapper, not the registered object");
        PropertyChangeListenerProxy back = (PropertyChangeListenerProxy) arr[0];
        check("pay".equals(back.getPropertyName()), "rewrapped proxy keeps the property name");
        check(back.getListener() == inner, "rewrapped proxy keeps the inner listener identity");
        PropertyChangeEvent ev = new PropertyChangeEvent(src, "pay", "o", "n");
        pcs.firePropertyChange(ev);
        check(inner.events.size() == 1, "matching property name forwarded to inner listener");
        check(inner.events.get(0) == ev, "forwarded event is the SAME object that was fired");
        pcs.firePropertyChange("other", "o", "n");
        check(inner.events.size() == 1, "non-matching property name NOT forwarded");
        pcs.firePropertyChange(new PropertyChangeEvent(src, null, "o", "n"));
        check(inner.events.size() == 1, "null property name NOT forwarded to named proxy");
        PropertyChangeListenerProxy otherSameInner = new PropertyChangeListenerProxy("pay", inner);
        pcs.removePropertyChangeListener(otherSameInner);
        check(pcs.getPropertyChangeListeners().length == 0,
                "remove via a different proxy wrapping the same inner removes (unwrapping)");
        pcs.removePropertyChangeListener(proxy);
        check(pcs.getPropertyChangeListeners().length == 0, "repeated remove is a no-op");
        check(!pcs.hasListeners("pay"), "hasListeners false after removal");
    }

    static void testDirectDelegation() {
        Recorder inner = new Recorder();
        PropertyChangeListenerProxy proxy = new PropertyChangeListenerProxy("n", inner);
        PropertyChangeEvent any = new PropertyChangeEvent("s", "whatever", Integer.valueOf(1), Integer.valueOf(2));
        proxy.propertyChange(any);
        check(inner.events.size() == 1 && inner.events.get(0) == any,
                "direct call forwards unconditionally (name is not checked)");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() {
                    new PropertyChangeListenerProxy("n", null).propertyChange(
                            new PropertyChangeEvent("s", "n", null, null)); } },
                "null listener -> NPE on dispatch");
        final RuntimeException boom = new IllegalStateException("boom");
        Recorder bad = new Recorder() {
            public void propertyChange(PropertyChangeEvent e) { throw boom; }
        };
        PropertyChangeListenerProxy badProxy = new PropertyChangeListenerProxy("n", bad);
        RuntimeException caught = null;
        try { badProxy.propertyChange(any); } catch (RuntimeException rte) { caught = rte; }
        check(caught == boom, "RuntimeException propagates with identity through the proxy");
    }

    static void testEqualsHashToString() {
        Recorder r = new Recorder();
        PropertyChangeListenerProxy a = new PropertyChangeListenerProxy("x", r);
        PropertyChangeListenerProxy b = new PropertyChangeListenerProxy("x", r);
        check(a.equals(a), "equals is reflexive");
        check(!a.equals(b), "equals is identity, not value equality");
        check(!a.equals(null), "equals(null) is false");
        check(!a.equals(r), "equals an unrelated object is false");
        check(a.hashCode() == a.hashCode(), "hashCode is stable across calls");
        check(a.toString() != null && a.toString().length() > 0, "toString non-empty");
    }

    static void testNestedProxy() {
        Recorder leaf = new Recorder();
        PropertyChangeListenerProxy innerP = new PropertyChangeListenerProxy("in", leaf);
        PropertyChangeListenerProxy outerP = new PropertyChangeListenerProxy("out", innerP);
        check(outerP.getListener() == innerP, "outer proxy wraps the inner proxy");
        check(innerP.getListener() == leaf, "inner proxy wraps the leaf listener");
        PropertyChangeEvent e1 = new PropertyChangeEvent("s", "zz", Integer.valueOf(1), Integer.valueOf(2));
        outerP.propertyChange(e1);
        check(leaf.events.size() == 1 && leaf.events.get(0) == e1,
                "direct call double-delegates down to the leaf listener");
        PropertyChangeSupport pcs = new PropertyChangeSupport("s");
        pcs.addPropertyChangeListener(outerP);
        check(pcs.hasListeners("out"), "nested registration is keyed by the OUTER name");
        check(!pcs.hasListeners("in"), "inner name is not registered");
        pcs.firePropertyChange("out", 1, 2);
        check(leaf.events.size() == 2, "fire(outer) reaches the leaf through both proxy levels");
        pcs.firePropertyChange("in", 1, 2);
        check(leaf.events.size() == 2, "fire(inner) does not reach the leaf");
    }

    static void testNamedRegistration() {
        Recorder inner = new Recorder();
        PropertyChangeListenerProxy proxy = new PropertyChangeListenerProxy("srcName", inner);
        PropertyChangeSupport pcs = new PropertyChangeSupport("s");
        pcs.addPropertyChangeListener("target", proxy);
        check(pcs.hasListeners("target"), "named add stores under the registration name");
        check(!pcs.hasListeners("srcName"), "proxy's own name is not registered by a named add");
        pcs.firePropertyChange("target", 1, 2);
        check(inner.events.size() == 1, "fire(target) invokes the inner (proxy delegates unconditionally)");
        pcs.firePropertyChange("srcName", 1, 2);
        check(inner.events.size() == 1, "fire(srcName) is not delivered");
    }

    static void testMixedWithPlainListener() {
        Object src = new Object();
        Recorder inner = new Recorder();
        Recorder plain = new Recorder();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        pcs.addPropertyChangeListener(new PropertyChangeListenerProxy("a", inner));
        pcs.addPropertyChangeListener(plain);
        pcs.firePropertyChange("a", 1, 2);
        check(inner.events.size() == 1, "proxy receives the matching event");
        check(plain.events.size() == 1, "plain global listener receives every event");
        pcs.firePropertyChange("b", 1, 2);
        check(inner.events.size() == 1, "proxy filters out the non-matching event");
        check(plain.events.size() == 2, "plain listener still notified for the other name");
        Recorder i2 = new Recorder();
        pcs.addPropertyChangeListener(new PropertyChangeListenerProxy("b", i2));
        pcs.firePropertyChange("b", 2, 3);
        check(inner.events.size() == 1, "first proxy still filtered");
        check(i2.events.size() == 1, "second proxy receives its own name");
        check(plain.events.size() == 3, "plain listener notified once more");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testBasics();
        testInventory();
        testNameSemantics();
        testGlobalRegistrationDispatch();
        testDirectDelegation();
        testEqualsHashToString();
        testNestedProxy();
        testNamedRegistration();
        testMixedWithPlainListener();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
