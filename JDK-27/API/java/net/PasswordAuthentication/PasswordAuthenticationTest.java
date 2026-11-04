/*
 * @test
 * @summary java.net.PasswordAuthentication conformance: white-box structure + black-box behavior.
 * @run main PasswordAuthenticationTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class PasswordAuthenticationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.PasswordAuthentication");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getConstructor(String.class, char[].class) != null, "ctor(String,char[])");
        check(c.getMethod("getUserName") != null, "getUserName");
        check(c.getMethod("getPassword") != null, "getPassword");
    }

    static void testBlackBox() throws Exception {
        char[] pw = { 's', 'e', 'c', 'r', 'e', 't' };
        PasswordAuthentication pa = new PasswordAuthentication("user", pw);
        check(pa.getUserName().equals("user"), "userName");
        check(Arrays.equals(pa.getPassword(), "secret".toCharArray()), "password content");

        // constructor clones the caller's array (documented)
        pw[0] = 'X';
        check(Arrays.equals(pa.getPassword(), "secret".toCharArray()), "constructor clones input array");

        // getPassword returns the internal array reference (documented)
        check(pa.getPassword() == pa.getPassword(), "getPassword returns internal reference");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
