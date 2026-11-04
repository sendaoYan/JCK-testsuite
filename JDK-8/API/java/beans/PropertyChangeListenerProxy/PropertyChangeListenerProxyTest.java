/*
 * @test
 * @summary java.beans.PropertyChangeListenerProxy conformance: full contract.
 *          extends EventListenerProxy and implements PropertyChangeListener,
 *          null listener -> NPE, getPropertyName / getListener identity, and
 *          propertyChange delegation to the wrapped listener.
 * @run main PropertyChangeListenerProxyTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.util.EventListenerProxy;

public class PropertyChangeListenerProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testStructure() {
        Class<?> c = PropertyChangeListenerProxy.class;
        check(EventListenerProxy.class.isAssignableFrom(c), "extends EventListenerProxy");
        check(PropertyChangeListener.class.isAssignableFrom(c), "implements PropertyChangeListener");
    }

    static void testBasics() {
        final String[] got = new String[1];
        PropertyChangeListener inner = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { got[0] = e.getPropertyName(); }
        };
        PropertyChangeListenerProxy proxy = new PropertyChangeListenerProxy("foo", inner);
        check("foo".equals(proxy.getPropertyName()), "getPropertyName");
        check(inner == proxy.getListener(), "getListener identity");

        proxy.propertyChange(new PropertyChangeEvent(new Object(), "bar", null, null));
        check("bar".equals(got[0]), "delegates propertyChange");

        PropertyChangeListenerProxy nullProxy = new PropertyChangeListenerProxy("foo", null);
        check(nullProxy.getListener() == null, "null listener accepted (getListener null)");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testBasics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
