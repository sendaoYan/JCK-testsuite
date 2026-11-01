/*
 * @test
 * @summary java.nio.channels.spi.AbstractInterruptibleChannel conformance: hierarchy.
 * @run main AbstractInterruptibleChannelTest
 */

import java.lang.reflect.Modifier;
import java.nio.channels.Channel;
import java.nio.channels.spi.AbstractInterruptibleChannel;

public class AbstractInterruptibleChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractInterruptibleChannel() {
        check(Modifier.isAbstract(AbstractInterruptibleChannel.class.getModifiers()),
              "abstract");
        check(Channel.class.isAssignableFrom(AbstractInterruptibleChannel.class),
              "implements Channel");
    }

    public static void main(String[] args) {
        testAbstractInterruptibleChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
