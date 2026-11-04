/*
 * @test
 * @summary javax.net.ssl.SSLException conformance: white-box structure + black-box behavior.
 * @run main SSLExceptionTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class SSLExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Class.forName("javax.net.ssl.SSLException").getSuperclass() == Class.forName("java.io.IOException"), "superclass java.io.IOException");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.SSLException e1 = new javax.net.ssl.SSLException("msg");
        check(e1.getMessage().equals("msg"), "message");
        check(e1 instanceof java.io.IOException, "is IOException");
        Throwable cause = new RuntimeException("cause");
        javax.net.ssl.SSLException e2 = new javax.net.ssl.SSLException("m2", cause);
        check(e2.getCause() == cause && e2.getMessage().equals("m2"), "message + cause");
        javax.net.ssl.SSLException e3 = new javax.net.ssl.SSLException(cause);
        check(e3.getCause() == cause, "cause ctor");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
