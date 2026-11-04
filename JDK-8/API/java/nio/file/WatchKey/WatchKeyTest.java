/*
 * @test
 * @summary java.nio.file.WatchKey conformance: hierarchy and methods.
 * @run main WatchKeyTest
 */

import java.nio.file.WatchKey;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class WatchKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWatchKey() {
        check(WatchKey.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : WatchKey.class.getMethods()) names.add(m.getName());
        String[] required = { "isValid", "pollEvents", "reset", "cancel", "watchable" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testWatchKey();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
