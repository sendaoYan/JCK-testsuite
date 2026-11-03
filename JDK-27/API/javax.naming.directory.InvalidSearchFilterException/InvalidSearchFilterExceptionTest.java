/*
 * @test
 * @summary javax.naming.directory.InvalidSearchFilterException conformance: white-box structure + black-box behavior.
 * @run main InvalidSearchFilterExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

public class InvalidSearchFilterExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.InvalidSearchFilterException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == NamingException.class, "extends NamingException");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 0), "no-arg ctor");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == String.class), "ctor(String)");
    }

    static void testBlackBox() {
        InvalidSearchFilterException e = new InvalidSearchFilterException("bad filter");
        check("bad filter".equals(e.getMessage()), "getMessage");
        check("bad filter".equals(e.getExplanation()), "getExplanation");
        check(e instanceof NamingException, "instanceof NamingException");
        check(e.getResolvedName() == null, "resolvedName null");
        check(e.getResolvedObj() == null, "resolvedObj null");
        check(e.getRemainingName() == null, "remainingName null");
        InvalidSearchFilterException e2 = new InvalidSearchFilterException();
        check(e2.getMessage() == null, "null message");
        check(e2.getExplanation() == null, "null explanation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
