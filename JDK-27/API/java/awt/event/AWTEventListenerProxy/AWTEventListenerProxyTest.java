/*
 * @test
 * @summary java.awt.event.AWTEventListenerProxy conformance: white-box structure + black-box behavior.
 * @run main AWTEventListenerProxyTest
 */

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.AWTEventListenerProxy;
import java.lang.reflect.Modifier;
import java.util.EventListener;

public class AWTEventListenerProxyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Recorder implements AWTEventListener {
        int count;
        public void eventDispatched(AWTEvent e) { count++; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.AWTEventListenerProxy");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "AWTEventListenerProxy is public");
        check(!Modifier.isAbstract(m), "AWTEventListenerProxy is concrete");
        check(c.getSuperclass() == java.util.EventListenerProxy.class, "AWTEventListenerProxy extends EventListenerProxy");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(AWTEventListener.class), "implements AWTEventListener");
        check(c.getConstructor(long.class, AWTEventListener.class) != null, "has (long, AWTEventListener) ctor");
        check(c.getMethod("getEventMask").getReturnType() == long.class, "getEventMask returns long");
    }

    static void testBlackBox() throws Exception {
        Recorder r = new Recorder();
        long mask = AWTEvent.ACTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK;
        AWTEventListenerProxy proxy = new AWTEventListenerProxy(mask, r);

        check(proxy.getEventMask() == mask, "getEventMask round-trips");
        check(proxy.getListener() == r, "getListener returns wrapped listener");
        check(proxy.getEventMask() == (AWTEvent.ACTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK), "mask is bitwise OR");

        // delegation: eventDispatched forwards to the wrapped listener
        check(r.count == 0, "listener not yet called");
        proxy.eventDispatched(new AWTEvent(new Object(), 1234) {});
        check(r.count == 1, "eventDispatched delegates once");
        proxy.eventDispatched(new AWTEvent(new Object(), 1234) {});
        check(r.count == 2, "eventDispatched delegates twice");

        // type relationships
        check(proxy instanceof AWTEventListener, "proxy instanceof AWTEventListener");
        check(proxy instanceof EventListener, "proxy instanceof EventListener");

        // separate proxy with a distinct mask
        AWTEventListenerProxy p2 = new AWTEventListenerProxy(AWTEvent.MOUSE_EVENT_MASK, r);
        check(p2.getEventMask() == AWTEvent.MOUSE_EVENT_MASK, "second proxy mask");
        check(p2.getListener() == r, "second proxy listener");

        // mask of zero is representable
        AWTEventListenerProxy p3 = new AWTEventListenerProxy(0L, r);
        check(p3.getEventMask() == 0L, "zero mask round-trips");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
