/*
 * @test
 * @summary java.nio.channels.ReadableByteChannel conformance: interface hierarchy and methods.
 * @run main ReadableByteChannelTest
 */

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Channel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ReadableByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testReadableByteChannel() {
        check(ReadableByteChannel.class.isInterface(), "is interface");
        check(Channel.class.isAssignableFrom(ReadableByteChannel.class), "extends Channel");
        Set<String> names = new HashSet<String>();
        for (Method m : ReadableByteChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "read" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testReadableByteChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
