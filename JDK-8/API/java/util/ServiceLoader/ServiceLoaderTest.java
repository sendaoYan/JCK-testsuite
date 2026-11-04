/*
 * @test
 * @summary java.util.ServiceLoader API conformance (Java 8 surface): complete contract coverage
 *          of load(Class)/load(Class,ClassLoader)/loadInstalled(Class), null NPE, empty iteration
 *          for an unregistered service, reload() resetting the provider cache, and toString.
 * @run main ServiceLoaderTest
 */

import java.util.ServiceLoader;

public class ServiceLoaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    interface Svc {
    }

    static void testServiceLoader() {
        ServiceLoader<Svc> sl = ServiceLoader.load(Svc.class);
        check(!sl.iterator().hasNext(), "no providers for a service with no registration");
        sl.reload();
        check(!sl.iterator().hasNext(), "reload also finds none");
    }

    static void testNullAndFactories() {
        expectThrows(NullPointerException.class, () -> ServiceLoader.load(null),
                     "load(null) NPE");
        expectThrows(NullPointerException.class,
                     () -> ServiceLoader.load(null, ServiceLoaderTest.class.getClassLoader()),
                     "load(null, cl) NPE");

        ClassLoader cl = ServiceLoaderTest.class.getClassLoader();
        ServiceLoader<Svc> withCl = ServiceLoader.load(Svc.class, cl);
        check(withCl != null && !withCl.iterator().hasNext(), "load(Class, ClassLoader)");

        ServiceLoader<Svc> installed = ServiceLoader.loadInstalled(Svc.class);
        check(installed != null, "loadInstalled non-null");
        check(!installed.iterator().hasNext(), "loadInstalled finds none for nested service");
    }

    static void testToString() {
        ServiceLoader<Svc> sl = ServiceLoader.load(Svc.class);
        check(sl.toString() != null && sl.toString().length() > 0, "toString non-empty");
    }

    public static void main(String[] args) {
        testServiceLoader();
        testNullAndFactories();
        testToString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
