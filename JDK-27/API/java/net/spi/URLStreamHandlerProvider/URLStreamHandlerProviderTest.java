/*
 * @test
 * @summary java.net.spi.URLStreamHandlerProvider conformance: white-box structure + black-box anonymous impl.
 * @run main URLStreamHandlerProviderTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
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

    // ====================== deepening below ======================

    /** Recording provider: remembers every protocol string it was asked about. */
    static class RecordingProvider extends URLStreamHandlerProvider {
        final java.util.List<String> asked = new java.util.ArrayList<String>();

        public URLStreamHandler createURLStreamHandler(String protocol) {
            asked.add(protocol);
            if ("dummy".equals(protocol)) {
                return new URLStreamHandler() {
                    protected java.net.URLConnection openConnection(java.net.URL u) { return null; }
                };
            }
            return null;
        }
    }

    // ---- Part 1: white-box shape, full precision (probed) ----
    static void testWhiteBoxShape() throws Exception {
        Class<?> c = Class.forName("java.net.spi.URLStreamHandlerProvider");
        // Probed: the class declares NO methods of its own - the single factory method
        // comes entirely from the URLStreamHandlerFactory interface.
        check(c.getDeclaredMethods().length == 0,
                "declares no methods of its own (contract only via URLStreamHandlerFactory)");
        check(c.getDeclaredConstructors().length == 1, "exactly one declared constructor");
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(ctor.getParameterCount() == 0, "constructor takes no arguments");
        check("protected".equals(Modifier.toString(ctor.getModifiers()
                & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE))),
                "constructor modifier string is 'protected'");
        check(c.getInterfaces() != null && c.getInterfaces().length == 1
                && c.getInterfaces()[0] == URLStreamHandlerFactory.class,
                "directly implements exactly URLStreamHandlerFactory");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(!c.isSealed(), "not sealed");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check("java.net.spi".equals(c.getPackage().getName()), "package java.net.spi");
        check(c.getAnnotations().length == 0, "no annotations");

        // The interface method shape inherited by every provider.
        java.lang.reflect.Method m = URLStreamHandlerFactory.class.getMethod("createURLStreamHandler", String.class);
        check(Modifier.isAbstract(m.getModifiers()), "createURLStreamHandler abstract on the factory interface");
        check(m.getReturnType() == URLStreamHandler.class, "returns URLStreamHandler");
        check(URLStreamHandlerFactory.class.isInterface(), "URLStreamHandlerFactory is an interface");

        // Reflection instantiation is blocked (probed): protected + not exported.
        try {
            ctor.newInstance();
            check(false, "newInstance must not succeed");
        } catch (IllegalAccessException e) {
            check(true, "newInstance -> IllegalAccessException (protected ctor)");
        }
        try {
            ctor.setAccessible(true);
            check(false, "setAccessible must be refused");
        } catch (java.lang.reflect.InaccessibleObjectException e) {
            check(true, "setAccessible -> InaccessibleObjectException (java.net.spi not opened)");
        }
    }

    // ---- Part 2: provider as a factory object (no global registration yet) ----
    static void testFactoryObject() throws Exception {
        RecordingProvider provider = new RecordingProvider();

        check(provider.createURLStreamHandler("dummy") != null, "handler for dummy");
        check(provider.createURLStreamHandler("http") == null, "declines http");
        check(provider.createURLStreamHandler("https") == null, "declines https");
        check(provider.createURLStreamHandler("") == null, "declines empty protocol");
        check(provider.createURLStreamHandler(null) == null, "null protocol tolerated by impl, returns null");
        check(provider.asked.equals(java.util.Arrays.asList("dummy", "http", "https", "", null)),
                "protocol arguments recorded verbatim including null and empty");
        check(provider.asked.size() == 5, "five queries recorded");

        // Returned handler object identity and type.
        URLStreamHandler h1 = provider.createURLStreamHandler("dummy");
        URLStreamHandler h2 = provider.createURLStreamHandler("dummy");
        check(h1 != null && h2 != null, "both dummy handlers non-null");
        check(h1 != h2, "fresh handler instance per call (no provider-side caching contract)");
        check(h1 instanceof URLStreamHandler, "handler type");
        check(h1.hashCode() == h1.hashCode(), "handler hashCode stable");
        check(h1.toString() != null, "handler toString non-null");
        check(provider.asked.size() == 7, "counter reached seven queries");

        // Provider identity semantics.
        RecordingProvider other = new RecordingProvider();
        check(!provider.equals(other) && provider.equals(provider), "default equals semantics");
        check(provider.toString().contains("RecordingProvider"), "toString names subclass");

        // A handler produced by the provider can be fed directly into the 5-arg URL
        // constructor - pure parsing, no network (URLStreamHandler base implementation).
        URL u = new URL("dummy", "h2.example", 81, "/f2", h1);
        check("dummy".equals(u.getProtocol()), "5-arg URL protocol");
        check("h2.example".equals(u.getHost()), "5-arg URL host");
        check(u.getPort() == 81, "5-arg URL port");
        check("/f2".equals(u.getFile()), "5-arg URL file");
        check("h2.example:81".equals(u.getAuthority()), "5-arg URL authority");
        check("dummy://h2.example:81/f2".equals(u.toExternalForm()), "toExternalForm reconstruction");
        check(u.getDefaultPort() == -1, "default port unknown for custom protocol");
        URL uDefPort = new URL("dummy", "h3.example", -1, "/f3", h1);
        check(uDefPort.getPort() == -1, "port -1 stays unset");
        check("dummy://h3.example/f3".equals(uDefPort.toExternalForm()),
                "unset port omitted from external form");
    }

    // ---- Part 3: real integration via URL.setURLStreamHandlerFactory ----
    // Runs last: the factory slot is JVM-global and can only be set once.
    static void testGlobalFactoryIntegration() throws Exception {
        final RecordingProvider provider = new RecordingProvider();
        URL.setURLStreamHandlerFactory(provider);
        check(true, "provider accepted as global URLStreamHandlerFactory (first set)");

        URL url = new URL("dummy://somehost/some/path?x=1");
        check("dummy".equals(url.getProtocol()), "URL routed through provider");
        check("somehost".equals(url.getHost()), "custom handler parses host");
        check(url.getPort() == -1, "no port in spec -> -1");
        check("/some/path".equals(url.getPath()), "custom handler parses path");
        check("x=1".equals(url.getQuery()), "custom handler parses query");
        check("/some/path?x=1".equals(url.getFile()), "file is path+query");
        check("somehost".equals(url.getAuthority()), "authority without port");
        check("dummy://somehost/some/path?x=1".equals(url.toExternalForm()),
                "full round-trip external form");
        check(provider.asked.contains("dummy"), "provider observed protocol 'dummy'");

        // Handler caching: a second URL of the same protocol does not re-ask (probed).
        int before = provider.asked.size();
        new URL("dummy://otherhost/other");
        check(provider.asked.size() == before,
                "protocol->handler mapping cached by URL after first creation (probed)");

        // Built-in protocols still work when the provider declines them (probed).
        URL httpUrl = new URL("http://127.0.0.1:1/p");
        check("http".equals(httpUrl.getProtocol()), "http falls back to built-in handler");
        check(httpUrl.getPort() == 1, "http port parsed by built-in handler");
        check(provider.asked.contains("http"), "provider was consulted for http and declined");

        // Protocol case: URL lower-cases the scheme before lookup (probed).
        new URL("DUMMY://casing.example/x");
        check(true, "upper-case scheme constructs fine (lower-cased internally)");
        check("dummy".equals(new URL("DUMMY://casing.example/x").getProtocol()),
                "scheme normalized to lower case");

        // Unknown protocol that neither provider nor built-ins serve.
        try {
            new URL("nosuchproto://h/p");
            check(false, "unknown protocol must fail");
        } catch (MalformedURLException e) {
            check(e.getMessage() != null && e.getMessage().contains("unknown protocol"),
                    "MalformedURLException 'unknown protocol' when provider declines");
        }

        // The global factory slot rejects a second installation with Error (probed).
        try {
            URL.setURLStreamHandlerFactory(new RecordingProvider());
            check(false, "second factory set must fail");
        } catch (Throwable t) {
            check(t instanceof Error, "second set -> Error");
            check(t.getMessage() != null && t.getMessage().contains("already defined"),
                    "Error message: factory already defined");
        }
        try {
            URL.setURLStreamHandlerFactory(null);
            check(false, "even null cannot reset the factory");
        } catch (Throwable t) {
            check(t instanceof Error, "null reset also -> Error");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxShape();
        testFactoryObject();
        testGlobalFactoryIntegration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
