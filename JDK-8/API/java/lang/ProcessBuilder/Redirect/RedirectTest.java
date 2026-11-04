/*
 * @test
 * @summary java.lang.ProcessBuilder.Redirect API conformance: constants/type/from/to/file.
 * @run main RedirectTest
 */

import java.io.File;
import java.lang.ProcessBuilder.Redirect;

public class RedirectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRedirect() {
        check(Redirect.INHERIT.type() == Redirect.Type.INHERIT, "INHERIT.type");
        check(Redirect.INHERIT.file() == null, "INHERIT.file is null");
        check(Redirect.PIPE.type() == Redirect.Type.PIPE, "PIPE.type");

        Redirect from = Redirect.from(new File("in"));
        check(from.type() == Redirect.Type.READ, "from().type is READ");
        check("in".equals(from.file().getName()), "from().file");

        Redirect to = Redirect.to(new File("out"));
        check(to.type() == Redirect.Type.WRITE, "to().type is WRITE");
        check("out".equals(to.file().getName()), "to().file");
    }

    public static void main(String[] args) {
        testRedirect();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
