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

    public static void main(String[] args) {
        check(RuntimeException.class.isAssignableFrom(org.w3c.dom.events.EventException.class),
              "extends RuntimeException");
        check(org.w3c.dom.events.EventException.UNSPECIFIED_EVENT_TYPE_ERR == org.w3c.dom.events.EventException.UNSPECIFIED_EVENT_TYPE_ERR, "UNSPECIFIED_EVENT_TYPE_ERR field");
        org.w3c.dom.events.EventException e = new org.w3c.dom.events.EventException(org.w3c.dom.events.EventException.UNSPECIFIED_EVENT_TYPE_ERR, "m");
        check(e.code == org.w3c.dom.events.EventException.UNSPECIFIED_EVENT_TYPE_ERR, "code field");
        check("m".equals(e.getMessage()), "message");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
