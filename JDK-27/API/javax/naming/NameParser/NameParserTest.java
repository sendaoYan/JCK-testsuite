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
import javax.naming.CompoundName;
import javax.naming.InvalidNameException;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dParserContract();
        System.out.println("deep: NameParser contracts OK");
    }

    static void dParserContract() throws Exception {
        NameParser parser = new NameParser() {
            public Name parse(String s) throws NamingException { return new CompositeName(s); }
        };
        Name n = parser.parse("a/b/c");
        check(n.size() == 3 && "a".equals(n.get(0)), "parse splits on composite syntax");
        check(parser.parse("").isEmpty(), "parse empty string -> empty name");
        check(parser.parse("x").size() == 1, "single component");
        Name rt = parser.parse("a\\/b");
        check(rt.size() == 1, "parse honors escapes");
        // compound-syntax parser
        java.util.Properties p = new java.util.Properties();
        p.setProperty("jndi.syntax.direction", "left_to_right");
        p.setProperty("jndi.syntax.separator", ".");
        p.setProperty("jndi.syntax.escape", "\\");
        final NameParser comp = new NameParser() {
            public Name parse(String s) throws NamingException { return new CompoundName(s, p); }
        };
        Name cn = comp.parse("x.y.z");
        check(cn.size() == 3 && "z".equals(cn.get(2)), "compound parse order");
        boolean ine = false;
        try { comp.parse("bad\\"); } catch (InvalidNameException e) { ine = true; }
        check(ine, "compound parse reports InvalidNameException for bad syntax");
        boolean npe = false;
        try { parser.parse(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "parse(null) -> NullPointerException");
        // round trip through toString
        Name orig = parser.parse("p/q");
        check(parser.parse(orig.toString()).equals(orig), "parse/toString round trip");
    }

}
