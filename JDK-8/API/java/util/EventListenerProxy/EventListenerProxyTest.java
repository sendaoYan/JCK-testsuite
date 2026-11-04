/*
 * @test
 * @summary java.util.EventListenerProxy API conformance (Java 8 surface): complete contract
 *          coverage of the constructor (stores listener verbatim, null allowed) and getListener()
 *          returning the exact stored instance.
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

    static void testNullListener() {
        // Constructor does not null-check; null listener is stored and returned.
        EventListenerProxy<MyListener> p = new Proxy(null);
        check(p.getListener() == null, "null listener stored verbatim");
        check(p instanceof EventListener, "still an EventListener");
    }

    public static void main(String[] args) {
        testEventListenerProxy();
        testNullListener();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
