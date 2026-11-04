/*
 * @test
 * @summary java.nio.channels.Channel conformance: interface hierarchy and methods.
 * @run main ChannelTest
 */

import java.nio.channels.Channel;
import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testChannel() {
        check(Channel.class.isInterface(), "is interface");
        check(Closeable.class.isAssignableFrom(Channel.class), "extends Closeable");
        Set<String> names = new HashSet<String>();
        for (Method m : Channel.class.getMethods()) names.add(m.getName());
        String[] required = { "isOpen", "close" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
