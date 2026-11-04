/*
 * @test
 * @summary java.beans.PropertyChangeEvent conformance: name/old/new/propagation.
 * @run main PropertyChangeEventTest
 */

import java.beans.*;
public class PropertyChangeEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        PropertyChangeEvent e = new PropertyChangeEvent("src", "p", "old", "new");
        check("p".equals(e.getPropertyName()), "property name");
        check("old".equals(e.getOldValue()), "old value");
        check("new".equals(e.getNewValue()), "new value");
        check("src".equals(e.getSource()), "source");
        e.setPropagationId(Integer.valueOf(5));
        check(Integer.valueOf(5).equals(e.getPropagationId()), "propagation id");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
