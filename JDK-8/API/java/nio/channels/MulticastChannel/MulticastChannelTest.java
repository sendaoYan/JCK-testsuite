/*
 * @test
 * @summary java.nio.channels.MulticastChannel conformance: interface hierarchy and methods.
 * @run main MulticastChannelTest
 */

import java.nio.channels.MulticastChannel;
import java.nio.channels.NetworkChannel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class MulticastChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMulticastChannel() {
        check(MulticastChannel.class.isInterface(), "is interface");
        check(NetworkChannel.class.isAssignableFrom(MulticastChannel.class), "extends NetworkChannel");
        Set<String> names = new HashSet<String>();
        for (Method m : MulticastChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "join", "close" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testMulticastChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
