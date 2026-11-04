/*
 * @test
 * @summary javax.naming.AuthenticationException conformance: white-box structure + black-box behavior.
 * @run main AuthenticationExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.AuthenticationException;

public class AuthenticationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = javax.naming.AuthenticationException.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == javax.naming.NamingSecurityException.class, "extends NamingSecurityException");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        Constructor<?>[] cs = c.getDeclaredConstructors();
        boolean noArg = false, strArg = false;
        for (Constructor<?> k : cs) {
            if (k.getParameterCount() == 0) noArg = true;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) strArg = true;
        }
        check(noArg, "has no-arg ctor");
        check(strArg, "has String ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AuthenticationException withMsg = new AuthenticationException("reason");
        check(withMsg.getMessage().equals("reason"), "message preserved");
        check(withMsg.getExplanation().equals("reason"), "explanation == message");
        AuthenticationException noMsg = new AuthenticationException();
        check(noMsg.getMessage() == null, "no-arg message null");

        // inherited NamingException plumbing used by every subclass
        withMsg.setRemainingName(new CompositeName("a/b"));
        check(withMsg.getRemainingName().toString().equals("a/b"), "remaining name round trip");
        withMsg.setResolvedName(new CompositeName("r"));
        check(withMsg.getResolvedName().size() == 1, "resolved name");
        withMsg.setResolvedObj("obj");
        check("obj".equals(withMsg.getResolvedObj()), "resolved obj");
        String ts = withMsg.toString();
        check(ts.contains("javax.naming.AuthenticationException") && ts.contains("reason"), "toString naming");
        check(withMsg instanceof NamingException, "catchable as NamingException");
        check(withMsg instanceof javax.naming.NamingSecurityException, "catchable as NamingSecurityException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
