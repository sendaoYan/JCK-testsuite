/*
 * @test
 * @summary javax.naming.MalformedLinkException conformance: white-box structure + black-box behavior.
 * @run main MalformedLinkExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.LinkException;
import javax.naming.MalformedLinkException;

public class MalformedLinkExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = MalformedLinkException.class;
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
        MalformedLinkException mle = new MalformedLinkException("invalid link name");
        check(mle.getMessage().equals("invalid link name"), "message");
        try { throw mle; } catch (LinkException le) { check(le.getMessage().equals("invalid link name"), "caught as LinkException"); }
        mle.setLinkRemainingName(new CompositeName("bad/link"));
        check(mle.toString().contains("bad/link"), "link context in toString");
        check(new MalformedLinkException().getMessage() == null, "no-arg message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
