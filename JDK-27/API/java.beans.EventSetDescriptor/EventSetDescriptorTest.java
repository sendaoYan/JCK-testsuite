/*
 * @test
 * @summary java.beans.EventSetDescriptor conformance: introspect add/remove listener methods.
 * @run main EventSetDescriptorTest
 */

import java.beans.*;

public class EventSetDescriptorTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

public static class Bean {
    public void addPropertyChangeListener(PropertyChangeListener l) {}
    public void removePropertyChangeListener(PropertyChangeListener l) {}
}
    public static void main(String[] args) throws Exception {
        EventSetDescriptor esd = new EventSetDescriptor(Bean.class, "propertyChange", PropertyChangeListener.class, "propertyChange");
        check(esd.getListenerType() == PropertyChangeListener.class, "listener type");
        check(esd.getListenerMethods().length == 1, "listener methods");
        check(esd.getAddListenerMethod() != null, "add method");
        check(esd.getRemoveListenerMethod() != null, "remove method");
        esd.setUnicast(true); check(esd.isUnicast(), "unicast");
        esd.setInDefaultEventSet(true); check(esd.isInDefaultEventSet(), "default event set");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
