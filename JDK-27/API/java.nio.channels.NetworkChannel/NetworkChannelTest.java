/*
 * @test
 * @summary java.nio.channels.NetworkChannel conformance: interface hierarchy and methods.
 * @run main NetworkChannelTest
 */

import java.nio.channels.NetworkChannel;
import java.nio.channels.Channel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class NetworkChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNetworkChannel() {
        check(NetworkChannel.class.isInterface(), "is interface");
        check(Channel.class.isAssignableFrom(NetworkChannel.class), "extends Channel");
        Set<String> names = new HashSet<String>();
        for (Method m : NetworkChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "bind", "getLocalAddress", "setOption", "getOption", "supportedOptions" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testNetworkChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
