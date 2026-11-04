/*
 * @test
 * @summary java.beans.PropertyVetoException conformance: full contract.
 *          extends Exception, message via superclass, getPropertyChangeEvent
 *          identity (incl. null event), and the throw/catch cycle used to
 *          veto a constrained property change.
 * @run main PropertyVetoExceptionTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

public class PropertyVetoExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testBasics() {
        PropertyChangeEvent evt = new PropertyChangeEvent(new Object(), "p", "old", "new");
        PropertyVetoException e = new PropertyVetoException("rejected", evt);
        check(Exception.class.isAssignableFrom(e.getClass()), "extends Exception");
        check("rejected".equals(e.getMessage()), "message");
        check(evt == e.getPropertyChangeEvent(), "propertyChangeEvent identity");

        PropertyVetoException n = new PropertyVetoException(null, null);
        check(n.getMessage() == null, "null message");
        check(n.getPropertyChangeEvent() == null, "null event retained");
    }

    static void testThrowCatch() {
        PropertyChangeEvent evt = new PropertyChangeEvent("src", "p", "a", "b");
        try {
            throw new PropertyVetoException("veto", evt);
        } catch (PropertyVetoException pve) {
            check("veto".equals(pve.getMessage()), "caught message");
            check("p".equals(pve.getPropertyChangeEvent().getPropertyName()), "caught event property");
            passed++;
        }
    }


    static void testStructure() {
        check(Exception.class.isAssignableFrom(PropertyVetoException.class), "extends Exception");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testBasics();
        testThrowCatch();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
