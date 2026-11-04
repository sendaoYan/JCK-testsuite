/*
 * @test
 * @summary java.net.Authenticator conformance: white-box structure + black-box behavior.
 * @run main AuthenticatorTest
 */

import java.lang.reflect.*;
import java.net.*;

public class AuthenticatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.Authenticator");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getMethod("setDefault", Authenticator.class) != null, "setDefault");
        check(c.getMethod("getDefault") != null, "getDefault");
        check(c.getMethod("requestPasswordAuthentication", InetAddress.class, int.class, String.class, String.class, String.class) != null, "requestPasswordAuthentication");
    }

    static void testBlackBox() throws Exception {
        Authenticator prev = Authenticator.getDefault();
        try {
            final PasswordAuthentication expected = new PasswordAuthentication("user", new char[]{'p', 'w'});
            Authenticator a = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return expected;
                }
            };
            Authenticator.setDefault(a);
            check(Authenticator.getDefault() == a, "setDefault/getDefault roundtrip");

            PasswordAuthentication got = Authenticator.requestPasswordAuthentication(
                InetAddress.getLoopbackAddress(), 80, "http", "prompt", "basic");
            check(got == expected, "requestPasswordAuthentication invokes override");
            check(got.getUserName().equals("user"), "username");

            // instance method path
            PasswordAuthentication got2 = a.requestPasswordAuthenticationInstance(
                "host", InetAddress.getLoopbackAddress(), 80, "http", "prompt", "basic", null, null);
            check(got2 == expected, "requestPasswordAuthenticationInstance");
        } finally {
            Authenticator.setDefault(prev);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
