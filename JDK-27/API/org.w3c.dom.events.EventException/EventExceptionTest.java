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

    public static void main(String[] args) {
        check(RuntimeException.class.isAssignableFrom(EventException.class),
              "extends RuntimeException");

        EventException e = new EventException(EventException.UNSPECIFIED_EVENT_TYPE_ERR, "t");
        check(e.code == EventException.UNSPECIFIED_EVENT_TYPE_ERR, "code field");
        check("t".equals(e.getMessage()), "message");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
