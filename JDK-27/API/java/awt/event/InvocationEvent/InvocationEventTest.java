/*
 * @test
 * @summary java.awt.event.InvocationEvent conformance: white-box structure + black-box behavior.
 * @run main InvocationEventTest
 */

import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import java.awt.event.InvocationEvent;
import java.lang.reflect.Modifier;

public class InvocationEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.InvocationEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "InvocationEvent is public");
        check(!Modifier.isAbstract(m), "InvocationEvent is concrete");
        check(c.getSuperclass() == AWTEvent.class, "InvocationEvent extends AWTEvent");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(ActiveEvent.class), "InvocationEvent implements ActiveEvent");
        check(c.getField("INVOCATION_DEFAULT").getType() == int.class, "INVOCATION_DEFAULT int");
        check(c.getConstructor(Object.class, Runnable.class) != null, "has (Object,Runnable) ctor");
        check(c.getConstructor(Object.class, Runnable.class, Object.class, boolean.class) != null, "has 4-arg ctor");
        check(c.getConstructor(Object.class, Runnable.class, Runnable.class, boolean.class) != null, "has listener ctor");
    }

    static void testBlackBox() throws Exception {
        Object src = new Object();
        final boolean[] ran = new boolean[1];
        long before = System.currentTimeMillis();
        InvocationEvent e = new InvocationEvent(src, new Runnable() { public void run() { ran[0] = true; } });

        check(e.getID() == InvocationEvent.INVOCATION_DEFAULT, "default id INVOCATION_DEFAULT");
        check(e.getSource() == src, "getSource round-trips");
        check(e.getWhen() >= before, "getWhen >= construction time");
        check(!e.isDispatched(), "isDispatched false before dispatch");
        check(e.getException() == null, "getException null before dispatch");

        e.dispatch();
        check(ran[0], "dispatch() runs the runnable");
        check(e.isDispatched(), "isDispatched true after dispatch");
        check(e.getException() == null, "getException null after success");

        // dispatch propagates exceptions when catchExceptions is false (default)
        InvocationEvent thrower = new InvocationEvent(src, new Runnable() { public void run() { throw new IllegalStateException("boom"); } });
        boolean propagated = false;
        try { thrower.dispatch(); } catch (IllegalStateException x) { propagated = true; }
        check(propagated, "default ctor does not catch exceptions");
        check(thrower.isDispatched(), "isDispatched true even when runnable throws");

        // catchThrowables=true stores the exception instead of propagating
        InvocationEvent catcher = new InvocationEvent(src, new Runnable() { public void run() { throw new IllegalStateException("stored"); } }, new Object(), true);
        catcher.dispatch(); // must not throw
        check(catcher.isDispatched(), "catchThrowables event dispatched");
        check(catcher.getException() instanceof IllegalStateException, "getException returns stored exception");
        check(catcher.getThrowable() instanceof IllegalStateException, "getThrowable returns stored throwable");

        // catchThrowables=false -> getException returns null even if it would have thrown
        InvocationEvent noCatch = new InvocationEvent(src, new Runnable() { public void run() { } }, new Object(), false);
        check(noCatch.getException() == null, "getException null when catchThrowables false");

        // listener constructor dispatches runnable and then listener
        final boolean[] listened = new boolean[1];
        InvocationEvent withListener = new InvocationEvent(src, new Runnable() { public void run() { } },
                new Runnable() { public void run() { listened[0] = true; } }, false);
        withListener.dispatch();
        check(listened[0], "listener runnable runs after dispatch");
        check(withListener.isDispatched(), "listener event dispatched");

        // documented constants
        check(InvocationEvent.INVOCATION_FIRST == 1200, "INVOCATION_FIRST == 1200");
        check(InvocationEvent.INVOCATION_DEFAULT == 1200, "INVOCATION_DEFAULT == 1200");
        check(InvocationEvent.INVOCATION_LAST == 1200, "INVOCATION_LAST == 1200");

        // paramString
        String ps = e.paramString();
        check(ps.contains("INVOCATION_DEFAULT"), "paramString id");
        check(ps.contains("runnable="), "paramString runnable");
        check(ps.contains("catchExceptions="), "paramString catchExceptions");

        check(e instanceof ActiveEvent, "InvocationEvent instanceof ActiveEvent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
