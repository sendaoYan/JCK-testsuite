/*
 * @test
 * @summary java.nio.file.OpenOption conformance: hierarchy and methods.
 * @run main OpenOptionTest
 */

import java.nio.file.OpenOption;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class OpenOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOpenOption() {
        check(OpenOption.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : OpenOption.class.getMethods()) names.add(m.getName());
        String[] required = {  };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testOpenOption();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
