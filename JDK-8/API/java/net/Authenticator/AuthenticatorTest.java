/*
 * @test
 * @summary java.net.Authenticator conformance: abstract class hierarchy and members.
 * @run main AuthenticatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;

public class AuthenticatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** Records every protected getter as observed inside getPasswordAuthentication. */
    static class RecordingAuth extends Authenticator {
        String host; int port = -999; InetAddress site; String prompt; String protocol;
        String scheme; URL url; RequestorType rt; int calls;
        PasswordAuthentication returned;
        @Override protected PasswordAuthentication getPasswordAuthentication() {
            calls++;
            host = getRequestingHost();
            port = getRequestingPort();
            site = getRequestingSite();
            prompt = getRequestingPrompt();
            protocol = getRequestingProtocol();
            scheme = getRequestingScheme();
            url = getRequestingURL();
            rt = getRequestorType();
            returned = new PasswordAuthentication("rec-" + calls, ("pw" + calls).toCharArray());
            return returned;
        }
    }

    static class NullAuth extends Authenticator {
        int calls;
        @Override protected PasswordAuthentication getPasswordAuthentication() {
            calls++;
            return null;
        }
    }

    static class ThrowingAuth extends Authenticator {
        final RuntimeException boom = new IllegalStateException("auth-boom");
        @Override protected PasswordAuthentication getPasswordAuthentication() { throw boom; }
    }

    // ---- white-box structure (javap java.net.Authenticator, JDK-8) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.Authenticator");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(Modifier.isPublic(c.getConstructor().getModifiers()), "public no-arg ctor");
        // no abstract methods at all: an empty { } subclass compiles (see testAnonymousSubclass)
        int abstractCount = 0;
        for (Method m : c.getDeclaredMethods()) if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount == 0, "zero abstract methods");
        // the three static requestPasswordAuthentication overloads of JDK-8
        check(c.getMethod("requestPasswordAuthentication", InetAddress.class, int.class,
            String.class, String.class, String.class) != null, "5-arg static overload");
        check(c.getMethod("requestPasswordAuthentication", String.class, InetAddress.class, int.class,
            String.class, String.class, String.class) != null, "6-arg static overload");
        check(c.getMethod("requestPasswordAuthentication", String.class, InetAddress.class, int.class,
            String.class, String.class, String.class, URL.class,
            Authenticator.RequestorType.class) != null, "8-arg static overload");
        check(Modifier.isSynchronized(c.getMethod("setDefault", Authenticator.class).getModifiers()),
            "setDefault synchronized");
        // JDK-8 does NOT have: getDefault(), requestPasswordAuthenticationInstance(),
        // the 9-arg static overload, or getRequestingKeyStore() (all JDK-9+)
        boolean absent;
        absent = false; try { c.getMethod("getDefault"); } catch (NoSuchMethodException e) { absent = true; }
        check(absent, "getDefault absent on JDK-8");
        absent = false; try { c.getMethod("requestPasswordAuthenticationInstance", String.class,
            InetAddress.class, int.class, String.class, String.class, String.class, URL.class,
            Authenticator.RequestorType.class); } catch (NoSuchMethodException e) { absent = true; }
        check(absent, "requestPasswordAuthenticationInstance absent on JDK-8");
        absent = false; try { c.getMethod("requestPasswordAuthentication", Authenticator.class, String.class,
            InetAddress.class, int.class, String.class, String.class, String.class, URL.class,
            Authenticator.RequestorType.class); } catch (NoSuchMethodException e) { absent = true; }
        check(absent, "9-arg static overload absent on JDK-8");
        absent = false; try { c.getDeclaredMethod("getRequestingKeyStore"); } catch (NoSuchMethodException e) { absent = true; }
        check(absent, "getRequestingKeyStore absent on JDK-8");
        // protected getters present
        String[] getters = {"getRequestingHost", "getRequestingSite", "getRequestingPort",
            "getRequestingProtocol", "getRequestingPrompt", "getRequestingScheme",
            "getRequestingURL", "getRequestorType"};
        for (String g : getters) check(c.getDeclaredMethod(g) != null, "getter " + g);
        check(Modifier.isProtected(c.getDeclaredMethod("getPasswordAuthentication").getModifiers()),
            "getPasswordAuthentication protected");
        check(!Modifier.isAbstract(c.getDeclaredMethod("getPasswordAuthentication").getModifiers()),
            "getPasswordAuthentication non-abstract (base returns null)");
        // nested RequestorType enum surface (full matrix in RequestorTypeTest)
        Class<?> rt = Class.forName("java.net.Authenticator$RequestorType");
        check(rt.isEnum(), "RequestorType is enum");
        check(rt.getEnumConstants().length == 2, "RequestorType has 2 constants");
    }

    // ---- anonymous subclass with no overrides is legal (no abstract methods) ----
    static void testAnonymousSubclass() throws Exception {
        Authenticator bare = new Authenticator() { };
        check(bare != null, "bare anonymous subclass instantiable");
        check(bare instanceof Authenticator, "type");
        // base getPasswordAuthentication returns null: static dispatch yields null result
        try {
            Authenticator.setDefault(bare);
            PasswordAuthentication r = Authenticator.requestPasswordAuthentication(
                InetAddress.getByName("127.0.0.1"), 80, "http", "p", "s");
            check(r == null, "base callback returns null result");
        } finally {
            Authenticator.setDefault(null);
        }
    }

    // ---- static requestPasswordAuthentication: exact context propagation (probed) ----
    static void testStaticContextPropagation() throws Exception {
        RecordingAuth rec = new RecordingAuth();
        try {
            Authenticator.setDefault(rec);
            InetAddress loop = InetAddress.getByName("127.0.0.1");
            int before = rec.calls;
            PasswordAuthentication r1 = Authenticator.requestPasswordAuthentication(
                loop, 8080, "http", "prompt-A", "basic");
            check(rec.calls == before + 1, "5-arg invokes callback once");
            check(r1 == rec.returned, "static returns the exact object the callback produced");
            check(r1.getUserName().equals("rec-1"), "result username from callback");
            check(new String(r1.getPassword()).equals("pw1"), "result password from callback");
            check(rec.host == null, "5-arg leaves requesting host null");
            check(rec.port == 8080, "port propagated");
            check(rec.site != null && rec.site.equals(loop), "site propagated");
            check("prompt-A".equals(rec.prompt), "prompt propagated");
            check("http".equals(rec.protocol), "protocol propagated");
            check("basic".equals(rec.scheme), "scheme propagated");
            check(rec.url == null, "5-arg leaves URL null");
            check(rec.rt == Authenticator.RequestorType.SERVER, "default RequestorType SERVER");

            before = rec.calls;
            PasswordAuthentication r2 = Authenticator.requestPasswordAuthentication(
                "hostA", loop, 8081, "ftp", "prompt-B", "digest");
            check(rec.calls == before + 1, "6-arg invokes callback once");
            check(r2 == rec.returned, "6-arg exact result");
            check("hostA".equals(rec.host), "6-arg sets host");
            check(rec.port == 8081, "6-arg port");
            check("ftp".equals(rec.protocol), "6-arg protocol");
            check("prompt-B".equals(rec.prompt), "6-arg prompt");
            check("digest".equals(rec.scheme), "6-arg scheme");
            check(rec.url == null, "6-arg leaves URL null");
            check(rec.rt == Authenticator.RequestorType.SERVER, "6-arg default rt SERVER");

            before = rec.calls;
            URL u = new URL("http://127.0.0.1/x");
            PasswordAuthentication r3 = Authenticator.requestPasswordAuthentication(
                "hostB", loop, 8082, "http", "prompt-C", "digest", u, Authenticator.RequestorType.PROXY);
            check(rec.calls == before + 1, "8-arg invokes callback once");
            check(r3 == rec.returned, "8-arg exact result");
            check("hostB".equals(rec.host), "8-arg host");
            check(rec.url != null && rec.url.equals(u), "8-arg URL propagated");
            check(rec.rt == Authenticator.RequestorType.PROXY, "8-arg RequestorType PROXY propagated");
            check(rec.port == 8082, "8-arg port");
            // a second 8-arg call with the other RequestorType value flips the getter
            Authenticator.requestPasswordAuthentication("hostC", loop, 8083, "http",
                "prompt-D", "digest", u, Authenticator.RequestorType.SERVER);
            check(rec.rt == Authenticator.RequestorType.SERVER, "RequestorType SERVER propagated");
            check(rec.port == 8083, "second 8-arg port");
        } finally {
            Authenticator.setDefault(null);
        }
    }

    // ---- null result / null default / exception transparency ----
    static void testNullAndExceptionPaths() throws Exception {
        // fresh JVM: no default authenticator -> static request returns null immediately
        // (probed: returns null; all parameters are literals/loopback IPs, no network)
        PasswordAuthentication none = Authenticator.requestPasswordAuthentication(
            InetAddress.getByName("127.0.0.1"), 80, "http", "p", "s");
        check(none == null, "no default registered -> null result");

        NullAuth na = new NullAuth();
        try {
            Authenticator.setDefault(na);
            check(na.calls == 0, "callback not yet invoked");
            PasswordAuthentication r = Authenticator.requestPasswordAuthentication(
                InetAddress.getByName("127.0.0.1"), 80, "http", "p", "s");
            check(na.calls == 1, "callback invoked");
            check(r == null, "null callback result passes through");

            ThrowingAuth ta = new ThrowingAuth();
            Authenticator.setDefault(ta);
            try {
                Authenticator.requestPasswordAuthentication(
                    InetAddress.getByName("127.0.0.1"), 80, "http", "p", "s");
                check(false, "callback exception must propagate");
            } catch (IllegalStateException e) {
                check(e == ta.boom, "exact exception object propagates");
            }
            // replacing the default switches the callback used
            RecordingAuth rec = new RecordingAuth();
            Authenticator.setDefault(rec);
            PasswordAuthentication r2 = Authenticator.requestPasswordAuthentication(
                InetAddress.getByName("127.0.0.1"), 80, "http", "p", "s");
            check(rec.calls == 1, "new default gets the call");
            check(r2 == rec.returned, "new default result returned");
            // setDefault(null) resets; static then returns null again
            Authenticator.setDefault(null);
            check(Authenticator.requestPasswordAuthentication(
                InetAddress.getByName("127.0.0.1"), 80, "http", "p", "s") == null,
                "setDefault(null) resets to no-authenticator");
            // and a default can be installed again afterwards
            Authenticator.setDefault(na);
            check(na.calls == 1, "still one call");
            Authenticator.requestPasswordAuthentication(
                InetAddress.getByName("127.0.0.1"), 80, "http", "p", "s");
            check(na.calls == 2, "reinstalled default receives calls");
        } finally {
            Authenticator.setDefault(null);
        }
    }

    // ---- values recorded by the getters during callback (probed on JDK-8) ----
    static void testStickyState() throws Exception {
        RecordingAuth rec = new RecordingAuth();
        try {
            Authenticator.setDefault(rec);
            Authenticator.requestPasswordAuthentication("sticky", InetAddress.getByName("127.0.0.1"),
                7777, "https", "prompt-S", "ntlm");
            check("sticky".equals(rec.host), "host recorded");
            check(rec.port == 7777, "port recorded");
            check("https".equals(rec.protocol), "protocol recorded");
            check("ntlm".equals(rec.scheme), "scheme recorded");
            check("prompt-S".equals(rec.prompt), "prompt recorded");
            // the getters are only meaningful inside the callback, but re-reading
            // them later still yields the last context (probed sticky behavior)
            class ReReader extends Authenticator {
                String lateHost; int latePort;
                void read() { lateHost = getRequestingHost(); latePort = getRequestingPort(); }
            }
            ReReader rr = new ReReader();
            rr.read();
            check(rr.lateHost == null && rr.latePort == 0,
                "a never-invoked instance sees null host / port 0");
        } finally {
            Authenticator.setDefault(null);
        }
    }

    // ---- boundary/null parameter propagation (probed identical on JDK-8/27) ----
    static void testBoundaryPropagation() throws Exception {
        RecordingAuth rec = new RecordingAuth();
        try {
            Authenticator.setDefault(rec);
            InetAddress loop = InetAddress.getByName("127.0.0.1");
            // 5-arg with null strings and negative port: no validation, getters see them
            Authenticator.requestPasswordAuthentication(loop, -1, null, null, null);
            check(rec.port == -1, "negative port passes through");
            check(rec.prompt == null, "null prompt visible");
            check(rec.protocol == null, "null protocol visible");
            check(rec.scheme == null, "null scheme visible");
            check(rec.rt == Authenticator.RequestorType.SERVER, "rt SERVER with nulls");
            // 6-arg with null host and out-of-range port
            Authenticator.requestPasswordAuthentication(null, loop, 65536, "p", "pr", "sch");
            check(rec.host == null, "null host visible");
            check(rec.port == 65536, "port 65536 passes through unvalidated");
            // 8-arg with port 0 and RequestorType PROXY
            Authenticator.requestPasswordAuthentication("h", loop, 0, "p", "pr", "sch",
                new URL("http://127.0.0.1/"), Authenticator.RequestorType.PROXY);
            check(rec.port == 0, "port 0 visible");
            check(rec.rt == Authenticator.RequestorType.PROXY, "PROXY visible");
            // a following 5-arg call resets RequestorType to SERVER (probed)
            Authenticator.requestPasswordAuthentication(loop, 80, "p", "pr", "sch");
            check(rec.rt == Authenticator.RequestorType.SERVER, "5-arg resets rt to SERVER");
            check(rec.host == null, "5-arg resets host to null");
            // 6-arg with everything null: even the site is null and port survives
            Authenticator.requestPasswordAuthentication(null, null, -5, null, null, null);
            check(rec.site == null, "null site visible");
            check(rec.port == -5, "arbitrary negative port visible");
            check(rec.rt == Authenticator.RequestorType.SERVER, "all-null rt SERVER");
        } finally {
            Authenticator.setDefault(null);
        }
    }

    // ---- result identity semantics and setDefault(null) idempotency ----
    static void testResultIdentityAndReset() throws Exception {
        final PasswordAuthentication shared = new PasswordAuthentication("shared", "sp".toCharArray());
        Authenticator same = new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() { return shared; }
        };
        try {
            Authenticator.setDefault(same);
            PasswordAuthentication a = Authenticator.requestPasswordAuthentication(
                InetAddress.getByName("127.0.0.1"), 80, "http", "p", "s");
            PasswordAuthentication b = Authenticator.requestPasswordAuthentication(
                InetAddress.getByName("127.0.0.1"), 81, "http", "p", "s");
            check(a == shared && b == shared, "same cached instance returned each call");
            // RecordingAuth hands out a new instance per call: static must return the latest
            RecordingAuth rec = new RecordingAuth();
            Authenticator.setDefault(rec);
            PasswordAuthentication r1 = Authenticator.requestPasswordAuthentication(
                InetAddress.getByName("127.0.0.1"), 80, "http", "p", "s");
            PasswordAuthentication r2 = Authenticator.requestPasswordAuthentication(
                InetAddress.getByName("127.0.0.1"), 81, "http", "p", "s");
            check(r1 != r2, "distinct results when callback rebuilds");
            check(r1 == rec.returned || r2 == rec.returned, "latest result wins");
            check(r2 == rec.returned, "static returns most recent callback result");
        } finally {
            Authenticator.setDefault(null);
        }
        // setDefault(null) is idempotent and re-installable
        Authenticator.setDefault(null);
        passed++;
        Authenticator.setDefault(same);
        check(Authenticator.requestPasswordAuthentication(
            InetAddress.getByName("127.0.0.1"), 80, "http", "p", "s") == shared, "reinstalled default works");
        Authenticator.setDefault(null);
        check(Authenticator.requestPasswordAuthentication(
            InetAddress.getByName("127.0.0.1"), 80, "http", "p", "s") == null, "final reset works");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.Authenticator");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("requestPasswordAuthentication"), "has method requestPasswordAuthentication");
        check(names.contains("setDefault"), "has method setDefault");
        testWhiteBox();
        testAnonymousSubclass();
        testStaticContextPropagation();
        testNullAndExceptionPaths();
        testStickyState();
        testBoundaryPropagation();
        testResultIdentityAndReset();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
