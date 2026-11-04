/*
 * @test
 * @summary javax.naming.LinkLoopException conformance: white-box structure + black-box behavior.
 * @run main LinkLoopExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.LinkException;
import javax.naming.LinkLoopException;
import javax.naming.NamingException;

public class LinkLoopExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = LinkLoopException.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == LinkException.class, "extends LinkException");
        Constructor<?>[] cs = c.getDeclaredConstructors();
        boolean noArg = false, strArg = false;
        for (Constructor<?> k : cs) {
            if (k.getParameterCount() == 0) noArg = true;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) strArg = true;
        }
        check(noArg && strArg, "both ctors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        LinkLoopException lle = new LinkLoopException("detected loop");
        check(lle.getMessage().equals("detected loop"), "message");
        // provider fills the link trail it walked before detecting the cycle
        lle.setLinkResolvedName(new CompositeName("linkA"));
        lle.setLinkRemainingName(new CompositeName("linkB/linkA"));
        String s = lle.toString();
        check(s.contains("LinkLoopException: detected loop"), "header");
        check(s.contains("Link Remaining Name: 'linkB/linkA'"), "trail visible");
        int caught = 0;
        try { throw lle; } catch (LinkException le) { caught++; }
        try { throw new LinkLoopException(); } catch (NamingException ne) { caught++; }
        check(caught == 2, "catchable as LinkException and NamingException");
        check(new LinkLoopException().getMessage() == null, "no-arg message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
