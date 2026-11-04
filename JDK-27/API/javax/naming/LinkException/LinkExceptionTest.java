/*
 * @test
 * @summary javax.naming.LinkException conformance: white-box structure + black-box behavior.
 * @run main LinkExceptionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.LinkException;
import javax.naming.NamingException;

public class LinkExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = LinkException.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == NamingException.class, "extends NamingException");
        for (String m : new String[] {"getLinkResolvedName", "setLinkResolvedName", "getLinkRemainingName",
                "setLinkRemainingName", "getLinkResolvedObj", "setLinkResolvedObj"})
            check(method(c, m), "declares " + m);
        check(c.getMethod("toString", boolean.class) != null, "toString(boolean)");
        check(javax.naming.LinkLoopException.class.getSuperclass() == c, "LinkLoopException subclass");
        check(javax.naming.MalformedLinkException.class.getSuperclass() == c, "MalformedLinkException subclass");
    }
    static boolean method(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        LinkException le = new LinkException("lex");
        check(le.getMessage().equals("lex"), "message");

        le.setLinkResolvedName(new CompositeName("lr"));
        le.setLinkRemainingName(new CompositeName("lm"));
        le.setLinkResolvedObj("lo");
        check(le.getLinkResolvedName().get(0).equals("lr"), "link resolved name");
        check(le.getLinkRemainingName().get(0).equals("lm"), "link remaining name");
        check("lo".equals(le.getLinkResolvedObj()), "link resolved obj");

        // toString exposes link parts alongside the base message
        String s = le.toString();
        check(s.contains("javax.naming.LinkException: lex"), "header");
        check(s.contains("Link Remaining Name: 'lm'"), "link remaining in toString");

        // independent from the non-link resolved/remaining state
        le.setRemainingName(new CompositeName("m"));
        check(!le.getRemainingName().equals(le.getLinkRemainingName()), "two tracks independent");
        check(le instanceof NamingException, "NamingException hierarchy");

        // setLinkRemainingName clones the argument
        CompositeName arg = new CompositeName("x/y");
        le.setLinkRemainingName(arg);
        check(le.getLinkRemainingName() != arg, "clone on set");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
