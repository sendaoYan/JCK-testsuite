/*
 * @test
 * @summary java.beans.EventHandler conformance: white-box structure + black-box behavior.
 * @run main EventHandlerTest
 */

import java.beans.EventHandler;

public class EventHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class Target {
        boolean fired;
        public void fire() { fired = true; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.EventHandler");
        check(java.lang.reflect.InvocationHandler.class.isAssignableFrom(c), "implements InvocationHandler");
    }

    static void testBlackBox() throws Exception {
        Target t = new Target();
        EventHandler eh = new EventHandler(t, "fire", null, null);
        check("fire".equals(eh.getAction()), "getAction");
        check(t == eh.getTarget(), "getTarget");
        check(eh.getEventPropertyName() == null, "eventPropertyName null default");
        check(eh.getListenerMethodName() == null, "listenerMethodName null default");

        java.awt.event.ActionListener al = (java.awt.event.ActionListener)
            EventHandler.create(java.awt.event.ActionListener.class, t, "fire");
        al.actionPerformed(null);
        check(t.fired, "create proxy invokes target action");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
