/*
 * @test
 * @summary java.nio.file.attribute.AttributeView conformance: hierarchy and methods.
 * @run main AttributeViewTest
 */

import java.nio.file.attribute.AttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAttributeView() {
        check(AttributeView.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : AttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "name" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testAttributeView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
