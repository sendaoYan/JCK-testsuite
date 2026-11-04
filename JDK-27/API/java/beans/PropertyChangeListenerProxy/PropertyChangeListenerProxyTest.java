/*
 * @test
 * @summary java.beans.PropertyChangeListenerProxy conformance: white-box structure + black-box behavior.
 * @run main PropertyChangeListenerProxyTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.util.EventListenerProxy;

public class PropertyChangeListenerProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.PropertyChangeListenerProxy");
        check(EventListenerProxy.class.isAssignableFrom(c), "extends EventListenerProxy");
        check(PropertyChangeListener.class.isAssignableFrom(c), "implements PropertyChangeListener");
    }

    static void testBlackBox() throws Exception {
        final String[] got = new String[1];
        PropertyChangeListener inner = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { got[0] = e.getPropertyName(); }
        };
        PropertyChangeListenerProxy proxy = new PropertyChangeListenerProxy("foo", inner);
        check("foo".equals(proxy.getPropertyName()), "getPropertyName");
        check(inner == proxy.getListener(), "getListener");
        proxy.propertyChange(new PropertyChangeEvent(new Object(), "bar", null, null));
        check("bar".equals(got[0]), "delegates propertyChange");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
