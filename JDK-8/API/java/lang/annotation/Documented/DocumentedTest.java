/*
 * @test
 * @summary java.lang.annotation.Documented conformance: presence on a documented annotation.
 * @run main DocumentedTest
 */

import java.lang.annotation.Documented;

public class DocumentedTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @Documented
    @interface Doc {
    }

    static void testDocumented() {
        check(Doc.class.isAnnotation(), "Doc is annotation");
        Documented d = Doc.class.getAnnotation(Documented.class);
        check(d != null, "Documented present");
        check(d.annotationType() == Documented.class, "annotationType");
    }

    public static void main(String[] args) {
        testDocumented();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
