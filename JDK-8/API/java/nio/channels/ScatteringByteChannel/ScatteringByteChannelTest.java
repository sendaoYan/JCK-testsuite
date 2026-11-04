/*
 * @test
 * @summary java.nio.channels.ScatteringByteChannel conformance: interface hierarchy and methods.
 * @run main ScatteringByteChannelTest
 */

import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ScatteringByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testScatteringByteChannel() {
        check(ScatteringByteChannel.class.isInterface(), "is interface");
        check(ReadableByteChannel.class.isAssignableFrom(ScatteringByteChannel.class), "extends ReadableByteChannel");
        Set<String> names = new HashSet<String>();
        for (Method m : ScatteringByteChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "read" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testScatteringByteChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
