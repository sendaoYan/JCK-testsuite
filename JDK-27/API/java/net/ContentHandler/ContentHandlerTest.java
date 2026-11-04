/*
 * @test
 * @summary java.net.ContentHandler conformance: white-box structure + black-box behavior.
 * @run main ContentHandlerTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;

public class ContentHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ContentHandler");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getMethod("getContent", URLConnection.class) != null, "getContent(URLConnection)");
        check(c.getMethod("getContent", URLConnection.class, Class[].class) != null, "getContent(URLConnection,Class[])");
    }

    static void testBlackBox() throws Exception {
        ContentHandler ch = new ContentHandler() {
            public Object getContent(URLConnection urlc) { return "text-content"; }
        };
        Object o = ch.getContent(null);
        check("text-content".equals(o), "getContent returns value");

        // two-arg form matches classes and falls back to null
        Object matched = ch.getContent(null, new Class[]{String.class});
        check("text-content".equals(matched), "two-arg matches String");
        Object unmatched = ch.getContent(null, new Class[]{Integer.class});
        check(unmatched == null, "two-arg returns null when no class matches");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
