/*
 * @test
 * @summary java.nio.channels.WritableByteChannel conformance: interface hierarchy and methods.
 * @run main WritableByteChannelTest
 */

import java.nio.channels.WritableByteChannel;
import java.nio.channels.Channel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class WritableByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWritableByteChannel() {
        check(WritableByteChannel.class.isInterface(), "is interface");
        check(Channel.class.isAssignableFrom(WritableByteChannel.class), "extends Channel");
        Set<String> names = new HashSet<String>();
        for (Method m : WritableByteChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "write" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testWritableByteChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
