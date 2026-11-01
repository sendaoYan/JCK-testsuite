/*
 * @test
 * @summary java.nio.channels.ByteChannel conformance: interface hierarchy and methods.
 * @run main ByteChannelTest
 */

import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testByteChannel() {
        check(ByteChannel.class.isInterface(), "is interface");
        check(ReadableByteChannel.class.isAssignableFrom(ByteChannel.class), "extends ReadableByteChannel");
        check(WritableByteChannel.class.isAssignableFrom(ByteChannel.class), "extends WritableByteChannel");
        Set<String> names = new HashSet<String>();
        for (Method m : ByteChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "read", "write" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testByteChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
