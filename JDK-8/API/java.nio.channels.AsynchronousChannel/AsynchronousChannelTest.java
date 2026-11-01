/*
 * @test
 * @summary java.nio.channels.AsynchronousChannel conformance: interface hierarchy and methods.
 * @run main AsynchronousChannelTest
 */

import java.nio.channels.AsynchronousChannel;
import java.nio.channels.Channel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class AsynchronousChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAsynchronousChannel() {
        check(AsynchronousChannel.class.isInterface(), "is interface");
        check(Channel.class.isAssignableFrom(AsynchronousChannel.class), "extends Channel");
        Set<String> names = new HashSet<String>();
        for (Method m : AsynchronousChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "close" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testAsynchronousChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
