/*
 * @test
 * @summary javax.xml.bind.PrintConversionEvent conformance: marker subinterface of ValidationEvent and impl channel.
 * @run main PrintConversionEventTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class PrintConversionEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @javax.xml.bind.annotation.XmlRootElement
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class Person {
        @javax.xml.bind.annotation.XmlElement String name;
        @javax.xml.bind.annotation.XmlElement int age;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.PrintConversionEvent");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.bind.ValidationEvent"), "implements javax.xml.bind.ValidationEvent");
        testMarkerShape(c);
        testImpl();
        testMarshalStillWorks();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMarkerShape(Class<?> c) throws Exception {
        check(c.getDeclaredMethods().length == 0, "declares no additional methods (marker)");
        check(c.getDeclaredFields().length == 0, "declares no constants");
        check(c.getMethods().length == 4, "inherits exactly the 4 ValidationEvent methods");
        check(javax.xml.bind.ValidationEvent.class.isAssignableFrom(c), "ValidationEvent.isAssignableFrom(PrintConversionEvent)");
    }

    static void testImpl() {
        javax.xml.bind.helpers.ValidationEventLocatorImpl loc =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        javax.xml.bind.PrintConversionEvent e =
                new javax.xml.bind.helpers.PrintConversionEventImpl(
                        javax.xml.bind.ValidationEvent.ERROR, "prc", loc);
        check(e.getSeverity() == 1, "impl severity readable through interface");
        check("prc".equals(e.getMessage()), "impl message readable through interface");
        check(e.getLocator() == loc, "impl locator readable through interface");
        check(e instanceof javax.xml.bind.ValidationEvent, "impl is a ValidationEvent");
        RuntimeException t = new RuntimeException("cvt");
        javax.xml.bind.helpers.PrintConversionEventImpl e4 =
                new javax.xml.bind.helpers.PrintConversionEventImpl(2, "m", loc, t);
        check(e4.getLinkedException() == t, "4-arg constructor stores linked exception");
        check(e4 instanceof javax.xml.bind.PrintConversionEvent, "4-arg instance is PrintConversionEvent");
        javax.xml.bind.helpers.ValidationEventImpl inherited = e4;
        inherited.setSeverity(javax.xml.bind.ValidationEvent.WARNING);
        check(inherited.getSeverity() == 0, "inherited setSeverity works on print event impl");
        inherited.setMessage("m2");
        check("m2".equals(inherited.getMessage()), "inherited setMessage works on print event impl");
    }

    // marshalling with an event handler installed: print-side pipeline completes without events for valid data
    static void testMarshalStillWorks() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        javax.xml.bind.Marshaller m = ctx.createMarshaller();
        java.util.List<javax.xml.bind.ValidationEvent> events =
                new java.util.ArrayList<javax.xml.bind.ValidationEvent>();
        m.setEventHandler(new javax.xml.bind.ValidationEventHandler() {
            public boolean handleEvent(javax.xml.bind.ValidationEvent e) {
                events.add(e);
                return true;
            }
        });
        Person p = new Person();
        p.name = "bob";
        p.age = 9;
        java.io.StringWriter sw = new java.io.StringWriter();
        m.marshal(p, sw);
        check(sw.toString().contains("<person>"), "marshal completes with handler installed");
        check(sw.toString().contains("<age>9</age>"), "int printed correctly");
        check(events.isEmpty(), "no events fired for valid print-side data (locked)");
    }
}
