/*
 * @test
 * @summary java.net.spi.URLStreamHandlerProvider conformance: white-box structure + black-box anonymous impl.
 * @run main URLStreamHandlerProviderTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.net.spi.URLStreamHandlerProvider;

public class URLStreamHandlerProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.spi.URLStreamHandlerProvider");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(URLStreamHandlerFactory.class.isAssignableFrom(c), "implements URLStreamHandlerFactory");
        check(c.getMethod("createURLStreamHandler", String.class) != null, "createURLStreamHandler present");

        Constructor<?> ctor = c.getDeclaredConstructor();
        check(ctor != null, "no-arg constructor present");
        check(Modifier.isProtected(ctor.getModifiers()), "constructor is protected");
    }

    static void testBlackBox() {
        URLStreamHandlerProvider provider = new URLStreamHandlerProvider() {
            public URLStreamHandler createURLStreamHandler(String protocol) {
                if ("dummy".equals(protocol)) return new URLStreamHandler() {
                    protected java.net.URLConnection openConnection(java.net.URL u) { return null; }
                };
                return null;
            }
        };

        check(provider instanceof URLStreamHandlerFactory, "is a URLStreamHandlerFactory");
        check(provider.createURLStreamHandler("unknown") == null, "null handler for unknown protocol");
        check(provider.createURLStreamHandler("dummy") != null, "non-null handler for known protocol");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
