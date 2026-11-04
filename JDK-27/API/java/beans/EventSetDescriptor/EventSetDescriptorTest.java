/*
 * @test
 * @summary java.beans.EventSetDescriptor conformance: white-box structure + black-box behavior.
 * @run main EventSetDescriptorTest
 */

import java.beans.EventSetDescriptor;
import java.util.EventObject;

interface FooListener extends java.util.EventListener { void fooHappened(FooEvent e); }
class FooEvent extends EventObject { FooEvent(Object s) { super(s); } }
class Source {
    private java.util.List<FooListener> l = new java.util.ArrayList<>();
    public void addFooListener(FooListener x) { l.add(x); }
    public void removeFooListener(FooListener x) { l.remove(x); }
}

public class EventSetDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.EventSetDescriptor");
        check(java.beans.FeatureDescriptor.class.isAssignableFrom(c), "extends FeatureDescriptor");
    }

    static void testBlackBox() throws Exception {
        EventSetDescriptor esd = new EventSetDescriptor(Source.class, "foo", FooListener.class, "fooHappened");
        check("foo".equals(esd.getName()), "name");
        check(FooListener.class == esd.getListenerType(), "listenerType");
        check(esd.getAddListenerMethod() != null && "addFooListener".equals(esd.getAddListenerMethod().getName()), "addListenerMethod");
        check(esd.getRemoveListenerMethod() != null && "removeFooListener".equals(esd.getRemoveListenerMethod().getName()), "removeListenerMethod");
        check(esd.getListenerMethods() != null && esd.getListenerMethods().length == 1, "listenerMethods");
        check(!esd.isUnicast(), "unicast default false");
        esd.setUnicast(true); check(esd.isUnicast(), "unicast set");
        check(esd.isInDefaultEventSet(), "inDefaultEventSet default true");
        esd.setInDefaultEventSet(false); check(!esd.isInDefaultEventSet(), "inDefaultEventSet set false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
