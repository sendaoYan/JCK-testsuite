/*
 * @test
 * @summary javax.net.ssl.SSLHandshakeException conformance: constructor chain + hierarchy.
 * @run main SSLHandshakeExceptionTest
 */
import java.io.*;
import java.lang.reflect.*;
import javax.net.ssl.*;
public class SSLHandshakeExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLHandshakeException");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("javax.net.ssl.SSLException"), "extends SSLException");
        int pubCtors = 0;
        for (Constructor<?> k : c.getDeclaredConstructors()) if (Modifier.isPublic(k.getModifiers())) pubCtors++;
        check(pubCtors == 1, "single public constructor (String); the (String,Throwable)"
                + " overload exists only on JDK 21+");

        SSLHandshakeException e = new SSLHandshakeException("handshake failed");
        check("handshake failed".equals(e.getMessage()), "message kept");
        check(e instanceof SSLException && e instanceof IOException, "SSLException and IOException hierarchy");
        check(e instanceof java.io.Serializable, "Serializable");
        check(new SSLHandshakeException(null).getMessage() == null, "null message preserved");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
