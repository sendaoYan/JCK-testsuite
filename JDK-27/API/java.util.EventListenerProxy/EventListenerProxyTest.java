/*
 * @test
 * @summary java.util.EventListenerProxy API conformance: getListener.
 * @run main EventListenerProxyTest
 */

import java.util.EventListener;
import java.util.EventListenerProxy;

public class EventListenerProxyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class MyListener implements EventListener {
    }

    static class Proxy extends EventListenerProxy<MyListener> {
        Proxy(MyListener listener) {
            super(listener);
        }
    }

    static void testEventListenerProxy() {
        MyListener listener = new MyListener();
        EventListenerProxy<MyListener> p = new Proxy(listener);
        check(p instanceof EventListener, "implements EventListener");
        check(p.getListener() == listener, "getListener");
    }

    public static void main(String[] args) {
        testEventListenerProxy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
