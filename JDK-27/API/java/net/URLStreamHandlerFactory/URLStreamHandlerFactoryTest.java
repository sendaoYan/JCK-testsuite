/*
 * @test
 * @summary java.net.URLStreamHandlerFactory conformance: white-box structure + black-box behavior.
 * @run main URLStreamHandlerFactoryTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;

public class URLStreamHandlerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URLStreamHandlerFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(c.getMethod("createURLStreamHandler", String.class) != null, "createURLStreamHandler");
    }

    static void testBlackBox() throws Exception {
        final URLStreamHandler handler = new URLStreamHandler() {
            protected URLConnection openConnection(URL u) { return null; }
        };
        URLStreamHandlerFactory factory = new URLStreamHandlerFactory() {
            public URLStreamHandler createURLStreamHandler(String protocol) { return handler; }
        };
        check(factory.createURLStreamHandler("http") == handler, "createURLStreamHandler returns handler");
        check(factory.createURLStreamHandler("ftp") == handler, "same handler for any protocol");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
