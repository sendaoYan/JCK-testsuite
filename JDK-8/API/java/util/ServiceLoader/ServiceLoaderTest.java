/*
 * @test
 * @summary java.util.ServiceLoader API conformance: empty iteration with no providers.
 * @run main ServiceLoaderTest
 */

import java.util.ServiceLoader;

public class ServiceLoaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Svc {
    }

    static void testServiceLoader() {
        ServiceLoader<Svc> sl = ServiceLoader.load(Svc.class);
        check(!sl.iterator().hasNext(), "no providers for a service with no registration");
        sl.reload();
        check(!sl.iterator().hasNext(), "reload also finds none");
    }

    public static void main(String[] args) {
        testServiceLoader();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
