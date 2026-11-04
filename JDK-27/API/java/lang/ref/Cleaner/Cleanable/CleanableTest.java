/*
 * @test
 * @summary java.lang.ref.Cleaner.Cleanable conformance: white-box structure + black-box behavior.
 * @run main CleanableTest
 */

import java.lang.ref.Cleaner;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

public class CleanableTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ref.Cleaner$Cleanable");
        check(c.isInterface(), "Cleanable is an interface");
        check(c.getDeclaringClass() == Cleaner.class, "nested in Cleaner");
        check(c.getMethods().length == 1, "exactly one abstract method");
        Method clean = c.getMethod("clean");
        check(clean.getReturnType() == void.class, "clean() returns void");
        check(clean.getExceptionTypes().length == 0, "clean() declares no checked exceptions");
        check(Modifier.isAbstract(clean.getModifiers()), "clean() abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // direct lambda implementation of the interface
        AtomicInteger count = new AtomicInteger();
        Cleaner.Cleanable asLambda = count::incrementAndGet;
        asLambda.clean();
        check(count.get() == 1, "lambda clean() runs action");
        asLambda.clean();
        check(count.get() == 2, "plain lambda impl has no at-most-once guard of its own");

        // anonymous class implementation wrapping an AutoCloseable-like close chain
        AtomicInteger closed = new AtomicInteger();
        Cleaner.Cleanable closeAdapter = new Cleaner.Cleanable() {
            @Override public void clean() { closed.addAndGet(3); }
        };
        closeAdapter.clean();
        closeAdapter.clean();
        check(closed.get() == 6, "anonymous impl clean() accumulates");

        // real instance from Cleaner.register: at-most-once semantics enforced by JDK impl
        Cleaner cleaner = Cleaner.create();
        AtomicInteger runs = new AtomicInteger();
        Cleaner.Cleanable registered = cleaner.register(new Object(), runs::incrementAndGet);
        check(registered instanceof Cleaner.Cleanable, "registered is a Cleanable");
        registered.clean();
        registered.clean();
        registered.clean();
        check(runs.get() == 1, "JDK-provided Cleanable runs action exactly once");

        // interface is usable as a generic resource type
        java.util.List<Cleaner.Cleanable> batch = new java.util.ArrayList<>();
        AtomicInteger total = new AtomicInteger();
        for (int i = 0; i < 3; i++) {
            batch.add(cleaner.register(new Object(), total::incrementAndGet));
        }
        for (Cleaner.Cleanable cl : batch) cl.clean();
        check(total.get() == 3, "batch of three cleanables each run once");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
