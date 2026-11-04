/*
 * @test
 * @summary java.nio.file.Watchable conformance: hierarchy and methods.
 * @run main WatchableTest
 */

import java.nio.file.Watchable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class WatchableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWatchable() {
        check(Watchable.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : Watchable.class.getMethods()) names.add(m.getName());
        String[] required = { "register" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testWatchable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
