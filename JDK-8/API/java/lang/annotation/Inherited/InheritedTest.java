/*
 * @test
 * @summary java.lang.annotation.Inherited conformance: presence on an inherited annotation.
 * @run main InheritedTest
 */

import java.lang.annotation.Inherited;

public class InheritedTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @Inherited
    @interface Inherit {
    }

    static void testInherited() {
        check(Inherit.class.isAnnotation(), "Inherit is annotation");
        Inherited i = Inherit.class.getAnnotation(Inherited.class);
        check(i != null, "Inherited present");
        check(i.annotationType() == Inherited.class, "annotationType");
    }

    public static void main(String[] args) {
        testInherited();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
