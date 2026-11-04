/*
 * @test
 * @summary javax.swing.event.SwingPropertyChangeSupport conformance: white-box structure + black-box behavior.
 * @run main SwingPropertyChangeSupportTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class SwingPropertyChangeSupportTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.SwingPropertyChangeSupport");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.beans.PropertyChangeSupport"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(Modifier.isFinal(mods) == true, "final modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        Object source = new Object();

        // One-arg constructor defaults to notifyOnEDT == false.
        javax.swing.event.SwingPropertyChangeSupport support =
            new javax.swing.event.SwingPropertyChangeSupport(source);
        check(support.isNotifyOnEDT() == false, "one-arg isNotifyOnEDT false");

        final java.util.List<PropertyChangeEvent> events = new java.util.ArrayList<PropertyChangeEvent>();
        PropertyChangeListener listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) { events.add(evt); }
        };
        support.addPropertyChangeListener(listener);

        support.firePropertyChange("name", "old", "new");
        check(events.size() == 1, "firePropertyChange(name,old,new) delivered once");
        PropertyChangeEvent e0 = events.get(0);
        check(e0.getPropertyName().equals("name"), "property name round-trip");
        check("old".equals(e0.getOldValue()) && "new".equals(e0.getNewValue()), "old/new values round-trip");
        check(e0.getSource() == source, "event source round-trip");

        support.firePropertyChange("count", 1, 2);
        check(events.size() == 2, "firePropertyChange int variant delivered");
        PropertyChangeEvent e1 = events.get(1);
        check(e1.getPropertyName().equals("count"), "int variant property name");
        check(Integer.valueOf(1).equals(e1.getOldValue()) && Integer.valueOf(2).equals(e1.getNewValue()), "int variant values");

        support.firePropertyChange(new PropertyChangeEvent(source, "evt", "a", "b"));
        check(events.size() == 3, "firePropertyChange(PropertyChangeEvent) delivered");
        check(events.get(2).getPropertyName().equals("evt"), "PropertyChangeEvent name");

        java.beans.PropertyChangeListener[] listeners = support.getPropertyChangeListeners();
        check(listeners.length == 1 && listeners[0] == listener, "getPropertyChangeListeners round-trip");

        support.removePropertyChangeListener(listener);
        support.firePropertyChange("name", "x", "y");
        check(events.size() == 3, "removed listener not notified");

        // notifyOnEDT flag round-trips through the two-arg constructor.
        javax.swing.event.SwingPropertyChangeSupport edt =
            new javax.swing.event.SwingPropertyChangeSupport(source, true);
        check(edt.isNotifyOnEDT() == true, "two-arg isNotifyOnEDT true");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.SwingPropertyChangeSupport", false, SwingPropertyChangeSupportTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.beans.PropertyChangeSupport"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("firePropertyChange"), "method firePropertyChange");
        check(names.contains("isNotifyOnEDT"), "method isNotifyOnEDT");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
