/*
 * @test
 * @summary java.net.ContentHandlerFactory conformance: white-box structure + black-box behavior.
 * @run main ContentHandlerFactoryTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;

public class ContentHandlerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ContentHandlerFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(c.getMethod("createContentHandler", String.class) != null, "createContentHandler");
    }

    static void testBlackBox() throws Exception {
        final ContentHandler handler = new ContentHandler() {
            public Object getContent(URLConnection urlc) { return "x"; }
        };
        ContentHandlerFactory factory = new ContentHandlerFactory() {
            public ContentHandler createContentHandler(String mimetype) { return handler; }
        };
        check(factory.createContentHandler("text/plain") == handler, "createContentHandler returns handler");
        check(factory.createContentHandler("text/html") == handler, "same handler for any mime");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
