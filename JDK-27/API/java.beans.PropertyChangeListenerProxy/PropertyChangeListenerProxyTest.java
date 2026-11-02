/*
 * @test
 * @summary java.beans.PropertyChangeListenerProxy conformance: property name and delegation.
 * @run main PropertyChangeListenerProxyTest
 */

import java.beans.*;

public class PropertyChangeListenerProxyTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        final boolean[] got = new boolean[1];
        PropertyChangeListener target = evt -> got[0] = true;
        PropertyChangeListenerProxy p = new PropertyChangeListenerProxy("name", target);
        check("name".equals(p.getPropertyName()), "property name");
        check(p.getListener() == target, "getListener");
        p.propertyChange(new PropertyChangeEvent(new Object(), "name", null, null));
        check(got[0], "delegates");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
