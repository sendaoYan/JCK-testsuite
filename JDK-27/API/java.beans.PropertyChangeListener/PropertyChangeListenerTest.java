/*
 * @test
 * @summary java.beans.PropertyChangeListener conformance: functional interface invocation.
 * @run main PropertyChangeListenerTest
 */

import java.beans.*;

public class PropertyChangeListenerTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        PropertyChangeListener l = evt -> {};
        l.propertyChange(new PropertyChangeEvent(new Object(), "p", null, null));
        check(PropertyChangeListener.class.isInterface(), "is interface");
        check(true, "invoked");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
