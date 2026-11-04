/*
 * @test
 * @summary javax.swing.colorchooser.DefaultColorSelectionModel conformance: white-box structure + black-box behavior.
 * @run main DefaultColorSelectionModelTest
 */

import java.awt.Color;
import java.lang.reflect.Modifier;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DefaultColorSelectionModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.colorchooser.DefaultColorSelectionModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.colorchooser.ColorSelectionModel"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
        check(c.getMethod("getChangeListeners") != null, "getChangeListeners present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Default constructor selects white.
        DefaultColorSelectionModel m1 = new DefaultColorSelectionModel();
        check(Color.white.equals(m1.getSelectedColor()), "default selected color is white");

        // Explicit constructor selects the given color.
        DefaultColorSelectionModel m2 = new DefaultColorSelectionModel(Color.blue);
        check(Color.blue.equals(m2.getSelectedColor()), "explicit constructor selects color");

        // set/get round-trip.
        m2.setSelectedColor(Color.green);
        check(Color.green.equals(m2.getSelectedColor()), "setSelectedColor updates color");

        // Same color does not fire an event; different color does.
        final int[] fired = { 0 };
        final Object[] source = { null };
        ChangeListener l = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                fired[0]++;
                source[0] = e.getSource();
            }
        };
        m2.addChangeListener(l);
        m2.setSelectedColor(Color.green);  // unchanged
        check(fired[0] == 0, "same color does not fire");
        m2.setSelectedColor(Color.red);    // changed
        check(fired[0] == 1, "different color fires once");
        check(source[0] == m2, "change event source is the model");

        // Null color is ignored (no NPE, no change, no event).
        m2.setSelectedColor(null);
        check(Color.red.equals(m2.getSelectedColor()), "null color leaves selection unchanged");
        check(fired[0] == 1, "null color fires no event");

        // getChangeListeners lists registered listeners and returns a snapshot copy.
        ChangeListener[] before = m2.getChangeListeners();
        check(before.length == 1 && before[0] == l, "getChangeListeners lists listener");
        before[0] = null;
        check(m2.getChangeListeners()[0] == l, "getChangeListeners returns a copy");

        // removeChangeListener stops delivery.
        m2.removeChangeListener(l);
        check(m2.getChangeListeners().length == 0, "listener removed");
        m2.setSelectedColor(Color.black);
        check(fired[0] == 1, "removed listener not notified");

        // Multiple listeners are all notified of a change.
        final java.util.List<String> notified = new java.util.ArrayList<String>();
        ChangeListener a = new ChangeListener() { public void stateChanged(ChangeEvent e) { notified.add("a"); } };
        ChangeListener b = new ChangeListener() { public void stateChanged(ChangeEvent e) { notified.add("b"); } };
        m2.addChangeListener(a);
        m2.addChangeListener(b);
        m2.setSelectedColor(Color.cyan);
        check(notified.size() == 2 && notified.contains("a") && notified.contains("b"),
                "all registered listeners notified");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // Appended contract: null-initialized model NPE, serialization round-trip.
    static void testContract() throws Exception {
        javax.swing.colorchooser.DefaultColorSelectionModel n =
                new javax.swing.colorchooser.DefaultColorSelectionModel(null);
        check(n.getSelectedColor() == null, "ctor(null) -> null selection");
        // a null-initialized model throws NPE when set to a real color
        // (both JDK-8 and JDK-27 compare via the stored null selection)
        try { n.setSelectedColor(java.awt.Color.BLUE);
              throw new AssertionError("expected NullPointerException on null-initialized model"); }
        catch (NullPointerException expected) { passed++; }
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(new javax.swing.colorchooser.DefaultColorSelectionModel(java.awt.Color.GRAY));
        oos.close();
        javax.swing.colorchooser.DefaultColorSelectionModel back =
                (javax.swing.colorchooser.DefaultColorSelectionModel)
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(java.awt.Color.GRAY.equals(back.getSelectedColor()), "serialized color preserved");
        check(back.getChangeListeners().length == 0, "listeners not serialized");
    }
}
