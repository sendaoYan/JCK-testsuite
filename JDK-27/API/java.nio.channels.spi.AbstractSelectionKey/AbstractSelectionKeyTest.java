/*
 * @test
 * @summary java.nio.channels.spi.AbstractSelectionKey conformance: hierarchy.
 * @run main AbstractSelectionKeyTest
 */

import java.lang.reflect.Modifier;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.AbstractSelectionKey;

public class AbstractSelectionKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractSelectionKey() {
        check(Modifier.isAbstract(AbstractSelectionKey.class.getModifiers()), "abstract");
        check(SelectionKey.class.isAssignableFrom(AbstractSelectionKey.class),
              "extends SelectionKey");
    }

    public static void main(String[] args) {
        testAbstractSelectionKey();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
