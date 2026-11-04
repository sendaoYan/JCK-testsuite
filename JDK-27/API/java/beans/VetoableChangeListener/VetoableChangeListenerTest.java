/*
 * @test
 * @summary java.beans.VetoableChangeListener conformance: white-box structure + black-box behavior.
 * @run main VetoableChangeListenerTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.VetoableChangeListener;

public class VetoableChangeListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.VetoableChangeListener");
        check(c.isInterface(), "is an interface");
        check(java.util.EventListener.class.isAssignableFrom(c), "extends EventListener");
    }

    static void testBlackBox() throws Exception {
        final String[] got = new String[1];
        VetoableChangeListener l = new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) { got[0] = e.getPropertyName(); }
        };
        l.vetoableChange(new PropertyChangeEvent(new Object(), "p", null, null));
        check("p".equals(got[0]), "vetoableChange dispatches");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
