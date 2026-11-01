/*
 * @test
 * @summary java.nio.channels.GatheringByteChannel conformance: interface hierarchy and methods.
 * @run main GatheringByteChannelTest
 */

import java.nio.channels.GatheringByteChannel;
import java.nio.channels.WritableByteChannel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class GatheringByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testGatheringByteChannel() {
        check(GatheringByteChannel.class.isInterface(), "is interface");
        check(WritableByteChannel.class.isAssignableFrom(GatheringByteChannel.class), "extends WritableByteChannel");
        Set<String> names = new HashSet<String>();
        for (Method m : GatheringByteChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "write" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testGatheringByteChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
