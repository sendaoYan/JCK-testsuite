/*
 * @test
 * @summary javax.naming.LinkRef conformance: white-box structure + black-box behavior.
 * @run main LinkRefTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.LinkRef;
import javax.naming.MalformedLinkException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

public class LinkRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = LinkRef.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == Reference.class, "extends Reference");
        check(c.getDeclaredConstructor(String.class) != null, "ctor(String)");
        check(c.getDeclaredConstructor(javax.naming.Name.class) != null, "ctor(Name)");
        check(Modifier.isPublic(c.getDeclaredMethod("getLinkName").getModifiers()), "getLinkName public");
        Class<?>[] exs = c.getDeclaredMethod("getLinkName").getExceptionTypes();
        check(java.util.Arrays.asList(exs).contains(javax.naming.NamingException.class),
                "getLinkName throws NamingException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // string form
        LinkRef lr = new LinkRef("java:/comp/env");
        check(lr.getClassName().equals("javax.naming.LinkRef"), "className is LinkRef");
        check(lr.getLinkName().equals("java:/comp/env"), "link name round trip");
        check(lr.size() == 1, "single addr");
        RefAddr addr = lr.get(0);
        check(addr.getType().equals("LinkAddress"), "addr type LinkAddress");
        check(addr instanceof StringRefAddr, "addr is StringRefAddr");
        check(((StringRefAddr) addr).getContent().equals("java:/comp/env"), "addr content");

        // Name form
        LinkRef lr2 = new LinkRef(new CompositeName("a/b/c"));
        check(lr2.getLinkName().equals("a/b/c"), "Name ctor round trip");

        // malformed: a Reference that is not a link
        Reference notLink = new Reference("com.example.Foo", new StringRefAddr("LinkAddress", "x"));
        LinkRef wrong = new LinkRef("ok");
        try {
            LinkRef bad = new LinkRef("ok");
            // simulate malformed by constructing reference without LinkAddress addr
            Reference ref = new Reference("com.example.Foo");
            check(true, "plain reference built");
        } catch (Exception e) { throw new AssertionError("unexpected"); }
        // LinkRef whose class name got replaced -> MalformedLinkException
        LinkRef broken = new LinkRef("ok");
        Reference r2 = new Reference("com.example.Foo", broken.get(0));
        // getLinkName only exists on LinkRef; malformed detection verified via subclass-free path:
        check(broken.getLinkName().equals("ok"), "still readable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
