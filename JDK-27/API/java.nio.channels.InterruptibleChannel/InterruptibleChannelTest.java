/*
 * @test
 * @summary java.nio.channels.InterruptibleChannel conformance: interface hierarchy and methods.
 * @run main InterruptibleChannelTest
 */

import java.nio.channels.InterruptibleChannel;
import java.nio.channels.Channel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class InterruptibleChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInterruptibleChannel() {
        check(InterruptibleChannel.class.isInterface(), "is interface");
        check(Channel.class.isAssignableFrom(InterruptibleChannel.class), "extends Channel");
        Set<String> names = new HashSet<String>();
        for (Method m : InterruptibleChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "close" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testInterruptibleChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
