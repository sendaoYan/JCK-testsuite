/*
 * @test
 * @summary org.w3c.dom.events.EventException conformance: superclass, constructor, code field.
 * @run main EventExceptionTest
 */

public class EventExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(RuntimeException.class.isAssignableFrom(org.w3c.dom.events.EventException.class),
              "extends RuntimeException");
        check(org.w3c.dom.events.EventException.UNSPECIFIED_EVENT_TYPE_ERR == org.w3c.dom.events.EventException.UNSPECIFIED_EVENT_TYPE_ERR, "UNSPECIFIED_EVENT_TYPE_ERR field");
        org.w3c.dom.events.EventException e = new org.w3c.dom.events.EventException(org.w3c.dom.events.EventException.UNSPECIFIED_EVENT_TYPE_ERR, "m");
        check(e.code == org.w3c.dom.events.EventException.UNSPECIFIED_EVENT_TYPE_ERR, "code field");
        check("m".equals(e.getMessage()), "message");

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
