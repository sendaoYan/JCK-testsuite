/*
 * @test
 * @summary java.nio.channels.spi.AsynchronousChannelProvider conformance: provider().
 * @run main AsynchronousChannelProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.HashSet;
import java.util.Set;

public class AsynchronousChannelProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAsynchronousChannelProvider() {
        check(Modifier.isAbstract(AsynchronousChannelProvider.class.getModifiers()),
              "abstract");
        check(AsynchronousChannelProvider.provider() != null, "provider() non-null");
        Set<String> names = new HashSet<String>();
        for (Method m : AsynchronousChannelProvider.class.getMethods()) names.add(m.getName());
        check(names.contains("openAsynchronousChannelGroup"), "has openAsynchronousChannelGroup");
        check(names.contains("openAsynchronousSocketChannel"), "has openAsynchronousSocketChannel");
        check(names.contains("openAsynchronousServerSocketChannel"),
              "has openAsynchronousServerSocketChannel");
    }

    public static void main(String[] args) {
        testAsynchronousChannelProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
