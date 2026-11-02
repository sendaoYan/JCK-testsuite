/*
 * @test
 * @summary java.beans.PropertyChangeSupport conformance: listener registration and firing.
 * @run main PropertyChangeSupportTest
 */

import java.beans.*;
public class PropertyChangeSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        final PropertyChangeEvent[] evt = new PropertyChangeEvent[1];
        pcs.addPropertyChangeListener("name", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { evt[0] = e; }
        });
        pcs.firePropertyChange("name", "old", "new");
        check(evt[0] != null, "event fired");
        check("name".equals(evt[0].getPropertyName()), "property name");
        check("old".equals(evt[0].getOldValue()) && "new".equals(evt[0].getNewValue()), "old/new");
        check(pcs.hasListeners("name"), "hasListeners");
        check(pcs.getPropertyChangeListeners("name").length == 1, "listener count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
