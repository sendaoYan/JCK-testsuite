/*
 * @test
 * @summary java.util.concurrent.locks.AbstractOwnableSynchronizer conformance: white-box
 *          structure + black-box owner-thread behavior via a minimal subclass.
 * @run main AbstractOwnableSynchronizerTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.concurrent.locks.AbstractOwnableSynchronizer;

public class AbstractOwnableSynchronizerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Minimal subclass exposing the protected owner accessors.
    static class AOS extends AbstractOwnableSynchronizer {
        void set(Thread t) { setExclusiveOwnerThread(t); }
        Thread get() { return getExclusiveOwnerThread(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.locks.AbstractOwnableSynchronizer");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getDeclaredConstructor() != null
              && !Modifier.isPublic(c.getDeclaredConstructor().getModifiers()),
              "protected constructor");
        java.lang.reflect.Method getter = c.getDeclaredMethod("getExclusiveOwnerThread");
        java.lang.reflect.Method setter = c.getDeclaredMethod("setExclusiveOwnerThread", Thread.class);
        check(Modifier.isProtected(getter.getModifiers()) && Modifier.isFinal(getter.getModifiers()),
              "getExclusiveOwnerThread protected final");
        check(Modifier.isProtected(setter.getModifiers()) && Modifier.isFinal(setter.getModifiers()),
              "setExclusiveOwnerThread protected final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AOS aos = new AOS();
        check(aos.get() == null, "initial owner null");
        aos.set(Thread.currentThread());
        check(aos.get() == Thread.currentThread(), "set/get owner");
        aos.set(null);
        check(aos.get() == null, "owner cleared via set(null)");
        Thread other = new Thread(() -> {});
        aos.set(other);
        check(aos.get() == other, "owner overwritten by another thread reference");
    }

    static void testCrossThreadSetAndGet() throws Exception {
        final AOS aos = new AOS();
        aos.set(Thread.currentThread());
        final java.util.concurrent.CountDownLatch set = new java.util.concurrent.CountDownLatch(1);
        Thread t = new Thread(() -> {
            aos.set(Thread.currentThread()); // another thread may set/clear the owner
            set.countDown();
        });
        t.setDaemon(true);
        t.start();
        if (!set.await(5, java.util.concurrent.TimeUnit.SECONDS))
            throw new AssertionError("timeout waiting for other thread to set the owner");
        t.join(5000);
        check(aos.get() == t, "owner visible across threads equals the setter thread");
        aos.set(null);
        check(aos.get() == null, "owner reset from main");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCrossThreadSetAndGet();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
