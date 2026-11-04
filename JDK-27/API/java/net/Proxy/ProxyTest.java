/*
 * @test
 * @summary java.net.Proxy conformance: type/address/NO_PROXY.
 * @run main ProxyTest
 */

import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class ProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void check(boolean c) { check(c, "assertion"); }
    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }
    static void failsWithMsg(Class<? extends Throwable> ex, String needle, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) {
                String msg = String.valueOf(t.getMessage());
                if (msg.contains(needle)) { passed++; return; }
                throw new AssertionError(m + " message lacks '" + needle + "': " + msg);
            }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    // ---- original white-box assertions (preserved verbatim) ----
    static void testOriginalWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.Proxy");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getField("NO_PROXY") != null, "NO_PROXY");
        check(c.getConstructor(Proxy.Type.class, java.net.SocketAddress.class) != null, "ctor(Type, SocketAddress)");
        check(c.getMethod("type") != null, "type()");
        check(c.getMethod("address") != null, "address()");
        Class<?> t = Class.forName("java.net.Proxy$Type");
        check(Modifier.isPublic(t.getModifiers()) && t.isEnum(), "Type enum");
    }

    // ---- original black-box assertions (preserved verbatim) ----
    static void testOriginalBlackBox() throws Exception {
        Proxy direct = Proxy.NO_PROXY;
        check(direct.type() == Proxy.Type.DIRECT, "NO_PROXY type DIRECT");
        check(direct.address() == null, "NO_PROXY null address");
        check(direct.toString().equals("DIRECT"), "DIRECT toString");

        InetSocketAddress addr = new InetSocketAddress("localhost", 3128);
        Proxy http = new Proxy(Proxy.Type.HTTP, addr);
        check(http.type() == Proxy.Type.HTTP, "HTTP type");
        check(http.address().equals(addr), "HTTP address");
        check(http.toString().startsWith("HTTP @ "), "HTTP toString prefix");
        check(http.toString().endsWith(":3128"), "HTTP toString port");

        Proxy http2 = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 3128));
        check(http.equals(http2), "equals");
        check(http.hashCode() == http2.hashCode(), "hashCode");
        check(!http.equals(new Proxy(Proxy.Type.SOCKS, addr)), "different type not equal");
        check(!http.equals(direct), "different proxy not equal");

        Proxy.Type[] types = Proxy.Type.values();
        check(types.length == 3, "3 proxy types");
        check(Proxy.Type.valueOf("DIRECT") == Proxy.Type.DIRECT, "valueOf DIRECT");
        check(Proxy.Type.valueOf("HTTP") == Proxy.Type.HTTP, "valueOf HTTP");
        check(Proxy.Type.valueOf("SOCKS") == Proxy.Type.SOCKS, "valueOf SOCKS");

        fails(IllegalArgumentException.class, "DIRECT with address", () -> new Proxy(Proxy.Type.DIRECT, addr));
        fails(IllegalArgumentException.class, "null type", () -> new Proxy(null, null));
        fails(IllegalArgumentException.class, "valueOf bogus", () -> Proxy.Type.valueOf("BOGUS"));
    }

    // ---- white-box structure (javap -public java.net.Proxy, JDK-27) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.Proxy");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        java.lang.reflect.Field f = c.getField("NO_PROXY");
        check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
            && Modifier.isPublic(f.getModifiers()), "NO_PROXY public static final");
        check(f.getType() == Proxy.class, "NO_PROXY typed Proxy");
        check(c.getConstructor(Proxy.Type.class, java.net.SocketAddress.class) != null, "ctor(Type,SocketAddress)");
        check(c.getMethod("type").getReturnType() == Proxy.Type.class, "type() returns Type");
        check(c.getMethod("address").getReturnType() == java.net.SocketAddress.class, "address() returns SocketAddress");
        check(Modifier.isFinal(c.getMethod("equals", Object.class).getModifiers()), "equals final");
        check(Modifier.isFinal(c.getMethod("hashCode").getModifiers()), "hashCode final");
    }

    // ---- NO_PROXY constant ----
    static void testNoProxy() {
        Proxy none = Proxy.NO_PROXY;
        check(none != null, "NO_PROXY non-null");
        check(Proxy.NO_PROXY == none, "NO_PROXY singleton identity");
        check(none.type() == Proxy.Type.DIRECT, "NO_PROXY type DIRECT");
        check(none.address() == null, "NO_PROXY address null");
        check("DIRECT".equals(none.toString()), "NO_PROXY toString DIRECT");
        check(none.equals(none), "NO_PROXY equals self");
        check(none.equals(Proxy.NO_PROXY), "NO_PROXY equals constant");
        check(!none.equals(null), "NO_PROXY not equals null");
        check(!none.equals("DIRECT"), "NO_PROXY not equals String");
        check(none.hashCode() == Proxy.NO_PROXY.hashCode(), "NO_PROXY hashCode consistent");
    }

    // ---- constructor + accessors, exact toString (probed on JDK-27) ----
    static void testCtorAndAccessors() {
        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 8080);
        Proxy http = new Proxy(Proxy.Type.HTTP, addr);
        check(http.type() == Proxy.Type.HTTP, "HTTP type");
        check(http.address() != null, "HTTP address non-null");
        check(http.address().equals(addr), "HTTP address equals");
        check(http.address() == addr, "HTTP address same reference");
        check(((InetSocketAddress) http.address()).getPort() == 8080, "address port");
        check("HTTP @ /127.0.0.1:8080".equals(http.toString()), "HTTP toString exact");

        Proxy socks = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080));
        check(socks.type() == Proxy.Type.SOCKS, "SOCKS type");
        check(((InetSocketAddress) socks.address()).getPort() == 1080, "SOCKS port");
        check("SOCKS @ /127.0.0.1:1080".equals(socks.toString()), "SOCKS toString exact");

        // unresolved address is legal; JDK-13+ InetSocketAddress.toString renders
        // unresolved endpoints as "host/<unresolved>:port" (probed on JDK-27)
        java.net.SocketAddress unres = InetSocketAddress.createUnresolved("proxy.example.com", 3128);
        check(((InetSocketAddress) unres).isUnresolved(), "unresolved helper");
        Proxy up = new Proxy(Proxy.Type.HTTP, unres);
        check(up.type() == Proxy.Type.HTTP, "unresolved proxy type");
        check(up.address() == unres, "unresolved address kept");
        check("HTTP @ proxy.example.com/<unresolved>:3128".equals(up.toString()),
            "unresolved toString (JDK-13+ form)");
    }

    // ---- constructor error paths (probed; ctor checks type==DIRECT || !(sa instanceof InetSocketAddress)) ----
    static void testCtorErrors() {
        final InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 8080);
        // null type + valid InetSocketAddress is NOT rejected: legacy quirk, type() stays null
        Proxy nullTyped = new Proxy(null, addr);
        check(nullTyped.type() == null, "null type passes with InetSocketAddress");
        check("null @ /127.0.0.1:8080".equals(nullTyped.toString()), "null-type toString");
        check(nullTyped.equals(new Proxy(null, new InetSocketAddress("127.0.0.1", 8080))), "null-type twin equal");
        check(!nullTyped.equals(new Proxy(Proxy.Type.HTTP, addr)), "null-type != HTTP");
        fails(NullPointerException.class, "null-type hashCode NPE", nullTyped::hashCode);
        fails(IllegalArgumentException.class, "null type null addr", () -> new Proxy(null, null));
        failsWithMsg(IllegalArgumentException.class, "DIRECT", "DIRECT with address", () -> new Proxy(Proxy.Type.DIRECT, addr));
        fails(IllegalArgumentException.class, "DIRECT null address", () -> new Proxy(Proxy.Type.DIRECT, null));
        failsWithMsg(IllegalArgumentException.class, "null", "HTTP null address", () -> new Proxy(Proxy.Type.HTTP, null));
        fails(IllegalArgumentException.class, "SOCKS null address", () -> new Proxy(Proxy.Type.SOCKS, null));
        // address must be an InetSocketAddress, not merely non-null
        failsWithMsg(IllegalArgumentException.class, "not compatible", "custom SocketAddress subclass",
            () -> new Proxy(Proxy.Type.HTTP, new java.net.SocketAddress() { }));
    }

    // ---- equals/hashCode contract ----
    static void testEqualsHashCode() {
        InetSocketAddress a = new InetSocketAddress("127.0.0.1", 8080);
        InetSocketAddress b = new InetSocketAddress("127.0.0.1", 8080);
        check(a.equals(b), "pre: addresses equal");
        Proxy p1 = new Proxy(Proxy.Type.HTTP, a);
        Proxy p2 = new Proxy(Proxy.Type.HTTP, b);
        check(p1.equals(p1), "reflexive");
        check(p1.equals(p2), "same type+address equal");
        check(p2.equals(p1), "symmetric");
        check(p1.hashCode() == p2.hashCode(), "equal => equal hashCode");
        check(!p1.equals(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8081))), "different port unequal");
        check(!p1.equals(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.2", 8080))), "different host unequal");
        Proxy socksA = new Proxy(Proxy.Type.SOCKS, a);
        check(!p1.equals(socksA), "different type unequal");
        check(!socksA.equals(p1), "different type unequal symmetric");
        check(!p1.equals(null), "equals null false");
        check(!p1.equals("HTTP @ /127.0.0.1:8080"), "not equal to String twin");
        check(!p1.equals(Proxy.NO_PROXY), "not equal NO_PROXY");
        check(!Proxy.NO_PROXY.equals(p1), "NO_PROXY not equal HTTP");
        check(p1.hashCode() != socksA.hashCode() || !p1.equals(socksA), "hashCode/type consistency");
        // equal pair built from unresolved addresses
        Proxy u1 = new Proxy(Proxy.Type.SOCKS, InetSocketAddress.createUnresolved("p.example.com", 1080));
        Proxy u2 = new Proxy(Proxy.Type.SOCKS, InetSocketAddress.createUnresolved("p.example.com", 1080));
        check(u1.equals(u2), "unresolved pair equal");
        check(u1.hashCode() == u2.hashCode(), "unresolved pair hashCode");
        check(!u1.equals(new Proxy(Proxy.Type.SOCKS, InetSocketAddress.createUnresolved("q.example.com", 1080))), "unresolved host unequal");
        check(!u1.equals(new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved("p.example.com", 1080))), "unresolved type unequal");
    }

    // ---- enum surface (details live in TypeTest; here only interplay with Proxy) ----
    static void testTypeInterplay() {
        check(Proxy.Type.values().length == 3, "3 types");
        check(Proxy.NO_PROXY.type() == Proxy.Type.valueOf("DIRECT"), "NO_PROXY type via valueOf");
        check(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1)).type().name().equals("HTTP"), "type name");
        check(Proxy.Type.DIRECT.ordinal() < Proxy.Type.HTTP.ordinal(), "DIRECT before HTTP");
        check(Proxy.Type.HTTP.ordinal() < Proxy.Type.SOCKS.ordinal(), "HTTP before SOCKS");
    }

    // ---- equals/hashCode in container semantics ----
    static void testCollectionSemantics() {
        java.util.Set<Proxy> set = new java.util.HashSet<Proxy>();
        Proxy a = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));
        Proxy b = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));
        set.add(a);
        set.add(b);
        check(set.size() == 1, "equal proxies dedupe in HashSet");
        check(set.contains(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080))), "contains equal proxy");
        check(!set.contains(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 8080))), "not contains different type");
        check(!set.contains(Proxy.NO_PROXY), "not contains NO_PROXY");
        set.add(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 8080)));
        check(set.size() == 2, "distinct type kept");
        set.add(Proxy.NO_PROXY);
        check(set.size() == 3, "NO_PROXY kept");
        set.add(Proxy.NO_PROXY);
        check(set.size() == 3, "NO_PROXY deduped");
        check(a.address() == a.address(), "address() stable reference");
        check(a.type() == a.type(), "type() stable");
    }

    public static void main(String[] args) throws Exception {
        testOriginalWhiteBox();
        testOriginalBlackBox();
        testWhiteBox();
        testNoProxy();
        testCtorAndAccessors();
        testCtorErrors();
        testEqualsHashCode();
        testTypeInterplay();
        testCollectionSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
