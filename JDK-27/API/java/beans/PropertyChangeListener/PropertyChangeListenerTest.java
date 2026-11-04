/*
 * @test
 * @summary java.beans.PropertyChangeListener conformance: exact interface
 *          inventory (single method propertyChange(PropertyChangeEvent),
 *          extends java.util.EventListener), named/anonymous/lambda/
 *          method-reference implementations with field-by-field event
 *          assertions, and PropertyChangeSupport integration where
 *          registration order == notification order.
 * @run main PropertyChangeListenerTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class PropertyChangeListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    /** Named-class implementation recording every event it receives. */
    public static class Recorder implements PropertyChangeListener {
        final List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        public void propertyChange(PropertyChangeEvent e) { events.add(e); }
    }

    /** Static method-reference target. */
    public static class Names {
        static String last;
        static void note(PropertyChangeEvent e) { last = e.getPropertyName(); }
    }

    static Recorder tagged(final String tag, final List<String> order) {
        return new Recorder() {
            public void propertyChange(PropertyChangeEvent e) { order.add(tag); super.propertyChange(e); }
        };
    }

    static void testLegacyInventory() throws Exception {
        // preserved original assertions
        Class<?> c = Class.forName("java.beans.PropertyChangeListener");
        check(c.isInterface(), "is an interface");
        check(java.util.EventListener.class.isAssignableFrom(c), "extends EventListener");
        final String[] got = new String[1];
        PropertyChangeListener l = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { got[0] = e.getPropertyName(); }
        };
        l.propertyChange(new PropertyChangeEvent(new Object(), "p", null, null));
        check("p".equals(got[0]), "propertyChange dispatches");
    }

    static void testInventory() throws Exception {
        Class<?> c = PropertyChangeListener.class;
        check(c.isInterface(), "PCL: is an interface");
        check(c.getDeclaredMethods().length == 1, "PCL: exactly one declared method");
        Method m = c.getDeclaredMethods()[0];
        check("propertyChange".equals(m.getName()), "PCL: method name is propertyChange");
        check(m.getReturnType() == void.class, "PCL: propertyChange returns void");
        check(m.getParameterTypes().length == 1, "PCL: propertyChange takes one parameter");
        check(m.getParameterTypes()[0] == PropertyChangeEvent.class, "PCL: parameter type is PropertyChangeEvent");
        check(Modifier.isAbstract(m.getModifiers()), "PCL: method is abstract");
        check(Modifier.isPublic(m.getModifiers()), "PCL: method is public");
        check(c.getInterfaces().length == 1, "PCL: exactly one superinterface");
        check(c.getInterfaces()[0] == EventListener.class, "PCL: superinterface is java.util.EventListener");
        check(EventListener.class.isAssignableFrom(c), "PCL: assignable to EventListener");
        check(PropertyChangeEvent.class.isAssignableFrom(m.getParameterTypes()[0]), "PCL: parameter is a PropertyChangeEvent type");
    }

    static void testImplementations() {
        Object src = "source";
        Recorder named = new Recorder();
        PropertyChangeEvent e1 = new PropertyChangeEvent(src, "amount", Integer.valueOf(1), Integer.valueOf(2));
        named.propertyChange(e1);
        check(named.events.size() == 1, "named impl invoked");
        check(named.events.get(0) == e1, "named impl receives the exact event object");
        check(named.events.get(0).getSource() == src, "event source preserved");
        check("amount".equals(named.events.get(0).getPropertyName()), "event property name preserved");
        check(Integer.valueOf(1).equals(named.events.get(0).getOldValue()), "event oldValue preserved");
        check(Integer.valueOf(2).equals(named.events.get(0).getNewValue()), "event newValue preserved");
        check(named.events.get(0).getPropagationId() == null, "default propagationId is null");
        named.propertyChange(e1);
        check(named.events.size() == 2, "each propertyChange call appends exactly one event");

        final String[] anon = new String[1];
        PropertyChangeListener anonymous = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { anon[0] = e.getPropertyName(); }
        };
        anonymous.propertyChange(new PropertyChangeEvent(src, "anon", null, null));
        check("anon".equals(anon[0]), "anonymous impl invoked");

        final List<String> lambdaSeen = new ArrayList<String>();
        PropertyChangeListener lambda = e -> lambdaSeen.add(e.getPropertyName());
        lambda.propertyChange(new PropertyChangeEvent(src, "lambda", null, null));
        check(lambdaSeen.size() == 1 && "lambda".equals(lambdaSeen.get(0)), "lambda impl invoked");

        Names.last = null;
        PropertyChangeListener methodRef = Names::note;
        methodRef.propertyChange(new PropertyChangeEvent(src, "mref", null, null));
        check("mref".equals(Names.last), "method-reference impl invoked");
    }

    static void testSupportIntegration() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        final List<String> order = new ArrayList<String>();
        Recorder l1 = tagged("l1", order);
        Recorder l2 = tagged("l2", order);
        Recorder l3 = tagged("l3", order);
        pcs.addPropertyChangeListener(l1);
        pcs.addPropertyChangeListener(l2);
        pcs.addPropertyChangeListener(l3);
        check(pcs.getPropertyChangeListeners().length == 3, "three listeners registered");
        PropertyChangeEvent ev = new PropertyChangeEvent(src, "p", "a", "b");
        pcs.firePropertyChange(ev);
        check(order.size() == 3, "all listeners notified exactly once");
        check("l1".equals(order.get(0)) && "l2".equals(order.get(1)) && "l3".equals(order.get(2)),
                "notification order == registration order");
        check(l1.events.get(0) == ev && l2.events.get(0) == ev && l3.events.get(0) == ev,
                "all listeners receive the SAME event object");
        pcs.firePropertyChange("p", "b", "c");
        check(order.size() == 6 && "l1".equals(order.get(3)) && "l2".equals(order.get(4)) && "l3".equals(order.get(5)),
                "order stable across repeated fires");
        pcs.removePropertyChangeListener(l2);
        pcs.firePropertyChange("p", "c", "d");
        check(order.size() == 8 && "l1".equals(order.get(6)) && "l3".equals(order.get(7)),
                "removed listener no longer notified");
        check(l2.events.size() == 2, "removed listener saw exactly the pre-removal events");
        pcs.removePropertyChangeListener(l2);
        check(pcs.getPropertyChangeListeners().length == 2, "repeated remove is a no-op");
        check(pcs.hasListeners("anything"), "hasListeners(name) true while global listeners exist");

        Recorder named = new Recorder();
        pcs.addPropertyChangeListener("special", named);
        check(pcs.hasListeners("special"), "hasListeners true for the named registration");
        pcs.firePropertyChange("special", 1, 2);
        check(named.events.size() == 1, "named listener fired for its own name");
        pcs.firePropertyChange("other", 1, 2);
        check(named.events.size() == 1, "named listener not fired for a different name");

        Recorder g = new Recorder();
        pcs.addPropertyChangeListener(g);
        pcs.firePropertyChange(new PropertyChangeEvent(src, null, "a", "b"));
        check(g.events.size() == 1, "null property name still notifies global listeners");
        check(g.events.get(0).getPropertyName() == null, "null property name preserved in delivered event");
    }

    static void testFireOverloads() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        Recorder r = new Recorder();
        pcs.addPropertyChangeListener(r);
        pcs.firePropertyChange("i", 3, 5);
        pcs.firePropertyChange("i", 5, 5);
        check(r.events.size() == 1, "int overload fires only when value changes");
        PropertyChangeEvent ei = r.events.get(0);
        check("i".equals(ei.getPropertyName()), "int overload property name");
        check(Integer.valueOf(3).equals(ei.getOldValue()) && Integer.valueOf(5).equals(ei.getNewValue()),
                "int overload boxes values as Integer");
        pcs.firePropertyChange("b", false, true);
        pcs.firePropertyChange("b", true, true);
        check(r.events.size() == 2, "boolean overload fires only when value changes");
        PropertyChangeEvent eb = r.events.get(1);
        check(Boolean.FALSE.equals(eb.getOldValue()) && Boolean.TRUE.equals(eb.getNewValue()),
                "boolean overload boxes values as Boolean");
        pcs.firePropertyChange("o", "x", "y");
        pcs.firePropertyChange("o", "y", "y");
        check(r.events.size() == 3, "Object overload fires only when !old.equals(new)");
        pcs.firePropertyChange("n", (Object) null, null);
        check(r.events.size() == 4, "Object overload with null old and new still notifies listeners");
        PropertyChangeEvent en = r.events.get(3);
        check(en.getOldValue() == null && en.getNewValue() == null, "null old/new preserved in fired event");
    }

    public static void main(String[] args) throws Exception {
        testLegacyInventory();
        testInventory();
        testImplementations();
        testSupportIntegration();
        testFireOverloads();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
