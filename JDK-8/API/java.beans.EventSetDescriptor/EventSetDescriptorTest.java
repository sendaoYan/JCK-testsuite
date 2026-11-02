/*
 * @test
 * @summary java.beans.EventSetDescriptor conformance: listener type + add/remove methods.
 * @run main EventSetDescriptorTest
 */

import java.beans.*;

public class EventSetDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class EventBean {
        public void addPropertyChangeListener(PropertyChangeListener l) {}
        public void removePropertyChangeListener(PropertyChangeListener l) {}
    }

    public static void main(String[] args) throws Exception {
        EventSetDescriptor esd = new EventSetDescriptor(EventBean.class, "propertyChange", PropertyChangeListener.class, "propertyChange");
        check(esd.getListenerType() == PropertyChangeListener.class, "listener type");
        check(esd.getAddListenerMethod() != null, "add method");
        check(esd.getRemoveListenerMethod() != null, "remove method");
        check(esd.getListenerMethods().length >= 1, "listener methods");
        esd.setUnicast(true);
        check(esd.isUnicast(), "unicast");
        esd.setInDefaultEventSet(true);
        check(esd.isInDefaultEventSet(), "default event set");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
