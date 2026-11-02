/*
 * @test
 * @summary java.beans.PropertyChangeEvent conformance: source/property/values/propagation id.
 * @run main PropertyChangeEventTest
 */

import java.beans.*;

public class PropertyChangeEventTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        Object src = new Object();
        PropertyChangeEvent e = new PropertyChangeEvent(src, "p", "old", "new");
        check(e.getSource() == src, "source");
        check("p".equals(e.getPropertyName()), "property name");
        check("old".equals(e.getOldValue()), "old value");
        check("new".equals(e.getNewValue()), "new value");
        check(e.getPropagationId() == null, "propagation id default null");
        e.setPropagationId("id"); check("id".equals(e.getPropagationId()), "propagation id");
        check(e.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
