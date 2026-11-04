/*
 * @test
 * @summary javax.naming.directory.InvalidAttributeIdentifierException conformance: white-box structure + black-box behavior.
 * @run main InvalidAttributeIdentifierExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

public class InvalidAttributeIdentifierExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.InvalidAttributeIdentifierException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == NamingException.class, "extends NamingException");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 0), "no-arg ctor");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == String.class), "ctor(String)");
    }

    static void testBlackBox() {
        InvalidAttributeIdentifierException e = new InvalidAttributeIdentifierException("bad id");
        check("bad id".equals(e.getMessage()), "getMessage");
        check("bad id".equals(e.getExplanation()), "getExplanation");
        check(e instanceof NamingException, "instanceof NamingException");
        check(e.getResolvedName() == null, "resolvedName null");
        check(e.getRemainingName() == null, "remainingName null");
        InvalidAttributeIdentifierException e2 = new InvalidAttributeIdentifierException();
        check(e2.getMessage() == null, "null message");
        check(e2.getExplanation() == null, "null explanation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
