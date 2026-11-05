/*
 * @test
 * @summary javax.xml.bind.NotIdentifiableEvent conformance: marker subinterface of ValidationEvent and concrete impl channel.
 * @run main NotIdentifiableEventTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class NotIdentifiableEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.NotIdentifiableEvent");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.bind.ValidationEvent"), "implements javax.xml.bind.ValidationEvent");
        testMarkerShape(c);
        testImpl();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // marker interface: adds no methods/constants, inherits the 4 ValidationEvent methods
    static void testMarkerShape(Class<?> c) throws Exception {
        check(c.getDeclaredMethods().length == 0, "declares no additional methods (marker)");
        check(c.getDeclaredFields().length == 0, "declares no constants");
        check(c.getMethods().length == 4, "inherits exactly the 4 ValidationEvent methods");
        check(javax.xml.bind.ValidationEvent.class.isAssignableFrom(c), "ValidationEvent.isAssignableFrom(NotIdentifiableEvent)");
        check(c.isAssignableFrom(javax.xml.bind.ValidationEvent.class) == false, "not assignable in reverse");
        // inherited severity domain
        check(javax.xml.bind.ValidationEvent.WARNING == 0
                && javax.xml.bind.ValidationEvent.ERROR == 1
                && javax.xml.bind.ValidationEvent.FATAL_ERROR == 2, "severity domain inherited");
    }

    // concrete helper impl is a usable NotIdentifiableEvent
    static void testImpl() {
        javax.xml.bind.helpers.ValidationEventLocatorImpl loc =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        loc.setLineNumber(3);
        loc.setColumnNumber(5);
        javax.xml.bind.NotIdentifiableEvent e =
                new javax.xml.bind.helpers.NotIdentifiableEventImpl(
                        javax.xml.bind.ValidationEvent.ERROR, "nid", loc);
        check(e.getSeverity() == 1, "impl severity readable through interface");
        check("nid".equals(e.getMessage()), "impl message readable through interface");
        check(e.getLocator() == loc, "impl locator readable through interface");
        check(e.getLinkedException() == null, "impl linked exception null by default");
        check(e instanceof javax.xml.bind.ValidationEvent, "impl is a ValidationEvent");
        RuntimeException t = new RuntimeException("x");
        javax.xml.bind.helpers.NotIdentifiableEventImpl e4 =
                new javax.xml.bind.helpers.NotIdentifiableEventImpl(2, "m", loc, t);
        check(e4.getLinkedException() == t, "4-arg constructor stores linked exception");
        check(e4 instanceof javax.xml.bind.NotIdentifiableEvent, "4-arg instance is NotIdentifiableEvent");
    }
}
