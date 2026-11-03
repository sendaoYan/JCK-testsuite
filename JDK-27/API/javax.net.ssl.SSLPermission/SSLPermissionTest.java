/*
 * @test
 * @summary javax.net.ssl.SSLPermission conformance: white-box structure + black-box behavior.
 * @run main SSLPermissionTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
@SuppressWarnings("removal")
public class SSLPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLPermission");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(Class.forName("javax.net.ssl.SSLPermission").getSuperclass() == Class.forName("java.security.BasicPermission"), "superclass java.security.BasicPermission");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.SSLPermission p = new javax.net.ssl.SSLPermission("setHostnameVerifier");
        check(p.getName().equals("setHostnameVerifier"), "name");
        check(p.implies(new javax.net.ssl.SSLPermission("setHostnameVerifier")), "implies self");
        check(!p.implies(new javax.net.ssl.SSLPermission("getSSLSessionContext")), "not implies other");
        check(new javax.net.ssl.SSLPermission("*").implies(p), "wildcard implies");
        check(p.getActions().isEmpty(), "empty actions");
        javax.net.ssl.SSLPermission p2 = new javax.net.ssl.SSLPermission("x", "read");
        check(p2.getActions().isEmpty(), "actions ignored");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
