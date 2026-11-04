/*
 * @test
 * @summary javax.management.remote.JMXPrincipal conformance: white-box structure + black-box behavior.
 * @run main JMXPrincipalTest
 */

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.security.Principal;

import javax.management.remote.JMXPrincipal;

public class JMXPrincipalTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXPrincipal");
        check(!c.isInterface(), "concrete class");
        check(Principal.class.isAssignableFrom(c), "implements Principal");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        Constructor<?> k = c.getConstructor(String.class);
        check(k != null, "(String) ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JMXPrincipal p = new JMXPrincipal("user");
        check(p.getName().equals("user"), "name");
        check(p.toString().equals("JMXPrincipal:  user"), "toString: " + p.toString());

        check(p.equals(new JMXPrincipal("user")), "equals same name");
        check(p.hashCode() == new JMXPrincipal("user").hashCode(), "hashCode consistent");
        check(!p.equals(new JMXPrincipal("other")), "different name not equal");
        check(!p.equals(new JMXPrincipal("User")), "equals case-sensitive");
        check(!p.equals("user"), "not equal to String");
        check(!p.equals(null), "not equal to null");
        check(p.equals(p), "reflexive");

        JMXPrincipal empty = new JMXPrincipal("");
        check(empty.getName().equals(""), "empty name allowed");
        check(empty.hashCode() == "".hashCode(), "empty name hashCode");

        try {
            new JMXPrincipal(null);
            check(false, "expected NPE for null name");
        } catch (NullPointerException e) {
            check(e.getMessage().equals("illegal null input"), "null name message: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
