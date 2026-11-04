/*
 * @test
 * @summary java.net.ContentHandlerFactory conformance: white-box structure + black-box
 *          behavior. Deep contract coverage: createContentHandler recording contract
 *          (mime order, per-mime mapping, null for unknown, null argument recorded),
 *          URLConnection.setContentHandlerFactory set-once semantics (second installation
 *          -> Error "factory already defined"), and full integration where a registered
 *          factory routes URLConnection.getContent() for a file: URL.
 * @run main ContentHandlerFactoryTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class ContentHandlerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ContentHandlerFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "no superclass (interface)");
        Method m = c.getMethod("createContentHandler", String.class);
        check(m != null, "createContentHandler");
        check(Modifier.isAbstract(m.getModifiers()), "createContentHandler abstract");
        check(m.getReturnType() == ContentHandler.class, "createContentHandler returns ContentHandler");
        check(m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == String.class,
                "createContentHandler takes a single String");
        check(m.getExceptionTypes().length == 0, "createContentHandler throws no checked exceptions");
        check(c.getMethods().length == 1, "exactly one interface method");
        check(!c.isAnnotationPresent(FunctionalInterface.class),
                "not annotated @FunctionalInterface (works as one anyway)");
        // usable as a lambda target
        ContentHandlerFactory lf = mime -> null;
        check(lf.createContentHandler("text/plain") == null, "lambda implementation returns null");
        check(lf.createContentHandler(null) == null, "lambda accepts null mime");
        ContentHandler h = new ContentHandler() {
            public Object getContent(URLConnection c) { return null; }
        };
        ContentHandlerFactory lf2 = mime -> "text/plain".equals(mime) ? h : null;
        check(lf2.createContentHandler("text/plain") == h, "lambda maps mime to handler");
        check(lf2.createContentHandler("other") == null, "lambda maps other to null");
    }

    /** Recording factory: per-mime mapping, null for unknown, records every call. */
    static class Recording implements ContentHandlerFactory {
        List<String> mimes = new ArrayList<String>();
        ContentHandler plainHandler = new ContentHandler() {
            public Object getContent(URLConnection c) { return "plain"; }
        };
        ContentHandler htmlHandler = new ContentHandler() {
            public Object getContent(URLConnection c) { return "html"; }
        };
        public ContentHandler createContentHandler(String mime) {
            mimes.add(mime);
            if ("text/plain".equals(mime)) return plainHandler;
            if ("text/html".equals(mime)) return htmlHandler;
            return null;
        }
    }

    static void testRecordingFactory() throws Exception {
        Recording f = new Recording();
        check(f.createContentHandler("text/plain") == f.plainHandler, "plain handler per mime");
        check(f.createContentHandler("text/html") == f.htmlHandler, "html handler per mime");
        check(f.createContentHandler("application/unknown") == null, "unknown mime -> null");
        check(f.createContentHandler("") == null, "empty mime -> null");
        check(f.createContentHandler(null) == null, "null mime accepted by contract (recorded)");
        List<String> expected = Arrays.asList("text/plain", "text/html", "application/unknown", "", null);
        check(f.mimes.size() == 5, "all invocations recorded");
        check("text/plain".equals(f.mimes.get(0)), "mime order 0");
        check("text/html".equals(f.mimes.get(1)), "mime order 1");
        check(f.mimes.get(4) == null, "null mime recorded verbatim");
        // handlers themselves work
        check("plain".equals(f.plainHandler.getContent(null)), "plain handler getContent");
        check("html".equals(f.htmlHandler.getContent(null)), "html handler getContent");
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

    // ------------------------------------------------------------------
    // JVM-global registration: set once, second set -> Error (probed exact message)
    // ------------------------------------------------------------------
    static void testRegisterOnceAndError() throws Exception {
        final List<String> mimes = new ArrayList<String>();
        final Object sentinel = "CHF-SENTINEL";
        // first installation succeeds
        URLConnection.setContentHandlerFactory(new ContentHandlerFactory() {
            public ContentHandler createContentHandler(String mime) {
                mimes.add(mime);
                return new ContentHandler() {
                    public Object getContent(URLConnection c) { return sentinel; }
                };
            }
        });
        // integration: getContent() on a file: URL is routed through the factory
        File dir = new File("/tmp/net-c-ContentHandlerFactory");
        dir.mkdirs();
        try {
            File f = new File(dir, "x.txt");
            Writer w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
            w.write("hello");
            w.close();
            Object got = f.toURI().toURL().openConnection().getContent();
            check(got == sentinel, "getContent routed through registered factory");
            check(mimes.contains("text/plain"), "factory saw text/plain, got " + mimes);
            check(mimes.size() == 1, "exactly one lookup per getContent");
        } finally {
            for (File x : dir.listFiles()) x.delete();
            dir.delete();
        }
        // second installation fails with Error
        try {
            URLConnection.setContentHandlerFactory(new ContentHandlerFactory() {
                public ContentHandler createContentHandler(String m) { return null; }
            });
            check(false, "second setContentHandlerFactory should throw Error");
        } catch (Error e) {
            check("factory already defined".equals(e.getMessage()),
                    "factory already defined message, got " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testRecordingFactory();
        testBlackBox();
        testRegisterOnceAndError(); // last: poisons the JVM-global factory
        System.out.println("PASS (" + passed + " assertions)");
    }
}
