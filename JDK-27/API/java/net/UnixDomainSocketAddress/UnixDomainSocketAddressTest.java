/*
 * @test
 * @summary java.net.UnixDomainSocketAddress conformance: white-box structure + black-box behavior.
 * @run main UnixDomainSocketAddressTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;

public class UnixDomainSocketAddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.UnixDomainSocketAddress");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(SocketAddress.class.isAssignableFrom(c), "extends SocketAddress");
        check(c.getMethod("of", String.class) != null, "of(String)");
        check(c.getMethod("of", Path.class) != null, "of(Path)");
        check(c.getMethod("getPath") != null, "getPath");
    }

    static void testBlackBox() throws Exception {
        UnixDomainSocketAddress u = UnixDomainSocketAddress.of("/tmp/jck-sock");
        check(u.getPath().toString().equals("/tmp/jck-sock"), "path from string");
        check(u.toString().equals("/tmp/jck-sock"), "toString");

        UnixDomainSocketAddress u2 = UnixDomainSocketAddress.of(Paths.get("/tmp/jck-sock"));
        check(u2.getPath().equals(u.getPath()), "path from Path");

        check(u.equals(UnixDomainSocketAddress.of("/tmp/jck-sock")), "equals");
        check(u.hashCode() == UnixDomainSocketAddress.of("/tmp/jck-sock").hashCode(), "hashCode");
        check(!u.equals(UnixDomainSocketAddress.of("/tmp/other")), "not equals different path");
        check(!u.equals(new InetSocketAddress(1)), "not equals other type");

        fails(NullPointerException.class, "of null string", () -> UnixDomainSocketAddress.of((String) null));
        fails(NullPointerException.class, "of null path", () -> UnixDomainSocketAddress.of((Path) null));
        fails(InvalidPathException.class, "embedded null char", () -> UnixDomainSocketAddress.of("\u0000bad"));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
