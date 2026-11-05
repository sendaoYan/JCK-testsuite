/*
 * @test
 * @summary javax.xml.bind.util.ValidationEventCollector conformance: record/reset lifecycle and real unmarshal handler channel.
 * @run main ValidationEventCollectorTest
 */

public class ValidationEventCollectorTest {
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
        javax.xml.bind.util.ValidationEventCollector c = new javax.xml.bind.util.ValidationEventCollector();
        check(!c.hasEvents(), "no events");
        check(c.getEvents().length == 0, "empty events");
        javax.xml.bind.helpers.ValidationEventImpl ev = new javax.xml.bind.helpers.ValidationEventImpl(1, "msg", (javax.xml.bind.ValidationEventLocator) null);
        check(c.handleEvent(ev), "handleEvent true");
        check(c.hasEvents(), "has events");
        c.reset();
        check(!c.hasEvents(), "reset");
        testInitialState();
        testRecordLifecycle();
        testNegative();
        testRealChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testInitialState() {
        javax.xml.bind.util.ValidationEventCollector c = new javax.xml.bind.util.ValidationEventCollector();
        check(!c.hasEvents(), "initial hasEvents false");
        javax.xml.bind.ValidationEvent[] evs = c.getEvents();
        check(evs != null, "initial getEvents() non-null empty array (locked)");
        check(evs.length == 0, "initial getEvents() length 0");
        check(c instanceof javax.xml.bind.ValidationEventHandler, "implements ValidationEventHandler");
        check(javax.xml.bind.util.ValidationEventCollector.class.getConstructors().length == 1, "single public constructor");
    }

    static void testRecordLifecycle() {
        javax.xml.bind.util.ValidationEventCollector c = new javax.xml.bind.util.ValidationEventCollector();
        javax.xml.bind.helpers.ValidationEventLocatorImpl loc =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        loc.setLineNumber(2);
        javax.xml.bind.helpers.ValidationEventImpl e1 =
                new javax.xml.bind.helpers.ValidationEventImpl(1, "first", loc);
        javax.xml.bind.helpers.ValidationEventImpl e2 =
                new javax.xml.bind.helpers.ValidationEventImpl(2, "second", loc);
        check(c.handleEvent(e1) == true, "handleEvent returns true (locked)");
        check(c.hasEvents(), "hasEvents true after record");
        javax.xml.bind.ValidationEvent[] evs = c.getEvents();
        check(evs.length == 1, "one event recorded");
        check(evs[0] == e1, "recorded event is the same instance");
        check("first".equals(evs[0].getMessage()), "recorded message readable");
        check(evs[0].getSeverity() == 1, "recorded severity readable");
        check(evs[0].getLocator() == loc, "recorded locator readable");

        c.handleEvent(e2);
        javax.xml.bind.ValidationEvent[] evs2 = c.getEvents();
        check(evs2.length == 2, "two events recorded");
        check(evs2[0] == e1 && evs2[1] == e2, "insertion order preserved (locked)");
        check(evs2 != evs, "getEvents() returns a fresh array each call (locked)");

        c.reset();
        check(!c.hasEvents(), "reset clears");
        check(c.getEvents().length == 0, "reset empties array");
        // collector is reusable after reset
        c.handleEvent(e2);
        check(c.getEvents().length == 1 && c.getEvents()[0] == e2, "reusable after reset");
    }

    static void testNegative() {
        javax.xml.bind.util.ValidationEventCollector c = new javax.xml.bind.util.ValidationEventCollector();
        boolean npe = false;
        try { c.handleEvent(null); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "handleEvent(null) throws NullPointerException (locked)");
        // RI quirk: the null is recorded BEFORE the dereference, so the list holds one null (locked)
        check(c.hasEvents(), "null event is recorded before the NPE (locked)");
        check(c.getEvents().length == 1 && c.getEvents()[0] == null, "recorded entry is null");
        c.reset();
        check(!c.hasEvents(), "reset clears the null entry");
    }

    // real channel: installed on a real Unmarshaller it captures the RI's recoverable-error event
    static void testRealChannel() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        javax.xml.bind.Unmarshaller u = ctx.createUnmarshaller();
        javax.xml.bind.util.ValidationEventCollector c = new javax.xml.bind.util.ValidationEventCollector();
        u.setEventHandler(c);
        check(true, "collector accepted as Unmarshaller handler");

        // good XML: completes with no events
        Object r = u.unmarshal(new java.io.StringReader(
                "<person><name>bob</name><age>9</age></person>"));
        check(r instanceof Person && "bob".equals(((Person) r).name), "good xml round-trip");
        check(!c.hasEvents(), "good xml produces no events (locked)");

        // recoverable error: tolerated (handleEvent returns true) but recorded
        r = u.unmarshal(new java.io.StringReader(
                "<person><name>alice</name><age>30</age><extra>x</extra></person>"));
        check(r instanceof Person, "collector's true verdict keeps unmarshal running");
        check(c.hasEvents(), "unexpected element recorded");
        javax.xml.bind.ValidationEvent[] evs = c.getEvents();
        check(evs.length == 1, "exactly one recorded event (locked)");
        check(evs[0].getSeverity() == javax.xml.bind.ValidationEvent.ERROR, "real event severity ERROR");
        check(evs[0].getMessage() != null && evs[0].getMessage().contains("unexpected element"),
                "real event message");
        check(evs[0].getLocator() != null && evs[0].getLocator().getLineNumber() == 1,
                "real event locator line 1");

        c.reset();
        check(!c.hasEvents(), "reset works after real capture");
    }
}
