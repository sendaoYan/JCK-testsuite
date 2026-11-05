/*
 * @test
 * @summary javax.xml.bind.ValidationEventHandler conformance: signature and real handler channel (true/false verdicts).
 * @run main ValidationEventHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ValidationEventHandlerTest {
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

    static class RecordingHandler implements javax.xml.bind.ValidationEventHandler {
        java.util.List<javax.xml.bind.ValidationEvent> events =
                new java.util.ArrayList<javax.xml.bind.ValidationEvent>();
        boolean verdict = true;
        public boolean handleEvent(javax.xml.bind.ValidationEvent e) {
            events.add(e);
            return verdict;
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.ValidationEventHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("handleEvent"), "has method handleEvent");
        testSignature(c);
        testRealChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testSignature(Class<?> c) throws Exception {
        Method m = c.getMethod("handleEvent", Class.forName("javax.xml.bind.ValidationEvent"));
        check(m != null, "handleEvent(ValidationEvent) declared");
        check(m.getReturnType() == boolean.class, "handleEvent returns boolean");
        check(Modifier.isAbstract(m.getModifiers()), "handleEvent is abstract");
        check(c.getDeclaredMethods().length == 1, "interface declares exactly one method");
        check(c.getInterfaces().length == 0, "extends no other interface");
    }

    // real channel through the RI Unmarshaller: verdict decides abort vs continue
    static void testRealChannel() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        javax.xml.bind.Unmarshaller u = ctx.createUnmarshaller();
        RecordingHandler h = new RecordingHandler();
        u.setEventHandler(h);
        check(true, "setEventHandler accepts custom handler");
        String xmlExtra =
                "<person><name>alice</name><age>30</age><extra>x</extra></person>";

        // verdict true: recoverable error tolerated, data round-trips
        h.verdict = true;
        h.events.clear();
        Object r = u.unmarshal(new java.io.StringReader(xmlExtra));
        check(r instanceof Person, "verdict=true: unmarshal completes");
        check("alice".equals(((Person) r).name), "verdict=true: name read");
        check(((Person) r).age == 30, "verdict=true: age read");
        check(h.events.size() == 1, "verdict=true: exactly one event reached handler (locked)");
        check(h.events.get(0).getSeverity() == javax.xml.bind.ValidationEvent.ERROR,
                "delivered event severity == ERROR");
        javax.xml.bind.ValidationEventLocator loc = h.events.get(0).getLocator();
        check(loc != null && loc.getLineNumber() == 1 && loc.getColumnNumber() == 47,
                "locator points at the extra element (line 1, col 47 locked for this xml)");

        // verdict false: same error aborts with UnmarshalException
        h.verdict = false;
        h.events.clear();
        boolean threw = false;
        try {
            u.unmarshal(new java.io.StringReader(xmlExtra));
        } catch (javax.xml.bind.UnmarshalException ue) {
            threw = true;
            check(ue.getCause() == null, "verdict=false abort: cause null (handler veto, not chained)");
        }
        check(threw, "verdict=false: unmarshal aborts with UnmarshalException");
        check(h.events.size() == 1, "verdict=false: event still reached handler");

        // fatal errors abort regardless of the verdict
        h.verdict = true;
        h.events.clear();
        boolean threw2 = false;
        try {
            u.unmarshal(new java.io.StringReader("<broken"));
        } catch (javax.xml.bind.UnmarshalException ue) {
            threw2 = true;
        }
        check(threw2, "FATAL_ERROR aborts even when handler returns true");
        check(h.events.size() == 1, "fatal event reached handler before abort");

        // setEventHandler(null) restores default: recoverable error aborts again
        u.setEventHandler(null);
        boolean threw3 = false;
        try {
            u.unmarshal(new java.io.StringReader(xmlExtra));
        } catch (javax.xml.bind.UnmarshalException ue) {
            threw3 = true;
        }
        check(threw3, "setEventHandler(null) restores default (aborting) behavior");

        // a handler may also be attached to a Marshaller without breaking marshalling
        javax.xml.bind.Marshaller m = ctx.createMarshaller();
        m.setEventHandler(h);
        h.verdict = true;
        Person mp = new Person();
        mp.name = "a";
        mp.age = 1;
        java.io.StringWriter sw = new java.io.StringWriter();
        m.marshal(mp, sw);
        check(sw.toString().contains("<person>"), "marshaller with custom handler still marshals");
    }
}
