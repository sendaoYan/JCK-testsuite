/*
 * @test
 * @summary java.nio.channels.spi.AbstractSelector conformance: hierarchy.
 * @run main AbstractSelectorTest
 */

import java.lang.reflect.Modifier;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelector;

public class AbstractSelectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractSelector() {
        check(Modifier.isAbstract(AbstractSelector.class.getModifiers()), "abstract");
        check(Selector.class.isAssignableFrom(AbstractSelector.class), "extends Selector");
    }

    public static void main(String[] args) {
        testAbstractSelector();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
