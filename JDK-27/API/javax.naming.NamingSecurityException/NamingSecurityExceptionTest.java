/*
 * @test
 * @summary javax.naming.NamingSecurityException conformance: white-box structure + black-box behavior.
 * @run main NamingSecurityExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import javax.naming.NamingException;
import javax.naming.NamingSecurityException;

public class NamingSecurityExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = NamingSecurityException.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == NamingException.class, "extends NamingException");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        Constructor<?>[] cs = c.getDeclaredConstructors();
        boolean noArg = false, strArg = false;
        for (Constructor<?> k : cs) {
            if (k.getParameterCount() == 0) noArg = true;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) strArg = true;
        }
        check(noArg && strArg, "both ctors");
        // concrete security subclasses in the same package
        check(javax.naming.AuthenticationException.class.getSuperclass() == c, "AuthenticationException subclass");
        check(javax.naming.AuthenticationNotSupportedException.class.getSuperclass() == c, "AuthNotSupportedException subclass");
        check(javax.naming.NoPermissionException.class.getSuperclass() == c, "NoPermissionException subclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Modifier.isAbstract(NamingSecurityException.class.getModifiers()), "abstract confirmed");

        // direct constructor invocation through minimal concrete subclasses
        javax.naming.NamingSecurityException e1 = new javax.naming.NamingSecurityException() {};
        check(e1.getMessage() == null, "no-arg constructor leaves message null");
        javax.naming.NamingSecurityException e2 = new javax.naming.NamingSecurityException("sec-msg") {};
        check("sec-msg".equals(e2.getMessage()), "(String) constructor preserves message");

        // polymorphic catch bucket for the security family
        javax.naming.AuthenticationException a = new javax.naming.AuthenticationException("auth");
        javax.naming.AuthenticationNotSupportedException b = new javax.naming.AuthenticationNotSupportedException("scheme");
        javax.naming.NoPermissionException p = new javax.naming.NoPermissionException("perm");
        int caught = 0;
        for (NamingException x : new NamingException[] {a, b, p}) {
            try { throw x; } catch (NamingSecurityException sec) { caught++; }
        }
        check(caught == 3, "all security subclasses caught as NamingSecurityException");
        check(!(new NamingException("x") instanceof NamingSecurityException), "plain NamingException not security");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
