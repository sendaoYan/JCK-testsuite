/*
 * @test
 * @summary javax.naming.NameParser conformance: white-box structure + black-box behavior.
 * @run main NameParserTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;

public class NameParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** Minimal parser impl: splits on '.' into a CompositeName. */
    static class DotParser implements NameParser {
        int calls;
        public Name parse(String s) throws NamingException {
            calls++;
            Name n = new CompositeName();
            if (!s.isEmpty()) for (String part : s.split("\\.", -1)) n.add(part);
            return n;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = NameParser.class;
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        Method m = c.getMethod("parse", String.class);
        check(m.getReturnType() == Name.class, "parse returns Name");
        check(java.util.Arrays.asList(m.getExceptionTypes()).contains(NamingException.class),
                "parse declares NamingException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        DotParser dp = new DotParser();
        Name n = dp.parse("a.b.c");
        check(n.size() == 3, "parsed size");
        check(n.get(0).equals("a") && n.get(2).equals("c"), "parsed components");
        check(dp.calls == 1, "parse invoked");

        Name empty = dp.parse("");
        check(empty.isEmpty(), "empty input -> empty name");

        // parser contract surface: same input yields equal Names
        check(dp.parse("x.y").equals(dp.parse("x.y")), "deterministic parse");

        // a parser may throw NamingException subclass for bad syntax
        NameParser throwing = new NameParser() {
            public Name parse(String s) throws NamingException {
                throw new javax.naming.InvalidNameException("bad: " + s);
            }
        };
        try { throwing.parse("!"); throw new AssertionError("no throw"); }
        catch (javax.naming.InvalidNameException e) {
            check(e.getMessage().equals("bad: !"), "parser exception passthrough");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
