/*
 * @test
 * @summary javax.xml.bind.ParseConversionEvent conformance: marker subinterface plus real text->int conversion failure channel.
 * @run main ParseConversionEventTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ParseConversionEventTest {
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
        Class<?> c = Class.forName("javax.xml.bind.ParseConversionEvent");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.bind.ValidationEvent"), "implements javax.xml.bind.ValidationEvent");
        testMarkerShape(c);
        testImpl();
        testRealParseFailure();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMarkerShape(Class<?> c) throws Exception {
        check(c.getDeclaredMethods().length == 0, "declares no additional methods (marker)");
        check(c.getDeclaredFields().length == 0, "declares no constants");
        check(c.getMethods().length == 4, "inherits exactly the 4 ValidationEvent methods");
        check(javax.xml.bind.ValidationEvent.class.isAssignableFrom(c), "ValidationEvent.isAssignableFrom(ParseConversionEvent)");
    }

    static void testImpl() {
        javax.xml.bind.helpers.ValidationEventLocatorImpl loc =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        javax.xml.bind.ParseConversionEvent e =
                new javax.xml.bind.helpers.ParseConversionEventImpl(
                        javax.xml.bind.ValidationEvent.ERROR, "pc", loc);
        check(e.getSeverity() == 1, "impl severity readable through interface");
        check("pc".equals(e.getMessage()), "impl message readable through interface");
        check(e.getLocator() == loc, "impl locator readable through interface");
        check(e instanceof javax.xml.bind.ValidationEvent, "impl is a ValidationEvent");
        RuntimeException t = new RuntimeException("cvt");
        javax.xml.bind.helpers.ParseConversionEventImpl e4 =
                new javax.xml.bind.helpers.ParseConversionEventImpl(2, "m", loc, t);
        check(e4.getLinkedException() == t, "4-arg constructor stores linked exception");
        check(e4 instanceof javax.xml.bind.ParseConversionEvent, "4-arg instance is ParseConversionEvent");
        check(e4 instanceof javax.xml.bind.ValidationEvent, "4-arg instance is also ValidationEvent");
    }

    // real channel: unparseable int text yields an ERROR event whose linked exception is NumberFormatException;
    // JDK-8 RI delivers it as a plain ValidationEventImpl (recorded fact, not the subinterface type)
    static void testRealParseFailure() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        javax.xml.bind.Unmarshaller u = ctx.createUnmarshaller();
        java.util.List<javax.xml.bind.ValidationEvent> events =
                new java.util.ArrayList<javax.xml.bind.ValidationEvent>();
        u.setEventHandler(new javax.xml.bind.ValidationEventHandler() {
            public boolean handleEvent(javax.xml.bind.ValidationEvent e) {
                events.add(e);
                return true;
            }
        });
        Object r = u.unmarshal(new java.io.StringReader(
                "<person><name>a</name><age>notanint</age></person>"));
        check(r instanceof Person, "tolerant handler lets failed conversion default to 0");
        check(((Person) r).age == 0, "int field defaulted to 0 after failed conversion (locked)");
        check(events.size() == 1, "exactly one conversion event delivered");
        javax.xml.bind.ValidationEvent ev = events.get(0);
        check(ev.getSeverity() == javax.xml.bind.ValidationEvent.ERROR, "conversion failure severity == ERROR");
        check(ev.getMessage() != null && ev.getMessage().contains("notanint"), "message mentions the bad lexical value");
        check(ev.getLinkedException() instanceof NumberFormatException, "linked exception is NumberFormatException (locked)");
        check(ev.getLocator() != null && ev.getLocator().getLineNumber() == 1, "locator attached to conversion event");
        check(!(ev instanceof javax.xml.bind.ParseConversionEvent),
                "RI delivers base ValidationEventImpl, not ParseConversionEvent (recorded)");
    }
}
