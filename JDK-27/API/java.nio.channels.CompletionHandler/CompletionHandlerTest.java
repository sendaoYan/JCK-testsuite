/*
 * @test
 * @summary java.nio.channels.CompletionHandler conformance: interface hierarchy and methods.
 * @run main CompletionHandlerTest
 */

import java.nio.channels.CompletionHandler;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class CompletionHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCompletionHandler() {
        check(CompletionHandler.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : CompletionHandler.class.getMethods()) names.add(m.getName());
        String[] required = { "completed", "failed" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testCompletionHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
