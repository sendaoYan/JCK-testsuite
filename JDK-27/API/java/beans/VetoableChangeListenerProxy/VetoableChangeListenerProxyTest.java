/*
 * @test
 * @summary java.beans.VetoableChangeListenerProxy conformance: white-box structure + black-box behavior.
 * @run main VetoableChangeListenerProxyTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeListenerProxy;
import java.util.EventListenerProxy;

public class VetoableChangeListenerProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.VetoableChangeListenerProxy");
        check(EventListenerProxy.class.isAssignableFrom(c), "extends EventListenerProxy");
        check(VetoableChangeListener.class.isAssignableFrom(c), "implements VetoableChangeListener");
    }

    static void testBlackBox() throws Exception {
        final String[] got = new String[1];
        VetoableChangeListener inner = new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) { got[0] = e.getPropertyName(); }
        };
        VetoableChangeListenerProxy proxy = new VetoableChangeListenerProxy("foo", inner);
        check("foo".equals(proxy.getPropertyName()), "getPropertyName");
        check(inner == proxy.getListener(), "getListener");
        proxy.vetoableChange(new PropertyChangeEvent(new Object(), "bar", null, null));
        check("bar".equals(got[0]), "delegates vetoableChange");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
