/*
 * @test
 * @summary java.beans.PropertyVetoException conformance: white-box structure + black-box behavior.
 * @run main PropertyVetoExceptionTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

public class PropertyVetoExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.PropertyVetoException");
        check(Exception.class.isAssignableFrom(c), "extends Exception");
    }

    static void testBlackBox() throws Exception {
        PropertyChangeEvent evt = new PropertyChangeEvent(new Object(), "p", null, null);
        PropertyVetoException e = new PropertyVetoException("rejected", evt);
        check("rejected".equals(e.getMessage()), "message");
        check(evt == e.getPropertyChangeEvent(), "propertyChangeEvent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
