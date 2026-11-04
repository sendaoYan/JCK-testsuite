/*
 * @test
 * @summary java.net.URLStreamHandlerFactory conformance: white-box structure + black-box
 *          behavior. Deep contract coverage: createURLStreamHandler recording contract,
 *          URL.setURLStreamHandlerFactory set-once semantics (second -> Error "factory
 *          already defined"), custom-protocol URLs built through a registered factory
 *          (default parseURL decomposition, default-port override, toExternalForm,
 *          equals/hashCode with literal-IP hosts, relative context resolution), unknown
 *          scheme -> MalformedURLException, and built-in protocols still working after
 *          factory installation.
          (Java 8 source level.)
 * @run main URLStreamHandlerFactoryTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class URLStreamHandlerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ------------------------------------------------------------------
    // original structure checks (kept from previous version)
    // ------------------------------------------------------------------
    static void testOriginalStructure() throws Exception {
        Class<?> c = Class.forName("java.net.URLStreamHandlerFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createURLStreamHandler"), "has method createURLStreamHandler");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URLStreamHandlerFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "no superclass (interface)");
        Method m = c.getMethod("createURLStreamHandler", String.class);
        check(m != null, "createURLStreamHandler");
        check(Modifier.isAbstract(m.getModifiers()), "createURLStreamHandler abstract");
        check(m.getReturnType() == URLStreamHandler.class, "returns URLStreamHandler");
        check(m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == String.class,
                "takes a single String protocol");
        check(m.getExceptionTypes().length == 0, "throws no checked exceptions");
        check(c.getMethods().length == 1, "exactly one interface method");
        check(!c.isAnnotationPresent(FunctionalInterface.class),
                "not annotated @FunctionalInterface (works as one anyway)");
        // usable as a lambda target
        URLStreamHandlerFactory lf = proto -> null;
        check(lf.createURLStreamHandler("http") == null, "lambda returns null handler");
        URLStreamHandler h = new URLStreamHandler() {
            protected URLConnection openConnection(URL u) { return null; }
        };
        URLStreamHandlerFactory lf2 = proto -> "x".equals(proto) ? h : null;
        check(lf2.createURLStreamHandler("x") == h, "lambda maps protocol to handler");
        check(lf2.createURLStreamHandler("y") == null, "lambda maps unknown to null");
    }

    /** Recording factory (not registered): per-protocol mapping, null for unknown. */
    static class Recording implements URLStreamHandlerFactory {
        List<String> protocols = new ArrayList<String>();
        URLStreamHandler handler = new URLStreamHandler() {
            protected URLConnection openConnection(URL u) { return null; }
        };
        public URLStreamHandler createURLStreamHandler(String protocol) {
            protocols.add(protocol);
            return "myproto".equals(protocol) ? handler : null;
        }
    }

    static void testRecordingFactory() {
        Recording f = new Recording();
        check(f.createURLStreamHandler("myproto") == f.handler, "registered protocol handler");
        check(f.createURLStreamHandler("other") == null, "unknown protocol -> null");
        check(f.createURLStreamHandler("") == null, "empty protocol -> null");
        check(f.createURLStreamHandler(null) == null, "null protocol accepted (recorded)");
        check(f.protocols.size() == 4, "all invocations recorded");
        check("myproto".equals(f.protocols.get(0)), "protocol order 0");
        check(f.protocols.get(3) == null, "null protocol recorded verbatim");
        // handler itself is a functional URLStreamHandler
        check(f.handler != null, "handler non-null");
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

    // ------------------------------------------------------------------
    // JVM-global registration: set once, custom protocols, second set -> Error
    // ------------------------------------------------------------------
    static void testRegisterAndUse() throws Exception {
        final List<String> seen = new ArrayList<String>();
        // first installation succeeds
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
            public URLStreamHandler createURLStreamHandler(String protocol) {
                seen.add(protocol);
                if ("chfproto".equals(protocol)) {
                    return new URLStreamHandler() {
                        protected URLConnection openConnection(URL u) { return null; }
                        protected int getDefaultPort() { return 99; }
                    };
                }
                return null; // fall back to built-in handlers
            }
        });
        // custom protocol URLs are parsed by the factory-provided handler
        URL u = new URL("chfproto://user@127.0.0.1:1234/p/q?x=1#f");
        check("chfproto".equals(u.getProtocol()), "custom protocol");
        check("127.0.0.1".equals(u.getHost()), "custom host");
        check(u.getPort() == 1234, "custom port");
        check("user".equals(u.getUserInfo()), "custom userInfo");
        check("user@127.0.0.1:1234".equals(u.getAuthority()), "custom authority");
        check("/p/q?x=1".equals(u.getFile()), "custom file");
        check("x=1".equals(u.getQuery()), "custom query");
        check("f".equals(u.getRef()), "custom ref");
        check(u.getDefaultPort() == 99, "custom default port override honored");
        check("chfproto://user@127.0.0.1:1234/p/q?x=1#f".equals(u.toExternalForm()),
                "custom toExternalForm");
        check(u.openConnection() == null, "openConnection delegates to factory handler");
        // relative resolution through the custom handler
        URL rel = new URL(u, "rel?y=2");
        check("/p/rel".equals(rel.getPath()), "relative path resolved via custom handler");
        check("y=2".equals(rel.getQuery()), "relative query");
        // equals/hashCode with literal-IP hosts (no DNS)
        URL a = new URL("chfproto://127.0.0.1:5/x");
        URL b = new URL("chfproto://127.0.0.1:5/x");
        check(a.equals(b), "literal IP equals");
        check(a.hashCode() == b.hashCode(), "literal IP hashCode");
        check(!a.equals(new URL("chfproto://127.0.0.1:6/x")), "different port not equal");
        // the factory was consulted for the custom protocol
        check(seen.contains("chfproto"), "factory saw chfproto, got " + seen);
        // unknown scheme -> MalformedURLException (probed message on both JDKs)
        try {
            new URL("nosuchproto://x/");
            check(false, "unknown scheme should throw MalformedURLException");
        } catch (MalformedURLException mue) {
            check(mue.getMessage() != null && mue.getMessage().contains("unknown protocol"),
                    "unknown protocol message, got " + mue.getMessage());
        }
        // built-in protocols still work through the null-return fallback
        URL file = new File("/tmp/some-file.txt").toURI().toURL();
        check("file".equals(file.getProtocol()), "file: still built-in after factory");
        URL http = new URL("http://127.0.0.1:1/");
        check("http".equals(http.getProtocol()), "http: still built-in after factory");
        check(http.getDefaultPort() == 80, "http default port from built-in handler");
        // second installation fails with Error
        try {
            URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
                public URLStreamHandler createURLStreamHandler(String p) { return null; }
            });
            check(false, "second setURLStreamHandlerFactory should throw Error");
        } catch (Error e) {
            check("factory already defined".equals(e.getMessage()),
                    "factory already defined message, got " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testOriginalStructure();
        testWhiteBox();
        testRecordingFactory();
        testBlackBox();
        testRegisterAndUse(); // last: poisons the JVM-global factory
        System.out.println("PASS (" + passed + " assertions)");
    }
}
