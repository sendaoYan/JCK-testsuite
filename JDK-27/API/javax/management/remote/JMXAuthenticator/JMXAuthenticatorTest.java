/*
 * @test
 * @summary javax.management.remote.JMXAuthenticator conformance: white-box structure + black-box behavior.
 * @run main JMXAuthenticatorTest
 */

import java.lang.reflect.Method;

import javax.management.remote.JMXAuthenticator;
import javax.security.auth.Subject;

public class JMXAuthenticatorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(Method[] mm, String n) {
        for (Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXAuthenticator");
        check(c.isInterface(), "is interface");
        Method m = c.getMethod("authenticate", Object.class);
        check(m.getReturnType() == Subject.class, "authenticate returns Subject");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final Subject subject = new Subject();
        JMXAuthenticator auth = new JMXAuthenticator() {
            public Subject authenticate(Object credentials) { return subject; }
        };
        check(auth.authenticate("password") == subject, "anonymous authenticate callable");
        check(auth.authenticate(null) == subject, "authenticate accepts null credentials");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
