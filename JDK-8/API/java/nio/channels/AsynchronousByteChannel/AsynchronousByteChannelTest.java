/*
 * @test
 * @summary java.nio.channels.AsynchronousByteChannel conformance: interface hierarchy and methods.
 * @run main AsynchronousByteChannelTest
 */

import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousChannel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class AsynchronousByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAsynchronousByteChannel() {
        check(AsynchronousByteChannel.class.isInterface(), "is interface");
        check(AsynchronousChannel.class.isAssignableFrom(AsynchronousByteChannel.class), "extends AsynchronousChannel");
        Set<String> names = new HashSet<String>();
        for (Method m : AsynchronousByteChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "read", "write" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testAsynchronousByteChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
