/*
 * @test
 * @summary java.nio.file.WatchEvent.Kind conformance: hierarchy and methods.
 * @run main KindTest
 */

import java.nio.file.WatchEvent.Kind;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class KindTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testKind() {
        check(Kind.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : Kind.class.getMethods()) names.add(m.getName());
        String[] required = { "name", "type" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testKind();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
