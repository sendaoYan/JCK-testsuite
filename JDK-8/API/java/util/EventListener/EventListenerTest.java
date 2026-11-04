/*
 * @test
 * @summary java.util.EventListener API conformance: marker interface with no methods;
 *          sub-interface extension and the EventListenerProxy<T extends EventListener>
 *          wrapper contract (getListener identity, EventListener assignability).
 * @run main EventListenerTest
 */

import java.util.EventListener;
import java.util.EventListenerProxy;

public class EventListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class MyListener implements EventListener {
    }

    static interface SubListener extends EventListener {
    }

    static class RecordingListener implements EventListener {
        int events;
    }

    /** Minimal concrete proxy: EventListenerProxy is abstract. */
    static class MyProxy extends EventListenerProxy<RecordingListener> {
        MyProxy(RecordingListener listener) { super(listener); }
    }

    static void testEventListener() {
        check(EventListener.class.isInterface(), "is an interface");
        check(EventListener.class.getDeclaredMethods().length == 0, "marker interface declares no methods");
        check(new MyListener() instanceof EventListener, "is implementable");
    }

    // ============ contract deepening (appended) ============

    static void testStructure() {
        check("java.util.EventListener".equals(EventListener.class.getName()),
              "declared in java.util");
        check(EventListener.class.getDeclaredFields().length == 0,
              "marker interface declares no fields");
        check(new SubListener() {} instanceof EventListener,
              "sub-interface instances are EventListeners");
        check(EventListener.class.isAssignableFrom(MyListener.class),
              "assignability: EventListener <- MyListener");
        check(!EventListener.class.isAssignableFrom(String.class),
              "assignability: String is not a listener");
        // the java.util proxy hierarchy serves as the canonical extension point
        check(EventListener.class.isAssignableFrom(EventListenerProxy.class),
              "EventListenerProxy is itself an EventListener");
        check(java.lang.reflect.Modifier.isAbstract(EventListenerProxy.class.getModifiers()),
              "EventListenerProxy is abstract");
    }

    static void testProxyContract() {
        RecordingListener inner = new RecordingListener();
        MyProxy proxy = new MyProxy(inner);
        check(proxy instanceof EventListener, "proxy is an EventListener");
        check(proxy.getListener() == inner, "getListener returns constructor argument by identity");
        check(proxy.getListener() == proxy.getListener(), "getListener stable");
        // proxy delegates nothing: the wrapped listener is untouched
        check(inner.events == 0, "proxy does not touch wrapped listener state");
        // proxies have identity equality (no equals override)
        check(!proxy.equals(new MyProxy(inner)), "proxy equality is identity, not content");
        check(proxy.equals(proxy), "identity equality reflexive");
    }

    public static void main(String[] args) {
        testEventListener();
        testStructure();
        testProxyContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
