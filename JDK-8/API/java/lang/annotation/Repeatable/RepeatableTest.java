/*
 * @test
 * @summary java.lang.annotation.Repeatable conformance: value points at container annotation.
 * @run main RepeatableTest
 */

import java.lang.annotation.Repeatable;

public class RepeatableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @interface Container {
        Rep[] value();
    }

    @Repeatable(Container.class)
    @interface Rep {
    }

    static void testRepeatable() {
        check(Rep.class.isAnnotation(), "Rep is annotation");
        Repeatable r = Rep.class.getAnnotation(Repeatable.class);
        check(r != null, "Repeatable present");
        check(r.value() == Container.class, "value is Container");
    }

    public static void main(String[] args) {
        testRepeatable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
