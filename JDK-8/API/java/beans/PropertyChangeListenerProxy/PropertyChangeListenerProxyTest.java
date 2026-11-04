/*
 * @test
 * @summary java.beans.PropertyChangeListenerProxy conformance: property name + listener.
 * @run main PropertyChangeListenerProxyTest
 */

import java.beans.*;
public class PropertyChangeListenerProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        PropertyChangeListener l = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {}
        };
        PropertyChangeListenerProxy p = new PropertyChangeListenerProxy("p", l);
        check("p".equals(p.getPropertyName()), "property name");
        check(p.getListener() == l, "listener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
