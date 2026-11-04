/*
 * @test
 * @summary java.nio.file.CopyOption conformance: hierarchy and methods.
 * @run main CopyOptionTest
 */

import java.nio.file.CopyOption;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CopyOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCopyOption() {
        check(CopyOption.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : CopyOption.class.getMethods()) names.add(m.getName());
        String[] required = {  };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testCopyOption();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
