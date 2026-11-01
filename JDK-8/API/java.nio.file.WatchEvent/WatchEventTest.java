/*
 * @test
 * @summary java.nio.file.WatchEvent conformance: hierarchy and methods.
 * @run main WatchEventTest
 */

import java.nio.file.WatchEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class WatchEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWatchEvent() {
        check(WatchEvent.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : WatchEvent.class.getMethods()) names.add(m.getName());
        String[] required = { "kind", "count", "context" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testWatchEvent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
