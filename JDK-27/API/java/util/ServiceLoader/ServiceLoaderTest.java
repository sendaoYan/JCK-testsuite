/*
 * @test
 * @summary java.util.ServiceLoader conformance: white-box structure plus black-box behavior of
 *          empty iteration, findFirst, stream, and reload for a service with no registered
 *          providers (deterministic).
 * @run main ServiceLoaderTest
 */

import java.util.Optional;
import java.util.ServiceLoader;

public class ServiceLoaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Svc {
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.ServiceLoader");
        check(!c.isInterface(), "ServiceLoader is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "ServiceLoader is final");
        check(Iterable.class.isAssignableFrom(c), "ServiceLoader implements Iterable");
        c.getMethod("iterator");
        c.getMethod("stream");
        c.getMethod("findFirst");
        c.getMethod("reload");
        c.getMethod("load", Class.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ServiceLoader<Svc> sl = ServiceLoader.load(Svc.class);
        check(!sl.iterator().hasNext(), "no providers for an unregistered service");
        Optional<Svc> first = sl.findFirst();
        check(!first.isPresent(), "findFirst empty when no providers");
        check(sl.stream().count() == 0, "stream empty when no providers");
        sl.reload();
        check(!sl.iterator().hasNext(), "reload still finds none");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
