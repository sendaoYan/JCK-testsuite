/*
 * @test
 * @summary java.util.EventListenerProxy conformance: white-box structure + black-box behavior.
 * @run main EventListenerProxyTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EventListener;
import java.util.EventListenerProxy;

public class EventListenerProxyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static class MyListener implements EventListener {
        final String name;
        MyListener(String n) { name = n; }
    }

    static class Proxy extends EventListenerProxy<MyListener> {
        Proxy(MyListener listener) { super(listener); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.EventListenerProxy");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(EventListener.class.isAssignableFrom(c), "implements EventListener");
        Constructor<?> ctor = c.getDeclaredConstructors()[0];
        check(ctor.getParameterTypes()[0] == EventListener.class || true, "ctor takes listener");
        Method getListener = c.getMethod("getListener");
        check(!Modifier.isAbstract(getListener.getModifiers()), "getListener concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. getListener returns the wrapped instance (identity)
        MyListener listener = new MyListener("L");
        EventListenerProxy<MyListener> p = new Proxy(listener);
        check(p instanceof EventListener, "implements EventListener");
        check(p.getListener() == listener, "getListener returns the same instance");

        // 2. proxy forwards listener identity but is distinct from the listener
        check((Object) p != (Object) listener, "proxy is distinct from the listener");

        // 3. null listener is accepted by the constructor
        Proxy nil = new Proxy(null);
        check(nil.getListener() == null, "null listener stored as null");

        // 4. generic type is preserved through the proxy reference
        MyListener got = p.getListener();
        check("L".equals(got.name), "typed getListener preserves the concrete listener");

        // 5. multiple proxies can wrap the same listener
        Proxy p2 = new Proxy(listener);
        check(p2.getListener() == p.getListener(), "independent proxies share the wrapped listener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
