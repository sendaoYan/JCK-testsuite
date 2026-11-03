/*
 * @test
 * @summary java.lang.ProcessBuilder.Redirect conformance: white-box structure + black-box behavior.
 * @run main RedirectTest
 */

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RedirectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Redirect.class;
        check(Modifier.isAbstract(c.getModifiers()), "Redirect is abstract");
        check(c.getConstructors().length == 0, "no public constructors");
        Method type = c.getMethod("type");
        check(Modifier.isAbstract(type.getModifiers()), "type() is abstract");
        check(type.getReturnType() == Redirect.Type.class, "type() returns Redirect.Type");
        check(c.getMethod("file").getReturnType() == File.class, "file() returns File");
        for (Constructor<?> ctor : c.getDeclaredConstructors()) {
            check(!Modifier.isPublic(ctor.getModifiers()), "no accessible constructor");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testConstants() {
        check(Redirect.PIPE.type() == Redirect.Type.PIPE, "PIPE.type");
        check(Redirect.PIPE.file() == null, "PIPE.file is null");
        check(Redirect.INHERIT.type() == Redirect.Type.INHERIT, "INHERIT.type");
        check(Redirect.INHERIT.file() == null, "INHERIT.file is null");
        check(Redirect.DISCARD.type() == Redirect.Type.WRITE, "DISCARD.type is WRITE");
        check(Redirect.DISCARD.file() != null, "DISCARD.file is the temp sink");
        check(Redirect.PIPE.equals(Redirect.PIPE), "constants are singletons (equals self)");
    }

    static void testFileRedirects() {
        File in = new File("in");
        Redirect from = Redirect.from(in);
        check(from.type() == Redirect.Type.READ, "from().type is READ");
        check(from.file() == in, "from().file is the given file");
        File out = new File("out");
        Redirect to = Redirect.to(out);
        check(to.type() == Redirect.Type.WRITE, "to().type is WRITE");
        check(to.file() == out, "to().file is the given file");
        Redirect app = Redirect.appendTo(out);
        check(app.type() == Redirect.Type.APPEND, "appendTo().type is APPEND");
        check(app.file() == out, "appendTo().file is the given file");
    }

    static void testValueSemantics() {
        File f = new File("same");
        check(Redirect.from(f).equals(Redirect.from(f)), "from(f) equals from(f)");
        check(Redirect.from(f).hashCode() == Redirect.from(f).hashCode(), "hashCode consistent");
        check(!Redirect.from(f).equals(Redirect.to(f)), "READ and WRITE redirects differ");
        check(!Redirect.from(f).equals(Redirect.from(new File("other"))), "different files differ");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstants();
        testFileRedirects();
        testValueSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
