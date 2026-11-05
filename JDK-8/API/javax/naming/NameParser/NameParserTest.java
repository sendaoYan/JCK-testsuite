/*
 * @test
 * @summary javax.naming.NameParser conformance: interface hierarchy and members.
 * @run main NameParserTest
 */



import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;
public class NameParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.NameParser");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("parse"), "has method parse");        testDeepContracts();
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
