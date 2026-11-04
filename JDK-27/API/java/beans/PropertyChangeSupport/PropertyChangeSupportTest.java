/*
 * @test
 * @summary java.beans.PropertyChangeSupport conformance: white-box structure + black-box behavior.
 * @run main PropertyChangeSupportTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class PropertyChangeSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.PropertyChangeSupport");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
    }

    static void testBlackBox() throws Exception {
        Object source = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(source);
        check(!pcs.hasListeners("foo"), "no listeners initially");

        final int[] any = new int[1];
        final int[] named = new int[1];
        pcs.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { any[0]++; }
        });
        PropertyChangeListener namedL = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { named[0]++; }
        };
        pcs.addPropertyChangeListener("foo", namedL);
        check(pcs.hasListeners("foo"), "hasListeners after add");
        check(pcs.getPropertyChangeListeners().length == 2, "listeners (global + named proxy)");
        check(pcs.getPropertyChangeListeners("foo").length == 1, "named listeners");

        pcs.firePropertyChange("foo", "a", "b");
        check(any[0] == 1 && named[0] == 1, "fire named hits both");

        pcs.firePropertyChange("bar", 1, 2);
        check(any[0] == 2 && named[0] == 1, "fire other only hits global");

        pcs.fireIndexedPropertyChange("foo", 3, "x", "y");
        check(any[0] == 3, "fireIndexedPropertyChange");

        pcs.removePropertyChangeListener("foo", namedL);
        check(pcs.getPropertyChangeListeners("foo").length == 0, "named listener removed");
        check(pcs.hasListeners("foo"), "hasListeners still true via global listener");

        PropertyChangeEvent evt = new PropertyChangeEvent(source, "z", null, "v");
        pcs.firePropertyChange(evt);
        check(any[0] == 4, "firePropertyChange(PropertyChangeEvent)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
