/*
 * @test
 * @summary java.beans.PropertyChangeListener conformance: white-box structure + black-box behavior.
 * @run main PropertyChangeListenerTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PropertyChangeListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.PropertyChangeListener");
        check(c.isInterface(), "is an interface");
        check(java.util.EventListener.class.isAssignableFrom(c), "extends EventListener");
    }

    static void testBlackBox() throws Exception {
        final String[] got = new String[1];
        PropertyChangeListener l = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { got[0] = e.getPropertyName(); }
        };
        l.propertyChange(new PropertyChangeEvent(new Object(), "p", null, null));
        check("p".equals(got[0]), "propertyChange dispatches");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
