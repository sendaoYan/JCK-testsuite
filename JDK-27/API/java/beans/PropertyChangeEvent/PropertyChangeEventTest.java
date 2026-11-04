/*
 * @test
 * @summary java.beans.PropertyChangeEvent conformance: white-box structure + black-box behavior.
 * @run main PropertyChangeEventTest
 */

import java.beans.PropertyChangeEvent;

public class PropertyChangeEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.PropertyChangeEvent");
        check(java.util.EventObject.class.isAssignableFrom(c), "extends EventObject");
    }

    static void testBlackBox() throws Exception {
        Object src = new Object();
        PropertyChangeEvent e = new PropertyChangeEvent(src, "name", "old", "new");
        check(e.getSource() == src, "source");
        check("name".equals(e.getPropertyName()), "propertyName");
        check("old".equals(e.getOldValue()), "oldValue");
        check("new".equals(e.getNewValue()), "newValue");
        check(e.getPropagationId() == null, "propagationId null default");
        e.setPropagationId("id1");
        check("id1".equals(e.getPropagationId()), "propagationId set/get");
        check(e.toString().contains("name"), "toString contains property name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
