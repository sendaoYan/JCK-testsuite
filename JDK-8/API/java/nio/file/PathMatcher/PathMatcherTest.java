/*
 * @test
 * @summary java.nio.file.PathMatcher conformance: hierarchy and methods.
 * @run main PathMatcherTest
 */

import java.nio.file.PathMatcher;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PathMatcherTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPathMatcher() {
        check(PathMatcher.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : PathMatcher.class.getMethods()) names.add(m.getName());
        String[] required = { "matches" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testPathMatcher();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
