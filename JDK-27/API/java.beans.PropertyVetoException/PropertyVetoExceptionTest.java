/*
 * @test
 * @summary java.beans.PropertyVetoException conformance: message and property change event.
 * @run main PropertyVetoExceptionTest
 */

import java.beans.*;

public class PropertyVetoExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        PropertyChangeEvent evt = new PropertyChangeEvent(new Object(), "p", "old", "new");
        PropertyVetoException e = new PropertyVetoException("veto", evt);
        check("veto".equals(e.getMessage()), "message");
        check(e.getPropertyChangeEvent() == evt, "event");
        check(e instanceof Exception, "extends Exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
