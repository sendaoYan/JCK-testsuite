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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
