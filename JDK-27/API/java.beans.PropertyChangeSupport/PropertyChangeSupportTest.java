/*
 * @test
 * @summary java.beans.PropertyChangeSupport conformance: add listener and fire property change.
 * @run main PropertyChangeSupportTest
 */

import java.beans.*;

public class PropertyChangeSupportTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        final boolean[] fired = new boolean[1];
        final String[] name = new String[1];
        pcs.addPropertyChangeListener("name", evt -> { fired[0] = true; name[0] = evt.getPropertyName(); });
        check(pcs.hasListeners("name"), "has listeners");
        pcs.firePropertyChange("name", "old", "new");
        check(fired[0], "fired");
        check("name".equals(name[0]), "property name");
        check(pcs.getPropertyChangeListeners().length >= 1, "listeners");
        check(pcs.getPropertyChangeListeners("name").length >= 1, "named listeners");
        pcs.firePropertyChange("other", 0, 1);
        pcs.fireIndexedPropertyChange("name", 0, "a", "b");
        check(true, "indexed and unrelated fire without exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
