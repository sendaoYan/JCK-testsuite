/*
 * @test
 * @summary java.beans.Customizer conformance: white-box structure + black-box behavior.
 * @run main CustomizerTest
 */

import java.beans.Customizer;
import java.beans.PropertyChangeListener;

public class CustomizerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.Customizer");
        check(c.isInterface(), "is an interface");
    }

    static void testBlackBox() throws Exception {
        final Object[] bean = new Object[1];
        Customizer c = new Customizer() {
            public void setObject(Object o) { bean[0] = o; }
            public void addPropertyChangeListener(PropertyChangeListener l) {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        Object b = new Object();
        c.setObject(b);
        check(b == bean[0], "setObject dispatches");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
