/*
 * @test
 * @summary java.nio.channels.SeekableByteChannel conformance: interface hierarchy and methods.
 * @run main SeekableByteChannelTest
 */

import java.nio.channels.SeekableByteChannel;
import java.nio.channels.Channel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class SeekableByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSeekableByteChannel() {
        check(SeekableByteChannel.class.isInterface(), "is interface");
        check(Channel.class.isAssignableFrom(SeekableByteChannel.class), "extends Channel");
        Set<String> names = new HashSet<String>();
        for (Method m : SeekableByteChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "read", "write", "position", "size", "truncate" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testSeekableByteChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
