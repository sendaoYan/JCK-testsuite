/*
 * @test
 * @summary javax.net.ssl.SSLProtocolException conformance: white-box structure + black-box behavior.
 * @run main SSLProtocolExceptionTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class SSLProtocolExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLProtocolException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Class.forName("javax.net.ssl.SSLProtocolException").getSuperclass() == Class.forName("javax.net.ssl.SSLException"), "superclass javax.net.ssl.SSLException");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.SSLProtocolException e1 = new javax.net.ssl.SSLProtocolException("msg");
        check(e1.getMessage().equals("msg"), "message");
        check(e1 instanceof javax.net.ssl.SSLException, "is SSLException");
        check(e1 instanceof java.io.IOException, "is IOException");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
