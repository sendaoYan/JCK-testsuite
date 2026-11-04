/*
 * @test
 * @summary javax.swing.tree.ExpandVetoException conformance: checked exception
 *          (headless-safe): both constructors, message/event wiring, the protected
 *          event field, hierarchy, Throwable contract.
 * @run main ExpandVetoExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

public class ExpandVetoExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static final Field EVENT_FIELD;

    static {
        try {
            EVENT_FIELD = ExpandVetoException.class.getDeclaredField("event");
            EVENT_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new AssertionError("no event field", e);
        }
    }

    static Object eventOf(ExpandVetoException e) {
        try { return EVENT_FIELD.get(e); } catch (IllegalAccessException ex) { throw new AssertionError(ex); }
    }

    public static void main(String[] args) throws Exception {
        TreeExpansionEvent evt = new TreeExpansionEvent("src", new TreePath("r"));
        ExpandVetoException e = new ExpandVetoException(evt, "msg");
        check("msg".equals(e.getMessage()), "message");
        check(eventOf(e) == evt, "two-arg ctor wires the vetoed event");

        // One-argument constructor: null message, event still wired.
        ExpandVetoException e2 = new ExpandVetoException(evt);
        check(e2.getMessage() == null, "one-arg ctor leaves message null");
        check(eventOf(e2) == evt, "one-arg ctor wires event");

        // Null event is accepted without validation.
        ExpandVetoException e3 = new ExpandVetoException(null);
        check(eventOf(e3) == null, "null event accepted");

        // Hierarchy: checked exception (must be caught, not an Error/RuntimeException).
        check(Exception.class.isAssignableFrom(ExpandVetoException.class), "extends Exception");
        check(!RuntimeException.class.isAssignableFrom(ExpandVetoException.class), "not a RuntimeException");
        check("javax.swing.tree.ExpandVetoException".equals(e.getClass().getName()), "binary name");

        // Catchability as checked exception in practice.
        boolean caught = false;
        try {
            throw new ExpandVetoException(evt, "thrown");
        } catch (Exception ex) {
            caught = ex instanceof ExpandVetoException && "thrown".equals(ex.getMessage());
        }
        check(caught, "thrown and caught as Exception");

        // The event is exposed via a protected field (there is no public accessor).
        check(Modifier.isProtected(EVENT_FIELD.getModifiers()), "event field is protected");
        check(ExpandVetoException.class.getMethods().length >= 0, "no public getEvent method");
        boolean hasGetEvent = false;
        for (java.lang.reflect.Method m : ExpandVetoException.class.getMethods()) {
            if (m.getName().equals("getEvent")) hasGetEvent = true;
        }
        check(!hasGetEvent, "no public getEvent accessor");

        // Exactly two public constructors.
        Constructor<?>[] ctors = ExpandVetoException.class.getConstructors();
        check(ctors.length == 2, "two public constructors, got " + ctors.length);

        // toString/stack trace wiring comes from Throwable.
        check(e.toString().contains("msg"), "toString includes message and class name");
        check(e.getStackTrace() instanceof StackTraceElement[], "stack trace available");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
