/*
 * @test
 * @summary java.nio.channels.spi.AbstractSelectableChannel conformance: hierarchy.
 * @run main AbstractSelectableChannelTest
 */

import java.lang.reflect.Modifier;
import java.nio.channels.SelectableChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

public class AbstractSelectableChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractSelectableChannel() {
        check(Modifier.isAbstract(AbstractSelectableChannel.class.getModifiers()),
              "abstract");
        check(SelectableChannel.class.isAssignableFrom(AbstractSelectableChannel.class),
              "extends SelectableChannel");
    }

    public static void main(String[] args) {
        testAbstractSelectableChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
