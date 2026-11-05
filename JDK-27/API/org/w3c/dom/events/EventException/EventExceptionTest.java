/*
 * @test
 * @summary org.w3c.dom.events.EventException conformance: superclass, constructor.
 * @run main EventExceptionTest
 */

import org.w3c.dom.events.EventException;

public class EventExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(RuntimeException.class.isAssignableFrom(EventException.class),
              "extends RuntimeException");

        EventException e = new EventException(EventException.UNSPECIFIED_EVENT_TYPE_ERR, "t");
        check(e.code == EventException.UNSPECIFIED_EVENT_TYPE_ERR, "code field");
        check("t".equals(e.getMessage()), "message");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(EventException.UNSPECIFIED_EVENT_TYPE_ERR == 0, "UNSPECIFIED_EVENT_TYPE_ERR == 0");
        org.w3c.dom.events.EventException e = new org.w3c.dom.events.EventException((short) 3, "msg");
        check(e.code == (short) 3, "code field set");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof RuntimeException, "is RuntimeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral EventException contract (real trigger) ----
    static void deepContract() throws Exception {
        org.w3c.dom.events.EventException ee = new org.w3c.dom.events.EventException(org.w3c.dom.events.EventException.UNSPECIFIED_EVENT_TYPE_ERR, "no type");
        check(ee.code == 0, "UNSPECIFIED_EVENT_TYPE_ERR is 0");
        check("no type".equals(ee.getMessage()), "message preserved");
        check(new org.w3c.dom.events.EventException((short) 0, null).getMessage() == null, "null message preserved");
        check(ee instanceof RuntimeException, "EventException is a RuntimeException");
        // real trigger: dispatch without initEvent
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element e = d.createElement("e");
        d.appendChild(e);
        org.w3c.dom.events.DocumentEvent de = (org.w3c.dom.events.DocumentEvent) d;
        boolean threw = false;
        try { ((org.w3c.dom.events.EventTarget) e).dispatchEvent(de.createEvent("Event")); }
        catch (org.w3c.dom.events.EventException ex) { threw = ex.code == org.w3c.dom.events.EventException.UNSPECIFIED_EVENT_TYPE_ERR; }
        check(threw, "dispatchEvent(createEvent() without initEvent) -> EventException code 0");
        // after initEvent the same event dispatches fine
        org.w3c.dom.events.Event ev = de.createEvent("Event");
        ev.initEvent("ok", false, false);
        ((org.w3c.dom.events.EventTarget) e).dispatchEvent(ev);
        check("ok".equals(ev.getType()), "initialized event dispatches without EventException");
    }
}
