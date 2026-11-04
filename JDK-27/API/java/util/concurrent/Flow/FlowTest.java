/*
 * @test
 * @summary java.util.concurrent.Flow conformance: white-box structure + black-box behavior.
 * @run main FlowTest
 */

import java.util.concurrent.*;

public class FlowTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.Flow");
        check(!c.isInterface(), "Flow is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Flow is final");
        hasMethod(c, "defaultBufferSize");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Flow.defaultBufferSize() > 0, "defaultBufferSize() > 0");
        check(Flow.Publisher.class.isInterface(), "Publisher is an interface");
        check(Flow.Subscriber.class.isInterface(), "Subscriber is an interface");
        check(Flow.Subscription.class.isInterface(), "Subscription is an interface");
        check(Flow.Processor.class.isInterface(), "Processor is an interface");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
