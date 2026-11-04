/*
 * @test
 * @summary java.beans.VetoableChangeListener conformance: exact interface
 *          inventory (single method vetoableChange(PropertyChangeEvent)
 *          declaring throws PropertyVetoException, extends
 *          java.util.EventListener), implementation forms, and
 *          VetoableChangeSupport integration: veto exception identity,
 *          later-listener skip, rollback events, notification order.
 * @run main VetoableChangeListenerTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class VetoableChangeListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    /** Non-vetoing recorder. */
    public static class Recorder implements VetoableChangeListener {
        final List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        public void vetoableChange(PropertyChangeEvent e) { events.add(e); }
    }

    /** Listener that throws a fixed veto exception (identity probe). */
    public static class Vetoer implements VetoableChangeListener {
        final PropertyVetoException veto;
        int calls;
        Vetoer(PropertyVetoException veto) { this.veto = veto; }
        public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
            calls++;
            if (veto != null) throw veto;
        }
    }

    static Recorder tagged(final String tag, final List<String> order) {
        return new Recorder() {
            public void vetoableChange(PropertyChangeEvent e) { order.add(tag); super.vetoableChange(e); }
        };
    }

    static void testLegacyInventory() throws Exception {
        // preserved original assertions
        Class<?> c = Class.forName("java.beans.VetoableChangeListener");
        check(c.isInterface(), "is an interface");
        check(java.util.EventListener.class.isAssignableFrom(c), "extends EventListener");
        final String[] got = new String[1];
        VetoableChangeListener l = new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) { got[0] = e.getPropertyName(); }
        };
        l.vetoableChange(new PropertyChangeEvent(new Object(), "p", null, null));
        check("p".equals(got[0]), "vetoableChange dispatches");
    }

    static void testInventory() throws Exception {
        Class<?> c = VetoableChangeListener.class;
        check(c.isInterface(), "VCL: is an interface");
        check(c.getDeclaredMethods().length == 1, "VCL: exactly one declared method");
        Method m = c.getDeclaredMethods()[0];
        check("vetoableChange".equals(m.getName()), "VCL: method name is vetoableChange");
        check(m.getReturnType() == void.class, "VCL: vetoableChange returns void");
        check(m.getParameterTypes().length == 1, "VCL: vetoableChange takes one parameter");
        check(m.getParameterTypes()[0] == PropertyChangeEvent.class, "VCL: parameter type is PropertyChangeEvent");
        check(m.getExceptionTypes().length == 1, "VCL: exactly one declared exception type");
        check(m.getExceptionTypes()[0] == PropertyVetoException.class, "VCL: declares throws PropertyVetoException");
        check(Modifier.isAbstract(m.getModifiers()), "VCL: method is abstract");
        check(Modifier.isPublic(m.getModifiers()), "VCL: method is public");
        check(c.getInterfaces().length == 1, "VCL: exactly one superinterface");
        check(c.getInterfaces()[0] == EventListener.class, "VCL: superinterface is java.util.EventListener");
        check(EventListener.class.isAssignableFrom(c), "VCL: assignable to EventListener");
    }

    static void testImplementations() throws PropertyVetoException {
        Object src = "source";
        Recorder named = new Recorder();
        PropertyChangeEvent e1 = new PropertyChangeEvent(src, "limit", "10", "20");
        named.vetoableChange(e1);
        check(named.events.size() == 1, "named impl invoked");
        check(named.events.get(0) == e1, "named impl receives the exact event object");
        check(named.events.get(0).getSource() == src, "event source preserved");
        check("limit".equals(named.events.get(0).getPropertyName()), "event property name preserved");
        check("10".equals(named.events.get(0).getOldValue()), "event oldValue preserved");
        check("20".equals(named.events.get(0).getNewValue()), "event newValue preserved");
        named.vetoableChange(e1);
        check(named.events.size() == 2, "each vetoableChange call appends exactly one event");

        final String[] anon = new String[1];
        VetoableChangeListener anonymous = new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) { anon[0] = e.getPropertyName(); }
        };
        anonymous.vetoableChange(new PropertyChangeEvent(src, "anon", null, null));
        check("anon".equals(anon[0]), "anonymous impl invoked");

        final List<String> lambdaSeen = new ArrayList<String>();
        VetoableChangeListener lambda = e -> lambdaSeen.add(e.getPropertyName());
        lambda.vetoableChange(new PropertyChangeEvent(src, "lambda", null, null));
        check(lambdaSeen.size() == 1 && "lambda".equals(lambdaSeen.get(0)), "lambda impl invoked");

        PropertyVetoException veto = new PropertyVetoException("no way", e1);
        Vetoer vetoer = new Vetoer(veto);
        try {
            vetoer.vetoableChange(e1);
            check(false, "vetoer must throw");
        } catch (PropertyVetoException pve) {
            check(pve == veto, "veto exception identity on direct call");
        }
        check(vetoer.calls == 1, "vetoer counted exactly one call");
    }

    static void testSupportPassPath() throws Exception {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        final List<String> order = new ArrayList<String>();
        Recorder l1 = tagged("l1", order);
        Recorder l2 = tagged("l2", order);
        Recorder l3 = tagged("l3", order);
        vcs.addVetoableChangeListener(l1);
        vcs.addVetoableChangeListener(l2);
        vcs.addVetoableChangeListener(l3);
        check(vcs.getVetoableChangeListeners().length == 3, "three listeners registered");
        PropertyChangeEvent ev = new PropertyChangeEvent(src, "p", "a", "b");
        vcs.fireVetoableChange(ev);
        check(order.size() == 3, "all listeners notified exactly once on pass path");
        check("l1".equals(order.get(0)) && "l2".equals(order.get(1)) && "l3".equals(order.get(2)),
                "notification order == registration order");
        check(l1.events.get(0) == ev && l2.events.get(0) == ev && l3.events.get(0) == ev,
                "all listeners receive the SAME event object");
        vcs.fireVetoableChange("q", 3, 5);
        check(order.size() == 6, "second fire notifies all again");
        check(l1.events.get(1).getPropertyName().equals("q")
                && Integer.valueOf(3).equals(l1.events.get(1).getOldValue())
                && Integer.valueOf(5).equals(l1.events.get(1).getNewValue()),
                "int overload boxes values as Integer for vetoable listeners");
        vcs.fireVetoableChange("q", 5, 5);
        check(order.size() == 6, "equal old/new values are not fired to vetoable listeners");
        vcs.removeVetoableChangeListener(l2);
        vcs.fireVetoableChange("p", "b", "c");
        check(order.size() == 8 && "l1".equals(order.get(6)) && "l3".equals(order.get(7)),
                "removed listener no longer notified");
        vcs.removeVetoableChangeListener(l2);
        check(vcs.getVetoableChangeListeners().length == 2, "repeated remove is a no-op");
        check(vcs.hasListeners("anything"), "hasListeners(name) true while global listeners exist");
        Recorder named = new Recorder();
        vcs.addVetoableChangeListener("special", named);
        vcs.fireVetoableChange("special", 1, 2);
        check(named.events.size() == 1, "named vetoable listener fired for its own name");
        vcs.fireVetoableChange("other", 1, 2);
        check(named.events.size() == 1, "named vetoable listener not fired for a different name");
    }

    static void testVetoPath() throws Exception {
        Object src = new Object();
        // vetoer in the middle: earlier listeners run, later listeners skipped
        Recorder first = new Recorder();
        PropertyVetoException veto = new PropertyVetoException("no", new PropertyChangeEvent(src, "p", "OLD", "NEW"));
        Vetoer vetoer = new Vetoer(veto);
        Recorder after = new Recorder();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        vcs.addVetoableChangeListener(first);
        vcs.addVetoableChangeListener(vetoer);
        vcs.addVetoableChangeListener(after);
        PropertyVetoException caught = null;
        try {
            vcs.fireVetoableChange("p", "OLD", "NEW");
        } catch (PropertyVetoException pve) {
            caught = pve;
        }
        check(caught != null, "veto propagated to caller");
        check(caught == veto, "caller receives THE SAME exception object");
        check(vetoer.calls == 1, "vetoer invoked exactly once");
        check(first.events.size() >= 1, "earlier listener invoked");
        check(after.events.size() == 0, "listener registered after the vetoer is NOT invoked");
        // rollback: the earlier listener observes a reversal event (NEW->OLD)
        check(first.events.size() == 2, "earlier listener also receives the automatic rollback event");
        PropertyChangeEvent rb = first.events.get(1);
        check("p".equals(rb.getPropertyName()), "rollback event keeps the property name");
        check("NEW".equals(rb.getOldValue()) && "OLD".equals(rb.getNewValue()),
                "rollback event reverses old and new values");

        // vetoer first: everyone after it is skipped
        PropertyVetoException veto2 = new PropertyVetoException("no2", new PropertyChangeEvent(src, "q", "a", "b"));
        Vetoer firstVetoer = new Vetoer(veto2);
        Recorder late = new Recorder();
        VetoableChangeSupport vcs2 = new VetoableChangeSupport(src);
        vcs2.addVetoableChangeListener(firstVetoer);
        vcs2.addVetoableChangeListener(late);
        PropertyVetoException caught2 = null;
        try {
            vcs2.fireVetoableChange("q", "a", "b");
        } catch (PropertyVetoException pve) {
            caught2 = pve;
        }
        check(caught2 == veto2, "vetoer-first: same exception object to caller");
        check(late.events.size() == 0, "vetoer-first: later listener not invoked");
        check(firstVetoer.calls == 1, "vetoer-first: vetoer invoked exactly once");

        // non-PropertyVetoException from a listener propagates unwrapped
        final RuntimeException boom = new IllegalStateException("boom");
        Recorder bad = new Recorder() {
            public void vetoableChange(PropertyChangeEvent e) { throw boom; }
        };
        VetoableChangeSupport vcs3 = new VetoableChangeSupport(src);
        vcs3.addVetoableChangeListener(bad);
        RuntimeException caughtRte = null;
        try {
            vcs3.fireVetoableChange("p", "a", "b");
        } catch (RuntimeException rte) {
            caughtRte = rte;
        }
        check(caughtRte == boom, "RuntimeException from listener propagates unwrapped with identity");
    }

    public static void main(String[] args) throws Exception {
        testLegacyInventory();
        testInventory();
        testImplementations();
        testSupportPassPath();
        testVetoPath();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
