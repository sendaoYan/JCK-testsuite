/*
 * @test
 * @summary javax.net.ssl.SSLKeyException conformance: constructor chain + hierarchy.
 * @run main SSLKeyExceptionTest
 */
import java.io.*;
import java.lang.reflect.*;
import javax.net.ssl.*;
public class SSLKeyExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLKeyException");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("javax.net.ssl.SSLException"), "extends SSLException");
        int pubCtors = 0;
        for (Constructor<?> k : c.getDeclaredConstructors()) if (Modifier.isPublic(k.getModifiers())) pubCtors++;
        check(pubCtors == 1, "single public constructor (String); the (String,Throwable)"
                + " overload exists only on JDK 21+");

        SSLKeyException e = new SSLKeyException("bad key");
        check("bad key".equals(e.getMessage()), "message kept");
        check(e instanceof SSLException && e instanceof IOException, "SSLException and IOException hierarchy");
        check(e instanceof java.io.Serializable, "Serializable");
        check(new SSLKeyException(null).getMessage() == null, "null message preserved");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
