/*
 * @test
 * @summary java.net.ContentHandler conformance: white-box structure + black-box behavior.
 *          Deep contract coverage: abstract getContent(URLConnection) recording contract,
 *          the DEFAULT two-arg getContent(URLConnection, Class[]) implementation (delegates
 *          to single-arg once per call, first isInstance match wins, superclass matches,
 *          no match / empty array -> null, null element -> NPE), and overriding both forms.
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
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends Object");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getMethod("getContent", URLConnection.class) != null, "getContent(URLConnection)");
        check(Modifier.isAbstract(c.getMethod("getContent", URLConnection.class).getModifiers()),
                "getContent(URLConnection) abstract");
        check(c.getMethod("getContent", URLConnection.class, Class[].class) != null, "getContent(URLConnection,Class[])");
        check(!Modifier.isAbstract(c.getMethod("getContent", URLConnection.class, Class[].class).getModifiers()),
                "getContent(URLConnection,Class[]) concrete (default impl)");
    }

    /** Recording handler returning a fixed sentinel from the single-arg form. */
    static class Recording extends ContentHandler {
        int singleCalls;
        URLConnection lastConn;
        Object sentinel = "sentinel";
        public Object getContent(URLConnection urlc) {
            singleCalls++;
            lastConn = urlc;
            return sentinel;
        }
    }

    static void testSingleArgContract() throws Exception {
        Recording ch = new Recording();
        URLConnection probe = new URLConnection(new URL("http://example.com/")) {
            public void connect() { connected = true; }
        };
        Object o = ch.getContent(probe);
        check(o == ch.sentinel, "getContent returns the subclass value");
        check(ch.singleCalls == 1, "getContent invoked once");
        check(ch.lastConn == probe, "connection argument passed through by identity");
        // null connection is passed verbatim (no validation in the abstract contract)
        Object n = ch.getContent(null);
        check(n == ch.sentinel && ch.lastConn == null, "null connection accepted, recorded");
        // second distinct connection
        URLConnection p2 = new URLConnection(new URL("http://example.com/b")) {
            public void connect() { connected = true; }
        };
        ch.getContent(p2);
        check(ch.singleCalls == 3 && ch.lastConn == p2, "third call records third connection");
    }

    static void testTwoArgDefaultDelegation() throws Exception {
        Recording ch = new Recording();
        // default two-arg: delegates to single-arg exactly once per call, returns its
        // result when one of the classes isInstance-matches it, else null
        Object m1 = ch.getContent(null, new Class<?>[]{String.class});
        check(m1 == ch.sentinel, "two-arg exact class match returns single-arg result");
        check(ch.singleCalls == 1, "two-arg delegates to single-arg once");
        Object m2 = ch.getContent(null, new Class<?>[]{CharSequence.class});
        check(m2 == ch.sentinel, "superclass (CharSequence) matches String result");
        check(ch.singleCalls == 2, "one delegation per two-arg call");
        Object m3 = ch.getContent(null, new Class<?>[]{Integer.class});
        check(m3 == null, "no matching class -> null (single-arg result discarded)");
        check(ch.singleCalls == 3, "still delegated exactly once");
        Object m4 = ch.getContent(null, new Class<?>[0]);
        check(m4 == null, "empty class array -> null");
        check(ch.singleCalls == 4, "empty array still delegates");
        // first matching class in order wins
        Object m5 = ch.getContent(null, new Class<?>[]{Integer.class, CharSequence.class});
        check(m5 == ch.sentinel, "first isInstance match in array order");
        Object m6 = ch.getContent(null, new Class<?>[]{Object.class});
        check(m6 == ch.sentinel, "Object.class matches everything");
        // null element inside the array -> NPE from Class.isInstance (probed both JDKs;
        // message text differs between JDK-8 and JDK-27 so only the type is asserted)
        try {
            ch.getContent(null, new Class<?>[]{null});
            check(false, "null class element should throw NPE");
        } catch (NullPointerException npe) {
            check(true, "null class element NPE");
        }
        // the classes array itself is never forwarded to the single-arg form
        check(ch.lastConn == null, "connection still forwarded, classes not");
    }

    static void testTwoArgOverride() throws Exception {
        final Object[] seen = {null, null};
        ContentHandler ch = new ContentHandler() {
            public Object getContent(URLConnection c) { return "single"; }
            @SuppressWarnings("unchecked")
            public Object getContent(URLConnection c, Class[] classes) {
                seen[0] = c;
                seen[1] = classes;
                return "two";
            }
        };
        URLConnection probe = new URLConnection(new URL("http://example.com/")) {
            public void connect() { connected = true; }
        };
        Class<?>[] arr = {String.class};
        Object r = ch.getContent(probe, arr);
        check("two".equals(r), "overridden two-arg used");
        check(seen[0] == probe, "overridden two-arg receives connection");
        check(seen[1] == arr, "overridden two-arg receives the exact classes array");
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
        testSingleArgContract();
        testTwoArgDefaultDelegation();
        testTwoArgOverride();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
